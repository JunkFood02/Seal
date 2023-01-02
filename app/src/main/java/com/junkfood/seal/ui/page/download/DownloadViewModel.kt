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
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.DownloadTaskItem
import com.junkfood.seal.R
import com.junkfood.seal.Downloader
import com.junkfood.seal.Downloader.downloaderState
import com.junkfood.seal.Downloader.mutablePlaylistResult
import com.junkfood.seal.Downloader.mutableTaskState
import com.junkfood.seal.Downloader.taskState
import com.junkfood.seal.Downloader.State
import com.junkfood.seal.Downloader.mutableErrorState
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.FORMAT_SELECTION
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.toHttpsUrl
import com.junkfood.seal.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        val showFormatSelectionPage: Boolean = false,
        val isUrlSharingTriggered: Boolean = false,
        val debugMode: Boolean = false
    )

    private fun clearProgressState() =
        mutableTaskState.update { it.copy(progress = 0f, progressText = "") }

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) =
        mutableViewStateFlow.update {
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

    private fun parsePlaylistInfo() {
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch

            val url = viewStateFlow.value.url
            Downloader.clearErrorState()
            Downloader.run {
                clearErrorState()
                updateState(State.FetchingInfo)
            }

            try {
                val info = DownloadUtil.getPlaylistOrVideoInfo(url)
                Downloader.updateState(State.Idle)
                when (info) {
                    is PlaylistResult -> {
                        showPlaylistPage(info)
                    }

                    is VideoInfo -> {
                        if (PreferenceUtil.getValue(FORMAT_SELECTION, true)) {
                            showFormatSelectionPage(info)
                        } else if (checkStateBeforeDownload()) {
                            downloadVideo(videoInfo = info)
                        }
                    }
                }
            } catch (e: Exception) {
                manageDownloadError(e)
            }
        }

    }

    fun downloadVideoInPlaylistByIndexList(
        playlistResult: PlaylistResult = Downloader.playlistResult.value,
        url: String = playlistResult.webpageUrl.toString(),
        indexList: List<Int>
    ) {
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            val itemCount = indexList.size
            if (!checkStateBeforeDownload()) return@launch
            Downloader.updateState(
                State.DownloadingPlaylist(
                    currentItem = 1,
                    itemCount = itemCount
                )
            )

            for (i in indexList.indices) {
                if (downloaderState.value !is State.DownloadingPlaylist) break
                with(playlistResult) {
                    val task = DownloadTaskItem(
                        webpageUrl = url,
                        title = title.toString(),
                        uploader = uploader ?: channel ?: "null",
                        playlistIndex = indexList[i]
                    )
                    downloaderState.value.run {
                        if (this is State.DownloadingPlaylist)
                            Downloader.updateState(copy(currentItem = i + 1))
                    }

                    if (App.isServiceRunning) NotificationUtil.updateServiceNotification(
                        index = i + 1, itemCount = itemCount
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
        val url = viewStateFlow.value.url

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
        if (url.isBlank()) {
            viewModelScope.launch { showErrorMessage(context.getString(R.string.url_empty)) }
            return
        }
        if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo()
            return
        }

        if (PreferenceUtil.getValue(FORMAT_SELECTION, true)) {
            viewModelScope.launch(Dispatchers.IO) {
                fetchVideoInfo(url = url, isFormatSelectionEnabled = true)
                mutableViewStateFlow.update { it.copy(showFormatSelectionPage = true) }
            }
            return
        }

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                fetchVideoInfo(url = url)?.let {
                    downloadVideo(videoInfo = it)
                }
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
        }
    }

    private suspend fun executeCustomCommand() {
        DownloadUtil.executeCommandInBackground(viewStateFlow.value.url)
    }

    private fun fetchInfoForFormatSelection(url: String) {
        Downloader.updateState(State.FetchingInfo)
        try {
            val videoInfo = DownloadUtil.fetchVideoInfoFromUrl(url = url)
            if (videoInfo.formats.isNullOrEmpty()) {
                throw Exception(context.getString(R.string.fetch_info_error_msg))
            }
            videoInfoFlow.update { videoInfo }
        } catch (e: Exception) {
            manageDownloadError(e)
        }
        Downloader.updateState(State.Idle)
    }

    /**
     * TODO: Split to multiple methods without side effects
     */
    private fun fetchVideoInfo(
        url: String,
        task: DownloadTaskItem = DownloadTaskItem(),
        isFormatSelectionEnabled: Boolean = false
    ): VideoInfo? {
        Downloader.updateState(State.FetchingInfo)

        val videoInfo = try {
            DownloadUtil.fetchVideoInfoFromUrl(url = url, playlistItem = task.playlistIndex)
        } catch (e: Exception) {
            manageDownloadError(e)
            return null
        }


        if (isFormatSelectionEnabled) {
            runCatching {
                if (videoInfo.formats?.isEmpty() == true) {
                    throw Exception(context.getString(R.string.fetch_info_error_msg))
                }
                videoInfoFlow.update { videoInfo }
            }.onFailure { manageDownloadError(it) }
        } else {
            updateDownloadTask(videoInfo, task)
        }
        Downloader.updateState(State.Idle)


        return videoInfo
    }


    private fun updateDownloadTask(videoInfo: VideoInfo, newTask: DownloadTaskItem) =
        mutableTaskState.update {
            newTask.copy(
                progress = 0f,
                taskId = videoInfo.id,
                title = videoInfo.title,
                uploader = videoInfo.uploader.toString(),
                thumbnailUrl = videoInfo.thumbnail.toHttpsUrl(),
                duration = videoInfo.duration?.roundToInt() ?: 0,
                fileSizeApprox = videoInfo.fileSize ?: videoInfo.fileSizeApprox ?: 0
            )
        }

    fun downloadVideoWithFormatId(videoInfo: VideoInfo, formatList: List<Format>) {
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            val fileSize = formatList.fold(0L) { acc, format ->
                acc + (format.fileSize ?: format.fileSizeApprox ?: 0L)
            }

            val info = videoInfo.run { if (fileSize != 0L) copy(fileSize = fileSize) else this }

            val audioOnly =
                formatList.isNotEmpty() && formatList.fold(true) { acc: Boolean, format: Format ->
                    acc && (format.vcodec == "none" && format.acodec != "none")
                }
            val formatId = formatList.fold("") { s, format ->
                s + "+" + format.formatId
            }.removePrefix("+")

            val downloadPreferences = DownloadUtil.DownloadPreferences().run {
                copy(extractAudio = extractAudio || audioOnly, formatId = formatId)
            }
            updateDownloadTask(
                info,
                DownloadTaskItem()
            )
            kotlin.runCatching {
                downloadVideo(
                    videoInfo = info,
                    downloadPreferences = downloadPreferences
                )
            }.onFailure { manageDownloadError(it) }
        }
    }

    private fun downloadVideo(
        playlistIndex: Int = 1,
        playlistUrl: String = "",
        videoInfo: VideoInfo,
        downloadPreferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ) {
        with(downloaderState) {
            if (value !is State.DownloadingPlaylist)
                Downloader.updateState(State.DownloadingVideo)

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
                notificationId = notificationId, title = videoInfo.title
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
                if (value !is State.DownloadingPlaylist) finishProcessing()
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

    /**
     * TODO: Rewrite state related methods
     */
    private fun checkStateBeforeDownload(): Boolean {
        if (downloaderState.value !is State.Idle) {
            TextUtil.makeToastSuspend(context.getString(R.string.task_running))
            return false
        }
        return true
    }

    private fun finishProcessing() {
        if (downloaderState.value is State.Idle) return
        mutableTaskState.update {
            it.copy(progress = 100f, progressText = "")
        }
        Downloader.updateState(State.Idle)
        mutablePlaylistResult.update { PlaylistResult() }
    }


    /**
     * TODO: Rewrite to reflect new error states
     */
    private fun manageDownloadError(
        th: Throwable, isFetchingInfo: Boolean = true, notificationId: Int? = null
    ) {
        viewModelScope.launch {
            if (th is YoutubeDL.CanceledException) return@launch
            th.printStackTrace()
            if (PreferenceUtil.getValue(PreferenceUtil.DEBUG)) showErrorReport(
                th.message ?: context.getString(R.string.unknown_error)
            )
            else if (isFetchingInfo) showErrorMessage(context.getString(R.string.fetch_info_error_msg))
            else showErrorMessage(context.getString(R.string.download_error_msg))
            notificationId?.let {
                NotificationUtil.finishNotification(
                    notificationId, text = context.getString(R.string.download_error_msg),
                )
            }
        }
    }


    private fun showErrorReport(report: String) {
        clearProgressState()
        mutableErrorState.update { it.copy(errorReport = report) }
    }

    private fun showErrorMessage(msg: String, report: String = "") {
        TextUtil.makeToastSuspend(msg)
        clearProgressState()
        mutableErrorState.update {
            it.copy(errorReport = report, errorMessage = msg)
        }
    }


    fun openVideoFile() {
        if (taskState.value.progress == 100f) openFile(downloadResultTemp)
    }

    private fun showPlaylistPage(playlistResult: PlaylistResult) {
        mutablePlaylistResult.update { playlistResult }
        mutableViewStateFlow.update {
            it.copy(
                showPlaylistSelectionDialog = true,
            )
        }
    }

    private fun showFormatSelectionPage(info: VideoInfo) {
        videoInfoFlow.update { info }
        mutableViewStateFlow.update {
            it.copy(
                showFormatSelectionPage = true,
            )
        }

    }

    fun hidePlaylistDialog() {
        mutableViewStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun hideFormatPage() {
        mutableViewStateFlow.update { it.copy(showFormatSelectionPage = false) }
    }

    fun cancelDownload() {
        TextUtil.makeToast(context.getString(R.string.task_canceled))
        currentJob?.cancel(CancellationException(context.getString(R.string.task_canceled)))
        mutableTaskState.update {
            it.copy(
                progress = 0f,
                progressText = "",
            )
        }
        Downloader.updateState(State.Idle)
        mutablePlaylistResult.update { PlaylistResult() }

        val taskId = taskState.value.taskId
        YoutubeDL.getInstance().destroyProcessById(taskId)
        NotificationUtil.cancelNotification(taskId.hashCode())
    }

    fun onShareIntentConsumed() {
        mutableViewStateFlow.update { it.copy(isUrlSharingTriggered = false) }
    }

    fun clearPlaylistResult() {
        mutablePlaylistResult.update { PlaylistResult() }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}