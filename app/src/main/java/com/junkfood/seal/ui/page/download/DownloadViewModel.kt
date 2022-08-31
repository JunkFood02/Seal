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
import com.junkfood.seal.util.*
import com.junkfood.seal.util.FileUtil.openFile
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.CancellationException
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)

// TODO: Refactoring for introducing multitasking and download queue management
class DownloadViewModel @Inject constructor() : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(DownloadViewState())
    val stateFlow = mutableStateFlow.asStateFlow()
    lateinit var currentJob: Job

    data class DownloadViewState(
        val showVideoCard: Boolean = false,
        val showPlaylistSelectionDialog: Boolean = false,
        val progress: Float = 0f,
        val url: String = "",
        val videoTitle: String = "",
        val videoThumbnailUrl: String = "",
        val videoAuthor: String = "",
        val isDownloadError: Boolean = false,
        val errorMessage: String = "",
        val progressText: String = "",
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
        val downloadingTaskId: String = "",
        val downloadItemCount: Int = 0,
        val currentIndex: Int = 0,
        val playlistInfo: DownloadUtil.PlaylistInfo = DownloadUtil.PlaylistInfo()
    )

    data class DownloadTaskViewState(
        val title: String = "",
        val artist: String = "",
        val progress: Float = 0f,
        val thumbnail: String = "",
        val progressText: String = "",
    )

    fun updateUrl(url: String) = mutableStateFlow.update { it.copy(url = url) }

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
            if (PreferenceUtil.getValue(PreferenceUtil.DEBUG))
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
        viewModelScope.launch(Dispatchers.IO) {
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
                        isFetchingInfo = true
                    )
                }
                try {
                    val playlistInfo = DownloadUtil.getPlaylistInfo(value.url)
//                    Log.d(TAG, playlistInfo.toString())
                    if (playlistInfo.size == 1) downloadVideo(value.url)
                    else showPlaylistDialog(playlistInfo)
                } catch (e: Exception) {
                    manageDownloadError(e)
                }
            }
        }
    }

    fun downloadVideoInPlaylistByIndexRange(
        url: String = stateFlow.value.url,
        indexRange: IntRange
    ) {
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            val itemCount = indexRange.last - indexRange.first + 1
            if (!checkStateBeforeDownload()) return@launch
            mutableStateFlow.update {
                it.copy(
                    isProcessRunning = true,
                    isDownloadingPlaylist = true,
                    downloadItemCount = itemCount
                )
            }
            for (index in indexRange) {
                if (!stateFlow.value.isDownloadingPlaylist) break
                mutableStateFlow.update { it.copy(currentIndex = index - indexRange.first + 1) }
                if (MainActivity.isServiceRunning)
                    NotificationUtil.updateServiceNotification(
                        index - indexRange.first + 1,
                        itemCount
                    )
                downloadVideo(url, index)
            }
            finishProcessing()
        }
    }

    fun startDownloadVideo() {
        if (stateFlow.value.url.isBlank()) {
            viewModelScope.launch { showErrorMessage(context.getString(R.string.url_empty)) }
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

        currentJob = viewModelScope.launch(Dispatchers.Default) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                downloadVideo(stateFlow.value.url)
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
            finishProcessing()
        }
    }

    private fun downloadVideo(url: String, index: Int = 1) {
        with(mutableStateFlow) {
            lateinit var videoInfo: VideoInfo
            try {
                update { it.copy(isFetchingInfo = true) }
                runBlocking { videoInfo = DownloadUtil.fetchVideoInfo(url, index) }
                update { it.copy(isFetchingInfo = false) }
            } catch (e: Exception) {
                manageDownloadError(e)
                return
            }
            update { it.copy(isDownloadError = false) }
            Log.d(TAG, "downloadVideo: $index" + videoInfo.title)
            if (value.isCancelled) return
            update {
                it.copy(
                    progress = 0f,
                    showVideoCard = true,
                    isProcessRunning = true,
                    downloadingTaskId = videoInfo.id,
                    videoTitle = videoInfo.title,
                    videoAuthor = videoInfo.uploader ?: "null",
                    videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
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
                        playlistInfo = stateFlow.value.playlistInfo,
                        playlistItem = index
                    ) { progress, _, line ->
                        Log.d(TAG, line)
                        mutableStateFlow.update {
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

            } catch (e: Exception) {
                manageDownloadError(e, false, notificationId)
                return
            }
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
        }
    }


    private fun switchDownloadMode(enabled: Boolean) {
        with(mutableStateFlow) {
            if (enabled) {
                update {
                    it.copy(
                        showVideoCard = false,
                        isInCustomCommandMode = true,
                    )
                }
            } else update { it.copy(isInCustomCommandMode = false) }
        }
    }

    private fun downloadWithCustomCommands() {

        mutableStateFlow.update {
            it.copy(
                isDownloadError = false,
                progress = 0f,
                debugMode = true,
                isCancelled = false
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            downloadResultTemp = DownloadUtil.Result.failure()
            try {
                if (stateFlow.value.url.isNotEmpty())
                    with(DownloadUtil.fetchVideoInfo(stateFlow.value.url)) {
                        if (!title.isNullOrEmpty() and !thumbnail.isNullOrEmpty())
                            mutableStateFlow.update {
                                it.copy(
                                    videoTitle = title,
                                    videoThumbnailUrl = TextUtil.urlHttpToHttps(thumbnail),
                                    videoAuthor = uploader ?: "null",
                                    showVideoCard = true
                                )
                            }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val notificationId = stateFlow.value.url.hashCode()

        TextUtil.makeToast(context.getString(R.string.start_execute))
        NotificationUtil.makeNotification(
            notificationId,
            title = context.getString(R.string.execute_command_notification), text = ""
        )
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                with(mutableStateFlow) {
                    val request = YoutubeDLRequest(value.url)
                    request.addOption("-P", "${BaseApplication.videoDownloadDir}/")
                    val m = Pattern.compile(commandRegex).matcher(PreferenceUtil.getTemplate())
                    val commands = ArrayList<String>()
                    while (m.find()) {
                        if (m.group(1) != null) {
                            commands.add(m.group(1).toString())
                        } else {
                            commands.add(m.group(2).toString())
                        }
                    }
                    request.addCommands(commands)
                    update { it.copy(downloadingTaskId = it.url, isProcessRunning = true) }
                    YoutubeDL.getInstance()
                        .execute(request, value.url) { progress, _, line ->
                            update {
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
                }
            } catch (e: Exception) {
                manageDownloadError(e, false, notificationId)
                return@launch
            }
        }
    }

    private suspend fun checkStateBeforeDownload(): Boolean {
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

    private suspend fun finishProcessing() {
        if (stateFlow.value.isCancelled) return
        mutableStateFlow.update {
            it.copy(
                progress = 100f,
                isProcessRunning = false,
                isFetchingInfo = false,
                progressText = "",
                downloadItemCount = 0,
                isDownloadingPlaylist = false,
                currentIndex = 0,
                playlistInfo = DownloadUtil.PlaylistInfo()
            )
        }
        MainActivity.stopService()
        if (!stateFlow.value.isDownloadError)
            TextUtil.makeToastSuspend(context.getString(R.string.download_success_msg))
    }

    private suspend fun showErrorReport(s: String) {
        TextUtil.makeToastSuspend(context.getString(R.string.error_copied))
        mutableStateFlow.update {
            it.copy(
                progress = 0f,
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false, progressText = "",
                isFetchingInfo = false
            )
        }
    }

    private suspend fun showErrorMessage(s: String) {
        TextUtil.makeToastSuspend(s)
        mutableStateFlow.update {
            it.copy(
                progress = 0f,
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false, progressText = "",
                isFetchingInfo = false
            )
        }
    }


    fun openVideoFile() {
        if (mutableStateFlow.value.progress == 100f)
            openFile(downloadResultTemp)
    }

    private fun showPlaylistDialog(playlistInfo: DownloadUtil.PlaylistInfo) {
        mutableStateFlow.update {
            it.copy(
                showPlaylistSelectionDialog = true,
                downloadItemCount = playlistInfo.size,
                isFetchingInfo = false,
                playlistInfo = playlistInfo
            )
        }
    }

    fun hidePlaylistDialog() {
        mutableStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun cancelDownload() {
        TextUtil.makeToast(context.getString(R.string.task_canceled))
        currentJob.cancel(CancellationException(context.getString(R.string.task_canceled)))
        MainActivity.stopService()
        mutableStateFlow.update {
            it.copy(
                isProcessRunning = false,
                isDownloadingPlaylist = false,
                isFetchingInfo = false,
                progress = 0f,
                progressText = "",
                isCancelled = true
            )
        }
        YoutubeDL.getInstance().destroyProcessById(stateFlow.value.downloadingTaskId)
        NotificationUtil.cancelNotification(stateFlow.value.downloadingTaskId.hashCode())
    }

    companion object {
        private const val TAG = "DownloadViewModel"
        private const val commandRegex = "\"([^\"]*)\"|(\\S+)"
    }
}