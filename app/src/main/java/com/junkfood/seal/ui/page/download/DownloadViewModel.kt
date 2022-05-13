package com.junkfood.seal.ui.page.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val errorMessage: String = ""
    )

    fun updateUrl(url: String) = _viewState.update { it.copy(url = url) }

    private lateinit var downloadResultTemp: DownloadUtil.Result


    fun startDownloadVideo() {
        if (isDownloading) {
            TextUtil.makeToast(context.getString(R.string.task_running))
            return
        }
        isDownloading = true
        viewModelScope.launch(Dispatchers.IO) {
            with(_viewState) {
                if (value.url.isBlank()) {
                    showErrorMessage(context.getString(R.string.url_empty))
                    return@launch
                }
                update { it.copy(isDownloadError = false) }
                val videoInfo = DownloadUtil.fetchVideoInfo(value.url)
                if (videoInfo == null) {
                    showErrorMessage(context.getString(R.string.fetch_info_error_msg))
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

                downloadResultTemp = DownloadUtil.downloadVideo(value.url, videoInfo)
                { fl: Float, _: Long, _: String -> _viewState.update { it.copy(progress = fl) } }

                if (downloadResultTemp.resultCode == DownloadUtil.ResultCode.EXCEPTION) {
                    showErrorMessage(context.getString(R.string.download_error_msg))
                    return@launch
                }
                DatabaseUtil.insertInfo(
                    DownloadedVideoInfo(
                        0,
                        videoInfo.title.toString(),
                        videoInfo.uploader.toString(),
                        viewState.value.url,
                        videoInfo.thumbnail.toString(),
                        downloadResultTemp.filePath.toString()
                    )
                )
                _viewState.update { it.copy(progress = 100f) }
                if (PreferenceUtil.getValue("open_when_finish"))
                    openFile(downloadResultTemp)
                isDownloading = false
            }
        }
    }

    private suspend fun showErrorMessage(s: String) {
        withContext(Dispatchers.Main) {
            _viewState.update { it.copy(progress = 0f, isDownloadError = true, errorMessage = s) }
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
    }
}