package com.junkfood.seal.ui.page.download

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.graphics.drawable.BitmapDrawable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
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
        val progressText: String = "",
        val IsExecutingCommand: Boolean = false,
        val customCommandMode: Boolean = false,
        val isProcessing: Boolean = false,
        val debugMode: Boolean = false,
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            isSkipHalfExpanded = true
        ),
        val palette: Palette? = null,
        val stopNext:Boolean = false,
        val playlistCount: Int = 0,
        val playlistIndex: Int = 0
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

    fun CoroutineScope.manageDownloadError(e: Exception) = launch {
        e.printStackTrace()
        if (PreferenceUtil.getValue(PreferenceUtil.DEBUG))
            showErrorReport(e.message ?: context.getString(R.string.unknown_error))
        else showErrorMessage(context.getString(R.string.fetch_info_error_msg))
    }

    fun startDownloadVideo() {
        switchDownloadMode(PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND))

        if (viewState.value.isProcessing) {
            TextUtil.makeToast(context.getString(R.string.task_running))
            return
        }
        _viewState.update {
            it.copy(
                isProcessing = true, debugMode = PreferenceUtil.getValue(
                    PreferenceUtil.DEBUG
                )
            )
        }
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
                update { it.copy(isDownloadError = false, stopNext = false) }
                var videoInfo: VideoInfo
                var plCount: Int = 1
                try {
                    plCount = DownloadUtil.fetchPlaylistInfo(viewState.value.url)
                } catch (e: Exception) {
                    manageDownloadError(e)
                    return@launch
                }
                update { it.copy(playlistCount = plCount) }
                var notificationID = value.url.hashCode()
                for (i in 0 until plCount) {       // i in 1 until 10, excluding 10
                    try {
                        videoInfo = DownloadUtil.fetchVideoInfo(viewState.value.url, i+1)
                    } catch (e: Exception) {
                        continue
                    }
/*                val palette = extractColorsFromImage(
                    TextUtil.urlHttpToHttps(
                        videoInfo.thumbnail ?: ""
                    )
                )*/
                    update {
                        it.copy(
                            progress = 0f,
                            playlistIndex = i,
                            showVideoCard = true,
                            videoTitle = videoInfo.title,
                            videoAuthor = videoInfo.uploader ?: "null",
                            videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail ?: "")
//                        palette = palette
                        )
                    }
//                PreferenceUtil.modifyThemeColor(palette.getVibrantColor(0xffffff))
                    notificationID = (value.url + (if (videoInfo.id != null) videoInfo.id else i)).hashCode()
                    val appendS = " [" + (i+1) + "/" + plCount + "]"
                    try {
                        NotificationUtil.makeNotification(
                            notificationID,
                            title = context.getString(R.string.download_notification_template)
                                .format(videoInfo.title) + appendS, text = ""
                        )
                        TextUtil.makeToastSuspend(
                            context.getString(R.string.download_start_msg).format(videoInfo.title) + appendS
                        )
                        downloadResultTemp = DownloadUtil.downloadVideo(value.url, videoInfo) { progress, _, line ->
                            _viewState.update { it.copy(progress = progress, progressText = line) }
                            NotificationUtil.updateNotification(
                                notificationID,
                                progress = progress.toInt(),
                                text = line
                            )
                        }

                    } catch (e: Exception) {
                        manageDownloadError(e)
                        NotificationUtil.finishNotification(
                            notificationID,
                            title = videoInfo.title,
                            text = context.getString(R.string.download_error_msg),
                            intent = null
                        )
                        return@launch
                    }
                val intent = FileUtil.createIntentForOpenFile(downloadResultTemp)
                NotificationUtil.finishNotification(
                    notificationID,
                    title = videoInfo.title,
                    text = context.getString(R.string.download_finish_notification),
                    intent = if (intent != null) PendingIntent.getActivity(
                        context,
                        0,
                        FileUtil.createIntentForOpenFile(downloadResultTemp), FLAG_IMMUTABLE
                    ) else null
                )
                    if (value.stopNext)
                        break
                }

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
                    _viewState.update { it.copy(progress = progress, progressText = line) }
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
                IsExecutingCommand = false, isProcessing = false, progressText = ""
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
                IsExecutingCommand = false, isProcessing = false, progressText = ""
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
                IsExecutingCommand = false, isProcessing = false, progressText = ""
            )
        }
    }

    suspend fun extractColorsFromImage(url: String): Palette {
        return Palette.Builder(
            (ImageLoader(context).execute(
                ImageRequest.Builder(context).data(url).allowHardware(false).build()
            ).drawable as BitmapDrawable).bitmap
        ).generate()
    }

    fun openVideoFile() {
        if (_viewState.value.progress == 100f)
            openFile(downloadResultTemp)
    }

    fun stopNext(v: Boolean) {
        _viewState.update {
            it.copy(
                stopNext = v
            )
        }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
        private const val commandRegex = "\"([^\"]*)\"|(\\S+)"
    }
}