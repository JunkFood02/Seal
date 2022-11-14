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
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getConfigFile
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.FileUtil.getTempDir
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.COOKIES
import com.junkfood.seal.util.PreferenceUtil.PRIVATE_DIRECTORY
import com.junkfood.seal.util.TextUtil
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
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

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)

// TODO: Refactoring for introducing multitasking and download queue management
class DownloadViewModel @Inject constructor() : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(DownloadViewState())
    private val mutableTaskState = MutableStateFlow(DownloadTaskItem())
    private val mutablePlaylistResult = MutableStateFlow(DownloadUtil.PlaylistResult())
    val taskState = mutableTaskState.asStateFlow()
    val stateFlow = mutableStateFlow.asStateFlow()
    val playlistResult = mutablePlaylistResult.asStateFlow()
    private var currentJob: Job? = null

    data class DownloadViewState(
        val showDownloadProgress: Boolean = false,
        val showPlaylistSelectionDialog: Boolean = false,
        val url: String = "",
        val currentDownloadTask: DownloadTaskItem = DownloadTaskItem(),

        val isDownloadError: Boolean = false,
        val errorMessage: String = "",
        val isInCustomCommandMode: Boolean = false,
        val isFetchingInfo: Boolean = false,
        val isProcessRunning: Boolean = false,
        val isCancelled: Boolean = false,
        val debugMode: Boolean = false,
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            isSkipHalfExpanded = true
        ),
        val showDownloadSettingDialog: Boolean = false,
        val isDownloadingPlaylist: Boolean = false,
        val downloadItemCount: Int = 0,
        val currentItem: Int = 0,
        val isUrlSharingTriggered: Boolean = false,
        val isShowingErrorReport: Boolean = false
    )

    data class DownloadTaskItem(
        var videoInfo: VideoInfo? = null,
        val webpageUrl: String = "",
        val title: String = "",
        val uploader: String = "",
        val progress: Float = 0f,
        val progressText: String = "",
        val thumbnailUrl: String = "",
        val taskId: String = "",
        val playlistIndex: Int = 0,
    )

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) =
        mutableStateFlow.update {
            it.copy(
                url = url,
                isUrlSharingTriggered = isUrlSharingTriggered
            )
        }

    fun hideDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            if (isDialog)
                mutableStateFlow.update { it.copy(showDownloadSettingDialog = false) }
            else
                stateFlow.value.drawerState.hide()
        }
    }

    fun showDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            if (isDialog)
                mutableStateFlow.update { it.copy(showDownloadSettingDialog = true) }
            else
                stateFlow.value.drawerState.show()
        }
    }

    private var downloadResultTemp: DownloadUtil.Result = DownloadUtil.Result.failure()

    private fun manageDownloadError(
        e: Exception,
        isFetchingInfo: Boolean = true,
        notificationId: Int? = null
    ) {
        if (stateFlow.value.isCancelled) return
        viewModelScope.launch {
            e.printStackTrace()
            if (PreferenceUtil.getValue(PreferenceUtil.DEBUG) || stateFlow.value.isInCustomCommandMode)
                showErrorReport(e.message ?: context.getString(R.string.unknown_error))
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
            with(mutableStateFlow) {
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
                    mutableStateFlow.update {
                        it.copy(
                            downloadItemCount = playlistResult.playlistCount,
                            isFetchingInfo = false,
                        )
                    }
                    mutablePlaylistResult.update { playlistResult }
                    if (playlistResult.playlistCount == 1) {
                        checkStateBeforeDownload()
                        downloadVideo(value.url)
                    } else showPlaylistDialog()
                } catch (e: Exception) {
                    manageDownloadError(e)
                }
            }
        }
    }

    fun downloadVideoInPlaylistByIndexList(
        playlistResult: DownloadUtil.PlaylistResult = this.playlistResult.value,
        url: String = playlistResult.webpageUrl.toString(),
        indexList: List<Int>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val itemCount = indexList.size
            if (!checkStateBeforeDownload()) return@launch
            mutableStateFlow.update {
                it.copy(
                    isProcessRunning = true,
                    isDownloadingPlaylist = true,
                    downloadItemCount = itemCount
                )
            }
            var videoInfoNext: Deferred<VideoInfo>? = null
            for (i in indexList.indices) {
                if (!stateFlow.value.isDownloadingPlaylist) break
                with(playlistResult) {
                    val task = DownloadTaskItem(
                        webpageUrl = url,
                        videoInfo = videoInfoNext?.await(),
                        title = title.toString(),
                        uploader = uploader.toString(),
                        playlistIndex = indexList[i] + 1
                    )
                    if (i != indexList.size - 1) {
                        videoInfoNext = supervisorScope {
                            async(Dispatchers.IO) {
                                Log.d(TAG, "fetching new!")
                                DownloadUtil.fetchVideoInfo(url, indexList[i + 1])
                            }
                        }
                    }
                    mutableStateFlow.update { it.copy(currentItem = i) }
                    if (MainActivity.isServiceRunning)
                        NotificationUtil.updateServiceNotification(i, itemCount)
                    downloadVideo(url, task)
                }
            }
            finishProcessing()
        }
    }

    fun startDownloadVideo() {
        if (stateFlow.value.url.isBlank()) {
            viewModelScope.launch { showErrorMessage(context.getString(R.string.url_empty)) }
            return
        }
        if (!PreferenceUtil.isNetworkAvailableForDownload()) {
            viewModelScope.launch {
                showErrorMessage(context.getString(R.string.download_disabled_with_cellular))
            }
            return
        }
        MainActivity.startService()
        switchDownloadMode(PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND))
        if (stateFlow.value.isInCustomCommandMode) {
            viewModelScope.launch { downloadWithCustomCommands() }
            return
        } else if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo()
            return
        }

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                downloadVideo(url = stateFlow.value.url)
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
        }
    }

    private fun downloadVideo(
        url: String,
        task: DownloadTaskItem = DownloadTaskItem(),
    ) {
        with(mutableStateFlow) {
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
            Log.d(TAG, "downloadVideo: $task.playlistIndex" + videoInfo.title)
            if (value.isCancelled) return
            update {
                it.copy(
                    showDownloadProgress = true,
                    isProcessRunning = true,
                )
            }
            mutableTaskState.update {
                task.copy(
                    progress = 0f,
                    taskId = videoInfo.id,
                    title = videoInfo.title,
                    uploader = videoInfo.uploader ?: "null",
                    thumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
                )
            }

            val notificationId = videoInfo.id.hashCode()
            val intent: Intent?
            viewModelScope.launch(Dispatchers.Main) {
                TextUtil.makeToast(
                    context.getString(R.string.download_start_msg)
                        .format(videoInfo.title)
                )
            }

            NotificationUtil.makeNotification(notificationId, videoInfo.title)
            try {
                downloadResultTemp =
                    DownloadUtil.downloadVideo(
                        videoInfo = videoInfo,
                        playlistUrl = playlistResult.value.webpageUrl ?: "",
                        playlistItem = task.playlistIndex
                    ) { progress, _, line ->
                        Log.d(TAG, line)
                        mutableTaskState.update {
                            it.copy(
                                progress = progress,
                                progressText = line
                            )
                        }
                        NotificationUtil.updateNotification(
                            notificationId,
                            progress = progress.toInt(),
                            text = line
                        )
                    }
                intent = FileUtil.createIntentForOpenFile(downloadResultTemp)
                if (!stateFlow.value.isDownloadingPlaylist)
                    finishProcessing()
                NotificationUtil.finishNotification(
                    notificationId,
                    title = videoInfo.title,
                    text = context.getString(R.string.download_finish_notification),
                    intent = if (intent != null) PendingIntent.getActivity(
                        context,
                        0,
                        FileUtil.createIntentForOpenFile(downloadResultTemp), FLAG_IMMUTABLE
                    ) else null
                )

            } catch (e: Exception) {
                manageDownloadError(e, false, notificationId)
                return
            }
        }

    }


    private fun switchDownloadMode(enabled: Boolean) {
        with(mutableStateFlow) {
            if (enabled) {
                update {
                    it.copy(
                        showDownloadProgress = true,
                        isInCustomCommandMode = true,
                    )
                }
            } else update { it.copy(isInCustomCommandMode = false) }
        }
    }

    private fun downloadWithCustomCommands() {
        with(mutableStateFlow) {

            update {
                it.copy(
                    isDownloadError = false,
                    debugMode = true,
                    isCancelled = false
                )
            }

            mutableTaskState.update { it.copy(progress = 0f) }

            val urlList = value.url.split(Regex("[\n ]"))
            downloadResultTemp = DownloadUtil.Result.failure()

            val notificationId = stateFlow.value.url.hashCode()

            TextUtil.makeToast(context.getString(R.string.start_execute))
            NotificationUtil.makeNotification(
                notificationId,
                title = context.getString(R.string.execute_command_notification), text = ""
            )
            currentJob = viewModelScope.launch(Dispatchers.IO) {
                try {
                    val request = YoutubeDLRequest(urlList)
                    request.addOption(
                        "-P",
                        if (PreferenceUtil.getValue(PRIVATE_DIRECTORY)) BaseApplication.getPrivateDownloadDirectory() else
                            BaseApplication.videoDownloadDir
                    )
                    request.addOption("-P", "temp:" + context.getTempDir())
                    FileUtil.writeContentToFile(
                        PreferenceUtil.getTemplate(),
                        context.getConfigFile()
                    )
                    request.addOption("--config-locations", context.getConfigFile().absolutePath)
                    if (PreferenceUtil.getValue(COOKIES)) {
                        FileUtil.writeContentToFile(
                            PreferenceUtil.getCookies(),
                            context.getCookiesFile()
                        )
                        request.addOption("--cookies", context.getCookiesFile().absolutePath)
                    }
                    mutableTaskState.update { it.copy(taskId = value.url) }
                    update { it.copy(isProcessRunning = true) }

                    YoutubeDL.getInstance()
                        .execute(request, value.url) { progress, _, line ->
                            mutableTaskState.update {
                                it.copy(progress = progress, progressText = line)
                            }
                            NotificationUtil.updateNotification(
                                notificationId,
                                progress = progress.toInt(),
                                text = line
                            )
                        }
                    finishProcessing()
                    NotificationUtil.finishNotification(
                        notificationId,
                        title = context.getString(R.string.download_success_msg),
                        text = null,
                        intent = null
                    )
                } catch (e: Exception) {
                    if (!e.message.isNullOrEmpty()) {
                        manageDownloadError(e, false, notificationId)
                        return@launch
                    }
                }
            }
        }
    }

    private fun checkStateBeforeDownload(): Boolean {
        with(mutableStateFlow) {
            if (value.isProcessRunning || value.isFetchingInfo) {
                TextUtil.makeToastSuspend(context.getString(R.string.task_running))
                return false
            }
            update {
                it.copy(
                    debugMode = PreferenceUtil.getValue(PreferenceUtil.DEBUG),
                    isCancelled = false
                )
            }
        }
        return true
    }

    private fun finishProcessing() {
        if (stateFlow.value.isCancelled) return
        mutableTaskState.update {
            it.copy(
                progress = 100f, progressText = "",
            )
        }
        mutableStateFlow.update {
            it.copy(
                isProcessRunning = false,
                isFetchingInfo = false,
                downloadItemCount = 0,
                isDownloadingPlaylist = false,
                currentItem = 0,
            )
        }
        mutablePlaylistResult.update { DownloadUtil.PlaylistResult() }
        MainActivity.stopService()
        if (!stateFlow.value.isDownloadError)
            TextUtil.makeToastSuspend(context.getString(R.string.download_success_msg))
    }

    private fun showErrorReport(s: String) {
        mutableTaskState.update {
            it.copy(
                progress = 0f, progressText = "",
            )
        }
        mutableStateFlow.update {
            it.copy(
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false,
                isFetchingInfo = false, isShowingErrorReport = true
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
        mutableStateFlow.update {
            it.copy(
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false,
                isFetchingInfo = false, isShowingErrorReport = false
            )
        }
    }


    fun openVideoFile() {
        if (taskState.value.progress == 100f)
            openFile(downloadResultTemp)
    }

    private fun showPlaylistDialog() {
        mutableStateFlow.update {
            it.copy(
                showPlaylistSelectionDialog = true,
            )
        }
    }

    fun hidePlaylistDialog() {
        mutableStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
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
        mutableStateFlow.update {
            it.copy(
                isProcessRunning = false,
                isDownloadingPlaylist = false,
                isFetchingInfo = false,

                isCancelled = true
            )
        }
        val taskId = taskState.value.taskId
        YoutubeDL.getInstance().destroyProcessById(taskId)
        NotificationUtil.cancelNotification(taskId.hashCode())
    }

    fun onShareIntentConsumed() {
        mutableStateFlow.update { it.copy(isUrlSharingTriggered = false) }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}