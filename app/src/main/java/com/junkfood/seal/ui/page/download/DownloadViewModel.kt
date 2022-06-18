package com.junkfood.seal.ui.page.download

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
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

    private val _viewState = MutableStateFlow(DownloadViewState())
    val viewState = _viewState.asStateFlow()

    data class DownloadViewState(
        val showVideoCard: Boolean = false,
        val progress: Float = 0f,
        val url: String = "",
        val videoTitle: String = "",
        val videoThumbnailUrl: String = "",
        val videoAuthor: String = "",
        val isDownloadError: Boolean = false,
        val errorMessage: String = "",
        val IsExecutingCommand: Boolean = false,
        val customCommandMode: Boolean = false,
        val isProcessing: Boolean = false,
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            isSkipHalfExpanded = true
        )
    )

    fun updateUrl(url: String) = _viewState.update { it.copy(url = url) }

    fun hideDrawer(scope: CoroutineScope) {
        scope.launch {
            viewState.value.drawerState.hide()
        }
    }

    fun showDrawer(scope: CoroutineScope) {
        scope.launch {
            viewState.value.drawerState.show()
        }
    }

    private var downloadResultTemp: DownloadUtil.Result = DownloadUtil.Result.failure()

    fun startDownloadVideo() {
        switchDownloadMode(PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND))

        if (viewState.value.isProcessing) {
            TextUtil.makeToast(context.getString(R.string.task_running))
            return
        }
        _viewState.update { it.copy(isProcessing = true) }
        if (viewState.value.customCommandMode) {
            downloadWithCustomCommands()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            with(_viewState) {
                if (value.url.isBlank()) {
                    showErrorMessage(context.getString(R.string.url_empty))
                    return@launch
                }
                update { it.copy(isDownloadError = false) }
                val videoInfo: VideoInfo
                try {
                    videoInfo = DownloadUtil.fetchVideoInfo(viewState.value.url)
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (PreferenceUtil.getValue(PreferenceUtil.DEBUG))
                        showErrorReport(e.message ?: context.getString(R.string.unknown_error))
                    else showErrorMessage(context.getString(R.string.fetch_info_error_msg))
                    return@launch
                }
                update {
                    it.copy(
                        progress = 0f,
                        showVideoCard = true,
                        videoTitle = videoInfo.title,
                        videoAuthor = videoInfo.uploader ?: "null",
                        videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
                    )
                }
                val notificationID = value.url.hashCode()
                try {
                    NotificationUtil.makeNotification(
                        notificationID,
                        title = context.getString(R.string.download_notification_template)
                            .format(videoInfo.title), text = ""
                    )
                    TextUtil.makeToastSuspend(
                        context.getString(R.string.download_start_msg).format(videoInfo.title)
                    )
                    downloadResultTemp = DownloadUtil.downloadVideo(value.url, videoInfo)
                    { progress, _, line ->
                        _viewState.update { it.copy(progress = progress) }
                        NotificationUtil.updateNotification(
                            notificationID,
                            progress = progress.toInt(),
                            text = line
                        )

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (PreferenceUtil.getValue(PreferenceUtil.DEBUG))
                        showErrorReport(e.message ?: context.getString(R.string.unknown_error))
                    else showErrorMessage(context.getString(R.string.download_error_msg))
                    NotificationUtil.finishNotification(
                        notificationID,
                        title = videoInfo.title,
                        text = context.getString(R.string.download_error_msg),
                        intent = null
                    )
                    return@launch
                }
                NotificationUtil.finishNotification(
                    notificationID,
                    title = videoInfo.title,
                    text = context.getString(R.string.download_finish_notification),
                    intent = PendingIntent.getActivity(
                        context,
                        0,
                        FileUtil.createIntentForOpenFile(downloadResultTemp), FLAG_IMMUTABLE
                    )
                )
                finishProcessing()
                /*                if (PreferenceUtil.getValue(PreferenceUtil.OPEN_IMMEDIATELY))
                                    openFile(downloadResultTemp)*/
            }
        }
    }

    private fun switchDownloadMode(enabled: Boolean) {
        with(_viewState) {
            if (enabled) {
                update {
                    it.copy(
                        showVideoCard = false,
                        customCommandMode = true,
                    )
                }
            } else update { it.copy(customCommandMode = false) }
        }
    }

    private fun downloadWithCustomCommands() {
        val request = YoutubeDLRequest(viewState.value.url)
        request.addOption("-P", "${BaseApplication.downloadDir}/")
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
        _viewState.update { it.copy(isDownloadError = false, progress = 0f) }
        viewModelScope.launch(Dispatchers.IO) {
            downloadResultTemp = DownloadUtil.Result.failure()
            try {
                if (viewState.value.url.isNotEmpty())
                    with(DownloadUtil.fetchVideoInfo(viewState.value.url)) {
                        if (!title.isNullOrEmpty() and !thumbnail.isNullOrEmpty())
                            _viewState.update {
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
        val notificationID = viewState.value.url.hashCode()

        TextUtil.makeToast(context.getString(R.string.start_execute))
        NotificationUtil.makeNotification(
            notificationID,
            title = context.getString(R.string.execute_command_notification), text = ""
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().execute(request) { progress, _, line ->
                    _viewState.update { it.copy(progress = progress) }
                    NotificationUtil.updateNotification(
                        notificationID,
                        progress = progress.toInt(),
                        text = line
                    )
                }
                finishProcessing()
                NotificationUtil.finishNotification(
                    notificationID,
                    title = context.getString(R.string.download_success_msg),
                    text = null,
                    intent = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorReport(e.message ?: context.getString(R.string.unknown_error))
                NotificationUtil.finishNotification(
                    notificationID,
                    title = context.getString(R.string.download_error_msg),
                    text = e.message ?: context.getString(R.string.unknown_error),
                    intent = null
                )
                return@launch
            }
        }
    }

    private suspend fun finishProcessing() {
        _viewState.update {
            it.copy(
                progress = 100f,
                IsExecutingCommand = false, isProcessing = false
            )
        }
        TextUtil.makeToastSuspend(context.getString(R.string.download_success_msg))
    }

    private suspend fun showErrorReport(s: String) {
        TextUtil.makeToastSuspend(context.getString(R.string.error_copied))
        _viewState.update {
            it.copy(
                progress = 0f,
                isDownloadError = true,
                errorMessage = s,
                IsExecutingCommand = false, isProcessing = false
            )
        }
    }

    private suspend fun showErrorMessage(s: String) {
        TextUtil.makeToastSuspend(s)
        _viewState.update {
            it.copy(
                progress = 0f,
                isDownloadError = true,
                errorMessage = s,
                IsExecutingCommand = false, isProcessing = false
            )
        }
    }

    fun openVideoFile() {
        if (_viewState.value.progress == 100f)
            openFile(downloadResultTemp)
    }

    companion object {
        private const val TAG = "DownloadViewModel"
        private const val commandRegex = "\"([^\"]*)\"|(\\S+)"
    }
}