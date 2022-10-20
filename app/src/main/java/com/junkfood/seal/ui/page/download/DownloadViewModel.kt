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
import com.junkfood.seal.util.FileUtil.getConfigFile
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.FileUtil.getTempDir
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.PreferenceUtil.CELLULAR_DOWNLOAD
import com.junkfood.seal.util.PreferenceUtil.COOKIES
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)

// TODO: Refactoring for introducing multitasking and download queue management
class DownloadViewModel @Inject constructor() : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(DownloadViewState())
    val stateFlow = mutableStateFlow.asStateFlow()
    private var currentJob: Job? = null

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
        val playlistInfo: DownloadUtil.PlaylistInfo = DownloadUtil.PlaylistInfo(),
        val isUrlSharingTriggered: Boolean = false,
        val isShowingErrorReport: Boolean = false
    )

    data class DownloadTaskViewState(
        val title: String = "",
        val artist: String = "",
        val progress: Float = 0f,
        val thumbnail: String = "",
        val progressText: String = "",
    )

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) =
        mutableStateFlow.update {
            it.copy(
                url = url,
                isUrlSharingTriggered = isUrlSharingTriggered
            )
        }

    fun updateTitle(title: String) {
        mutableStateFlow.update {
            it.copy(
                videoTitle = title
            )
        }
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
                    val playlistInfo = DownloadUtil.getPlaylistInfo(value.url)
                    mutableStateFlow.update {
                        it.copy(
                            downloadItemCount = playlistInfo.size,
                            isFetchingInfo = false,
                            playlistInfo = playlistInfo
                        )
                    }
                    if (playlistInfo.size == 1) {
                        checkStateBeforeDownload()
                        downloadVideo(value.url,0,value.videoTitle)
                        finishProcessing()
                    } else showPlaylistDialog(playlistInfo)
                } catch (e: Exception) {
                    manageDownloadError(e)
                }
            }
        }
    }

    fun downloadVideoInPlaylistByIndexRange(
        url: String = stateFlow.value.url,
        title: String = stateFlow.value.videoTitle,
        indexRange: IntRange
    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
                downloadVideo(url,index,title)
            }
            finishProcessing()
        }
    }

    fun startDownloadVideo() {
        val title = stateFlow.value.videoTitle

        if (stateFlow.value.url.isBlank()) {
            viewModelScope.launch { showErrorMessage(context.getString(R.string.url_empty)) }
            return
        }

        if (!PreferenceUtil.getValue(CELLULAR_DOWNLOAD) && BaseApplication.connectivityManager.isActiveNetworkMetered) {
            viewModelScope.launch {
                showErrorMessage(context.getString(R.string.download_disabled_with_cellular))
            }
            return
        }
        MainActivity.startService()
        switchDownloadMode(PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND))
        if (stateFlow.value.isInCustomCommandMode) {
            viewModelScope.launch { downloadWithCustomCommands(stateFlow.value.videoTitle) }
            return
        } else if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo()
            return
        }

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                downloadVideo(url = stateFlow.value.url, newTitle = title)
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
            finishProcessing()
        }
    }

    private fun downloadVideo(url: String, index: Int = 0,newTitle: String) {

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
            val newVideoTitle: String
            if (newTitle.isBlank()){
                newVideoTitle = videoInfo.title
            }else{
                newVideoTitle = newTitle
            }
            Log.d(TAG, "downloadVideo: $index" + videoInfo.title)
            if (value.isCancelled) return
            update {
                it.copy(
                    progress = 0f,
                    showVideoCard = true,
                    isProcessRunning = true,
                    downloadingTaskId = videoInfo.id,
                    videoTitle = newVideoTitle,
                    videoAuthor = videoInfo.uploader ?: "null",
                    videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
                )
            }
            val notificationId = videoInfo.id.hashCode()
            val intent: Intent?
            viewModelScope.launch(Dispatchers.Main) {
                TextUtil.makeToast(
                    context.getString(R.string.download_start_msg)
                        .format(newVideoTitle)
                )
            }

            NotificationUtil.makeNotification(notificationId, newVideoTitle)
            try {
                downloadResultTemp =
                    DownloadUtil.downloadVideo(
                        videoInfo = videoInfo,
                        playlistInfo = stateFlow.value.playlistInfo,
                        playlistItem = index,
                        newTitle = newVideoTitle,
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
                title = newVideoTitle,
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

    private fun downloadWithCustomCommands(newTitle: String) {
        mutableStateFlow.update {
            it.copy(
                isDownloadError = false,
                progress = 0f,
                debugMode = true,
                isCancelled = false
            )
        }
        val urlList = stateFlow.value.url.split(Regex("[\n ]"))
        downloadResultTemp = DownloadUtil.Result.failure()

        viewModelScope.launch(Dispatchers.IO) {
            if (urlList.size != 1) return@launch
            kotlin.runCatching {
                with(DownloadUtil.fetchVideoInfo(urlList[0],0)) {
                    val newVideoTitle: String
                    if (newTitle.isBlank()){
                        newVideoTitle = title
                    }else{
                        newVideoTitle = newTitle
                    }
                    mutableStateFlow.update {
                        it.copy(
                            videoTitle = newVideoTitle,
                            videoThumbnailUrl = TextUtil.urlHttpToHttps(thumbnail),
                            videoAuthor = uploader.toString(),
                            showVideoCard = true
                        )
                    }
                }
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
                    val request = YoutubeDLRequest(urlList)
                    request.addOption("-P", BaseApplication.videoDownloadDir)
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
                }
            } catch (e: Exception) {
                if (!e.message.isNullOrEmpty()) {
                    manageDownloadError(e, false, notificationId)
                    return@launch
                }
                finishProcessing()
            }
            NotificationUtil.finishNotification(
                notificationId,
                title = context.getString(R.string.download_success_msg),
                text = null,
                intent = null
            )
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

    private fun showErrorReport(s: String) {
        mutableStateFlow.update {
            it.copy(
                progress = 0f,
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false, progressText = "",
                isFetchingInfo = false, isShowingErrorReport = true
            )
        }
    }

    private fun showErrorMessage(s: String) {
        TextUtil.makeToastSuspend(s)
        mutableStateFlow.update {
            it.copy(
                progress = 0f,
                isDownloadError = true,
                errorMessage = s,
                isProcessRunning = false, progressText = "",
                isFetchingInfo = false, isShowingErrorReport = false
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

    fun onShareIntentConsumed() {
        mutableStateFlow.update { it.copy(isUrlSharingTriggered = false) }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}