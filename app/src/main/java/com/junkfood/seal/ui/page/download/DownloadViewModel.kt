package com.junkfood.seal.ui.page.download

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.page.StateHolder
import com.junkfood.seal.ui.page.StateHolder.downloaderState
import com.junkfood.seal.ui.page.StateHolder.mutableDownloaderState
import com.junkfood.seal.ui.page.StateHolder.mutablePlaylistResult
import com.junkfood.seal.ui.page.StateHolder.mutableTaskState
import com.junkfood.seal.ui.page.StateHolder.taskState
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.toHttpsUrl
import com.junkfood.seal.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)

// TODO: Refactoring for introducing multitasking and download queue management
class DownloadViewModel @Inject constructor() : ViewModel() {


    private var currentJob: Job? = null
    private val mutableViewStateFlow = MutableStateFlow(ViewState())
    val viewStateFlow = mutableViewStateFlow.asStateFlow()

    val videoInfoFlow = MutableStateFlow(VideoInfo())

    data class ViewState(
        val showPlaylistSelectionDialog: Boolean = false,
        val url: String = "",
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true
        ),
        val showDownloadSettingDialog: Boolean = false,
    )

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) =
        mutableDownloaderState.update {
            it.copy(
                url = url, isUrlSharingTriggered = isUrlSharingTriggered
            )
        }

    fun hideDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            if (isDialog) mutableViewStateFlow.update { it.copy(showDownloadSettingDialog = false) }
            else viewStateFlow.value.drawerState.hide()
        }
    }

    fun showDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            if (isDialog) mutableViewStateFlow.update { it.copy(showDownloadSettingDialog = true) }
            else viewStateFlow.value.drawerState.show()
        }
    }

    private var downloadResultTemp: DownloadUtil.Result = DownloadUtil.Result.failure()

    private fun manageDownloadError(
        e: Exception, isFetchingInfo: Boolean = true, notificationId: Int? = null
    ) {
        viewModelScope.launch {
            e.printStackTrace()
            if (PreferenceUtil.getValue(PreferenceUtil.DEBUG)) showErrorReport(
                e.message ?: context.getString(R.string.unknown_error)
            )
            else if (isFetchingInfo) showErrorMessage(context.getString(R.string.fetch_info_error_msg))
            else showErrorMessage(context.getString(R.string.download_error_msg))
            notificationId?.let {
                NotificationUtil.finishNotification(
                    notificationId, text = context.getString(R.string.download_error_msg),
                )
            }
            MainActivity.stopService()
        }
    }

    private fun parsePlaylistInfo() {
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            with(mutableDownloaderState) {
                update {
                    it.copy(
                        isDownloadError = false,
                        isDownloadingPlaylist = false,
                        isFetchingInfo = true,
                    )
                }
                try {
                    val playlistResult = DownloadUtil.getPlaylistInfo(value.url)
                    mutableDownloaderState.update {
                        it.copy(
                            downloadItemCount = playlistResult.playlistCount,
                            isFetchingInfo = false,
                        )
                    }
                    if (playlistResult.playlistCount == 1) {
                        checkStateBeforeDownload()
                        fetchVideoInfo(value.url)?.let {
                            downloadVideo(videoInfo = it)
                        }
                    } else {
                        mutablePlaylistResult.update { playlistResult }
                        showPlaylistDialog()
                    }
                } catch (e: Exception) {
                    manageDownloadError(e)
                }
            }
        }
    }

    fun downloadVideoInPlaylistByIndexList(
        playlistResult: PlaylistResult = StateHolder.playlistResult.value,
        url: String = playlistResult.webpageUrl.toString(),
        indexList: List<Int>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val itemCount = indexList.size
            if (!checkStateBeforeDownload()) return@launch
            mutableDownloaderState.update {
                it.copy(
                    isProcessRunning = true,
                    isDownloadingPlaylist = true, currentItem = 1, downloadItemCount = itemCount
                )
            }
            var videoInfoNext: Deferred<VideoInfo>? = null
            for (i in indexList.indices) {
                if (!downloaderState.value.isDownloadingPlaylist) break
                with(playlistResult) {
                    val task = StateHolder.DownloadTaskItem(
                        webpageUrl = url,
                        videoInfo = videoInfoNext?.await(),
                        title = title.toString(),
                        uploader = uploader ?: channel ?: "null",
                        playlistIndex = indexList[i]
                    )
                    if (i != indexList.size - 1) {
                        videoInfoNext = supervisorScope {
                            async(Dispatchers.IO) {
                                val title = playlistResult.entries?.get(indexList[i + 1] - 1)?.title
                                title?.let { Log.d(TAG, "fetching ${it}!") }
                                val res = DownloadUtil.fetchVideoInfoFromUrl(url, indexList[i + 1])
                                title?.let { Log.d(TAG, "finish ${it}!") }
                                return@async res
                            }
                        }
                    }
                    mutableDownloaderState.update { it.copy(currentItem = i + 1) }
                    if (MainActivity.isServiceRunning)
                        NotificationUtil.updateServiceNotification(
                            index = i + 1,
                            itemCount = itemCount
                        )
                    fetchVideoInfo(url, task)?.let {
                        downloadVideo(
                            playlistIndex = task.playlistIndex,
                            playlistUrl = playlistResult.webpageUrl ?: "",
                            videoInfo = it
                        )
                    }
                }
            }
            finishProcessing()
        }
    }

    fun startDownloadVideo() {

        if (!PreferenceUtil.isNetworkAvailableForDownload()) {
            viewModelScope.launch {
                showErrorMessage(context.getString(R.string.download_disabled_with_cellular))
            }
            return
        }
        if (PreferenceUtil.getValue(CUSTOM_COMMAND)) {
            applicationScope.launch(Dispatchers.IO) { executeCustomCommand() }
            return
        }
        if (downloaderState.value.url.isBlank()) {
            viewModelScope.launch { showErrorMessage(context.getString(R.string.url_empty)) }
            return
        }
        MainActivity.startService()
        if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo()
            return
        }

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                fetchVideoInfo(url = downloaderState.value.url)?.let {
                    downloadVideo(videoInfo = it)
                }
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
        }
    }

    private suspend fun executeCustomCommand() {
        DownloadUtil.executeCommandInBackground(downloaderState.value.url)
    }


    private fun fetchVideoInfo(
        url: String,
        task: StateHolder.DownloadTaskItem = StateHolder.DownloadTaskItem()
    ): VideoInfo? {
        val videoInfo = task.videoInfo ?: with(mutableDownloaderState) {
            update { it.copy(isFetchingInfo = true) }
            try {
                return@with DownloadUtil.fetchVideoInfoFromUrl(
                    url = url,
                    playlistItem = task.playlistIndex
                )
            } catch (e: Exception) {
                manageDownloadError(e)
                return null
            }
        }

        updateDownloadTask(videoInfo, task)

        return videoInfo
    }

    private fun updateDownloadTask(videoInfo: VideoInfo, newTask: StateHolder.DownloadTaskItem) =
        mutableTaskState.update {
            newTask.copy(
                videoInfo = videoInfo,
                progress = 0f,
                taskId = videoInfo.id,
                title = videoInfo.title,
                uploader = videoInfo.uploader.toString(),
                thumbnailUrl = videoInfo.thumbnail.toHttpsUrl(),
                duration = videoInfo.duration?.roundToInt() ?: 0,
                fileSizeApprox = videoInfo.fileSize ?: videoInfo.fileSizeApprox ?: 0
            )
        }

    private fun downloadVideo(
        playlistIndex: Int = 1,
        playlistUrl: String = "",
        videoInfo: VideoInfo,
        downloadPreferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ) {
        with(mutableDownloaderState) {
            MainActivity.startService()
            update { it.copy(isDownloadError = false, isProcessRunning = true) }

            Log.d(TAG, "downloadVideo: ${videoInfo.id}" + videoInfo.title)

            val notificationId = videoInfo.id.hashCode()
            Log.d(TAG, notificationId.toString())
            val intent: Intent?
            viewModelScope.launch(Dispatchers.Main) {
                TextUtil.makeToast(
                    context.getString(R.string.download_start_msg).format(videoInfo.title)
                )
            }

            NotificationUtil.notifyProgress(
                notificationId = notificationId,
                title = videoInfo.title
            )
            try {
                downloadResultTemp = DownloadUtil.downloadVideo(
                    videoInfo = videoInfo,
                    playlistUrl = playlistUrl,
                    playlistItem = playlistIndex,
                    downloadPreferences = downloadPreferences
                ) { progress, _, line ->
                    Log.d(TAG, line)
                    mutableTaskState.update {
                        it.copy(progress = progress, progressText = line)
                    }
                    NotificationUtil.notifyProgress(
                        notificationId = notificationId,
                        progress = progress.toInt(),
                        text = line,
                        title = videoInfo.title
                    )
                }
                intent = FileUtil.createIntentForOpenFile(downloadResultTemp)
                if (!downloaderState.value.isDownloadingPlaylist) finishProcessing()
                NotificationUtil.finishNotification(
                    notificationId,
                    title = videoInfo.title,
                    text = context.getString(R.string.download_finish_notification),
                    intent = if (intent != null) PendingIntent.getActivity(
                        context,
                        0,
                        FileUtil.createIntentForOpenFile(downloadResultTemp),
                        FLAG_IMMUTABLE
                    ) else null
                )

            } catch (e: Exception) {
                manageDownloadError(e, false, notificationId)
                return
            }
        }

    }


    private fun checkStateBeforeDownload(): Boolean {
        with(mutableDownloaderState) {
            if (value.isProcessRunning || value.isFetchingInfo) {
                TextUtil.makeToastSuspend(context.getString(R.string.task_running))
                return false
            }
            update {
                it.copy(
                    debugMode = PreferenceUtil.getValue(PreferenceUtil.DEBUG)
                )
            }
        }
        return true
    }

    private fun finishProcessing() {
        if (!downloaderState.value.isProcessRunning) return
        mutableTaskState.update {
            it.copy(progress = 100f, progressText = "")
        }
        mutableDownloaderState.update {
            it.copy(
                isProcessRunning = false,
                isFetchingInfo = false,
                downloadItemCount = 0,
                isDownloadingPlaylist = false,
                currentItem = 0,
            )
        }
        mutablePlaylistResult.update { PlaylistResult() }
        MainActivity.stopService()
        if (!downloaderState.value.isDownloadError) TextUtil.makeToastSuspend(context.getString(R.string.download_success_msg))
    }

    private fun showErrorReport(s: String) {
        mutableTaskState.update {
            it.copy(
                progress = 0f, progressText = "",
            )
        }
        mutableDownloaderState.update {
            it.copy(
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false,
                isFetchingInfo = false,
                isShowingErrorReport = true
            )
        }
    }

    private fun showErrorMessage(s: String) {
        TextUtil.makeToastSuspend(s)
        mutableTaskState.update {
            it.copy(
                progress = 0f, progressText = "",
            )
        }
        mutableDownloaderState.update {
            it.copy(
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false,
                isFetchingInfo = false,
                isShowingErrorReport = false
            )
        }
    }


    fun openVideoFile() {
        if (taskState.value.progress == 100f) openFile(downloadResultTemp)
    }

    private fun showPlaylistDialog() {
        mutableViewStateFlow.update {
            it.copy(
                showPlaylistSelectionDialog = true,
            )
        }
    }

    fun hidePlaylistDialog() {
        mutableViewStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun cancelDownload() {
        TextUtil.makeToast(context.getString(R.string.task_canceled))
        currentJob?.cancel(CancellationException(context.getString(R.string.task_canceled)))
        MainActivity.stopService()
        mutableTaskState.update {
            it.copy(
                progress = 0f,
                progressText = "",
            )
        }
        mutableDownloaderState.update {
            it.copy(
                isProcessRunning = false,
                isDownloadingPlaylist = false,
                isFetchingInfo = false,
            )
        }
        mutablePlaylistResult.update { PlaylistResult() }

        val taskId = taskState.value.taskId
        YoutubeDL.getInstance().destroyProcessById(taskId)
        NotificationUtil.cancelNotification(taskId.hashCode())
    }

    fun onShareIntentConsumed() {
        mutableDownloaderState.update { it.copy(isUrlSharingTriggered = false) }
    }

    fun clearPlaylistResult() {
        mutablePlaylistResult.update { PlaylistResult() }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}