package com.junkfood.seal.ui.page.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor() : ViewModel() {

    private val _viewState = MutableStateFlow(DownloadViewState())
    val viewState = _viewState.asStateFlow()
    private var isDownloading = false

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
        val hintText: String = context.getString(R.string.video_url)
    )

    fun updateUrl(url: String) = _viewState.update { it.copy(url = url) }

    private var downloadResultTemp: DownloadUtil.Result = DownloadUtil.Result.failure()


    fun startDownloadVideo() {
        switchDownloadMode(PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND))

        if (isDownloading) {
            TextUtil.makeToast(context.getString(R.string.task_running))
            return
        }
        isDownloading = true
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
                        showErrorMessage(e.message ?: context.getString(R.string.unknown_error))
                    else showErrorMessage(context.getString(R.string.fetch_info_error_msg))
                    return@launch
                }
                update {
                    it.copy(
                        progress = 0f,
                        showVideoCard = true,
                        videoTitle = videoInfo.title,
                        videoAuthor = videoInfo.uploader,
                        videoThumbnailUrl = TextUtil.urlHttpToHttps(videoInfo.thumbnail)
                    )
                }
                try {
                    downloadResultTemp = DownloadUtil.downloadVideo(value.url, videoInfo)
                    { fl: Float, _: Long, _: String -> _viewState.update { it.copy(progress = fl) } }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (PreferenceUtil.getValue(PreferenceUtil.DEBUG))
                        showErrorMessage(e.message ?: context.getString(R.string.unknown_error))
                    else showErrorMessage(context.getString(R.string.download_error_msg))
                    return@launch
                }

                DatabaseUtil.insertInfo(
                    DownloadedVideoInfo(
                        0,
                        videoInfo.title.toString(),
                        videoInfo.uploader.toString(),
                        viewState.value.url,
                        TextUtil.urlHttpToHttps(videoInfo.thumbnail),
                        downloadResultTemp.filePath.toString()
                    )
                )
                _viewState.update { it.copy(progress = 100f) }
                if (PreferenceUtil.getValue(PreferenceUtil.OPEN_IMMEDIATELY))
                    openFile(downloadResultTemp)
                isDownloading = false
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
                .matcher(PreferenceUtil.getString(PreferenceUtil.TEMPLATE).toString())
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1))
            } else {
                request.addOption(m.group(2))
            }
        }
        TextUtil.makeToast(context.getString(R.string.start_execute))
        _viewState.update { it.copy(isDownloadError = false, progress = 0f) }
        viewModelScope.launch(Dispatchers.IO) {
            val videoInfo: VideoInfo?
            try {
                videoInfo = DownloadUtil.fetchVideoInfo(viewState.value.url)
                with(videoInfo) {
                    if (!title.isNullOrEmpty() and !thumbnail.isNullOrEmpty())
                        _viewState.update {
                            it.copy(
                                videoTitle = title,
                                videoThumbnailUrl = thumbnail,
                                videoAuthor = uploader.toString(),
                                showVideoCard = true
                            )
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                YoutubeDL.getInstance()
                    .execute(request) { progress, _, _ ->
                        _viewState.update { it.copy(progress = progress) }
                    }
                _viewState.update {
                    it.copy(
                        progress = 100f,
                        IsExecutingCommand = false
                    )
                }
                withContext(Dispatchers.Main) {
                    TextUtil.makeToast(R.string.download_success_msg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorMessage(e.message ?: context.getString(R.string.unknown_error))
                return@launch
            }
            isDownloading = false
        }
    }


    private suspend fun showErrorMessage(s: String) {
        withContext(Dispatchers.Main) {
            _viewState.update {
                it.copy(
                    progress = 0f,
                    isDownloadError = true,
                    errorMessage = s,
                    IsExecutingCommand = false
                )
            }
            TextUtil.makeToast(s)
        }
        isDownloading = false
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