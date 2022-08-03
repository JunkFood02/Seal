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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)
class DownloadViewModel @Inject constructor() : ViewModel() {

    private val mutableStateFlow = MutableStateFlow(DownloadViewState())
    val stateFlow = mutableStateFlow.asStateFlow()

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
        val isProcessing: Boolean = false,
        val debugMode: Boolean = false,
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            isSkipHalfExpanded = true
        ),
        val showDownloadSettingDialog: Boolean = false,
        val isDownloadingPlaylist: Boolean = false,
        val downloadItemCount: Int = 0,
        val currentIndex: Int = 0
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
     suspend fun getVideoThumbnailUrl(){
        with(mutableStateFlow) {
            lateinit var videoInfo: VideoInfo
            try {
                videoInfo = DownloadUtil.fetchVideoInfo(stateFlow.value.url, 1)
            } catch (e: Exception) {
                manageDownloadError(e)
                return
            }
            update { it.copy(
                videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
            ) }
        }
    }

     fun showDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            getVideoThumbnailUrl()
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
    ) =
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
                        isProcessing = true
                    )
                }
                try {
                    val playlistSize = DownloadUtil.getPlaylistSize(value.url)
                    Log.d(TAG, playlistSize.toString())
                    if (playlistSize == 1) downloadVideo(value.url)
                    else showPlaylistDialog(playlistSize)
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
        viewModelScope.launch(Dispatchers.IO) {
            val itemCount = indexRange.last - indexRange.first + 1
            if (!checkStateBeforeDownload()) return@launch
            mutableStateFlow.update {
                it.copy(
                    isDownloadingPlaylist = true,
                    downloadItemCount = itemCount
                )
            }
            for (index in indexRange) {
                Log.d(TAG, stateFlow.value.isDownloadingPlaylist.toString())
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
            downloadWithCustomCommands()
            return
        } else if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo()
            return
        }



        viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                downloadVideo(stateFlow.value.url)
            } catch (e: Exception) {
                manageDownloadError(e)
            }
            finishProcessing()
        }
    }

    private suspend fun downloadVideo(url: String, index: Int = 1) {
        with(mutableStateFlow) {

            update { it.copy(isDownloadError = false) }

            lateinit var videoInfo: VideoInfo
            try {
                videoInfo = DownloadUtil.fetchVideoInfo(url, index)
            } catch (e: Exception) {
                manageDownloadError(e)
                return
            }
            Log.d(TAG, "downloadVideo: $index" + videoInfo.title)
            update {
                it.copy(
                    progress = 0f,
                    showVideoCard = true,
                    videoTitle = videoInfo.title,
                    videoAuthor = videoInfo.uploader ?: "null",
                    videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
                )
            }
            val notificationId = (url + index).hashCode()
            var intent: Intent? = null
            try {
                TextUtil.makeToastSuspend(
                    context.getString(R.string.download_start_msg)
                        .format(videoInfo.title)
                )
                NotificationUtil.makeNotification(notificationId, videoInfo.title)
                downloadResultTemp =
                    DownloadUtil.downloadVideo(videoInfo) { progress, _, line ->
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
        val request = YoutubeDLRequest(stateFlow.value.url)
        request.addOption("-P", "${BaseApplication.videoDownloadDir}/")
        val m =
            Pattern.compile(commandRegex)
                .matcher(PreferenceUtil.getTemplate())
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1))
            } else {
                request.addOption(m.group(2))
            }
        }
        mutableStateFlow.update { it.copy(isDownloadError = false, progress = 0f) }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().execute(request) { progress, _, line ->
                    mutableStateFlow.update { it.copy(progress = progress, progressText = line) }
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
                manageDownloadError(e, false, notificationId)
                return@launch
            }
        }
    }

    private suspend fun checkStateBeforeDownload(): Boolean {
        if (stateFlow.value.isProcessing) {
            TextUtil.makeToastSuspend(context.getString(R.string.task_running))
            return false
        }
        mutableStateFlow.update {
            it.copy(
                isProcessing = true, debugMode = PreferenceUtil.getValue(
                    PreferenceUtil.DEBUG
                )
            )
        }
        return true
    }

    private suspend fun finishProcessing() {
        mutableStateFlow.update {
            it.copy(
                progress = 100f,
                isProcessing = false,
                progressText = "",
                downloadItemCount = 0,
                isDownloadingPlaylist = false,
                currentIndex = 0
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
                isProcessing = false, progressText = ""
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
                isProcessing = false, progressText = ""
            )
        }
    }


    fun openVideoFile() {
        if (mutableStateFlow.value.progress == 100f)
            openFile(downloadResultTemp)
    }

    private fun showPlaylistDialog(playlistSize: Int) {
        mutableStateFlow.update {
            it.copy(
                showPlaylistSelectionDialog = true,
                downloadItemCount = playlistSize, isProcessing = false
            )
        }
    }

    fun hidePlaylistDialog() {
        mutableStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun stopDownloadPlaylistOnNextItem() {
        mutableStateFlow.update { it.copy(isDownloadingPlaylist = false) }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
        private const val commandRegex = "\"([^\"]*)\"|(\\S+)"
    }
}