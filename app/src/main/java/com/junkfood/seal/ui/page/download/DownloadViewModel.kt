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
import com.junkfood.seal.ui.page.StateHolder.mutablePlaylistResult
import com.junkfood.seal.ui.page.StateHolder.mutableDownloaderState
import com.junkfood.seal.ui.page.StateHolder.mutableTaskState
import com.junkfood.seal.ui.page.StateHolder.playlistResult
import com.junkfood.seal.ui.page.StateHolder.downloaderState
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

    data class ViewState(
        val showPlaylistSelectionDialog: Boolean = false,
        val url: String = "",
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true
        ),
        val showDownloadSettingDialog: Boolean = false,
    )

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) = mutableDownloaderState.update {
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
        if (downloaderState.value.isCancelled) return
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
                if (value.url.isBlank()) {
                    showErrorMessage(context.getString(R.string.url_empty))
                    return@launch
                }
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
                        downloadVideo(value.url)
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
                    Log.d(TAG, task.toString())
                    if (i != indexList.size - 1) {
                        videoInfoNext = supervisorScope {
                            async(Dispatchers.IO) {
                                Log.d(TAG, "fetching new!")
                                val res = DownloadUtil.fetchVideoInfo(url, indexList[i + 1])
                                Log.d(TAG, "finish!")
                                return@async res
                            }
                        }
                    }
                    mutableDownloaderState.update { it.copy(currentItem = i + 1) }
                    if (MainActivity.isServiceRunning) NotificationUtil.updateServiceNotification(
                        i,
                        itemCount
                    )
                    downloadVideo(url, task)
                }
            }
            finishProcessing()
        }
    }

    fun startDownloadVideo() {
        if (downloaderState.value.url.isBlank()) {
            viewModelScope.launch { showErrorMessage(context.getString(R.string.url_empty)) }
            return
        }
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

        MainActivity.startService()
        if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo()
            return
        }

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                downloadVideo(url = downloaderState.value.url)
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
        }
    }

    private suspend fun executeCustomCommand() {
        DownloadUtil.executeCommandInBackground(downloaderState.value.url)
    }

    private fun downloadVideo(
        url: String,
        task: StateHolder.DownloadTaskItem = StateHolder.DownloadTaskItem(),
    ) {
        with(mutableDownloaderState) {
            if (value.isCancelled) return

            val videoInfo: VideoInfo = if (task.videoInfo == null) {
                val _videoInfo: VideoInfo
                try {
                    update { it.copy(isFetchingInfo = true) }
                    _videoInfo = DownloadUtil.fetchVideoInfo(url, task.playlistIndex)
                    update { it.copy(isFetchingInfo = false) }
                } catch (e: Exception) {
                    manageDownloadError(e)
                    return
                }
                _videoInfo
            } else task.videoInfo!!
            update { it.copy(isDownloadError = false) }
            Log.d(TAG, "downloadVideo: ${videoInfo.id}" + videoInfo.title)
            update {
                it.copy(
                    isProcessRunning = true,
                )
            }
            mutableTaskState.update {
                task.copy(
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

            val notificationId = videoInfo.id.hashCode()
            val intent: Intent?
            viewModelScope.launch(Dispatchers.Main) {
                TextUtil.makeToast(
                    context.getString(R.string.download_start_msg).format(videoInfo.title)
                )
            }

            NotificationUtil.makeNotification(notificationId, videoInfo.title)
            try {
                downloadResultTemp = DownloadUtil.downloadVideo(
                    videoInfo = videoInfo,
                    playlistUrl = playlistResult.value.webpageUrl ?: "",
                    playlistItem = task.playlistIndex
                ) { progress, _, line ->
                    Log.d(TAG, line)
                    mutableTaskState.update {
                        it.copy(
                            progress = progress, progressText = line
                        )
                    }
                    NotificationUtil.updateNotification(
                        notificationId, progress = progress.toInt(), text = line
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
                    debugMode = PreferenceUtil.getValue(PreferenceUtil.DEBUG), isCancelled = false
                )
            }
        }
        return true
    }

    private fun finishProcessing() {
        if (downloaderState.value.isCancelled) return
        mutableTaskState.update {
            it.copy(
                progress = 100f, progressText = "",
            )
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
                isCancelled = true
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