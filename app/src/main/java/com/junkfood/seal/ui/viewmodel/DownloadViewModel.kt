package com.junkfood.seal.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadViewModel : ViewModel() {

    private val _progress = MutableLiveData<Float>().apply {
        value = 0f
    }
    val isDownloading: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val progress: LiveData<Float> = _progress
    val url: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val videoTitle: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val videoThumbnailUrl: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val videoAuthor: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val downloadError: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val errorMessage: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }

    private lateinit var downloadResultTemp: DownloadUtil.Result
    fun startDownloadVideo() {
        viewModelScope.launch(Dispatchers.IO) {
            with(url.value) {
                if (isNullOrBlank()) {
                    showErrorMessage(context.getString(R.string.url_empty))
                } else {
                    downloadError.postValue(false)
                    val videoInfo: VideoInfo? =
                        DownloadUtil.fetchVideoInfo(this@with)
                    if (videoInfo == null) {
                        showErrorMessage(context.getString(R.string.fetch_info_error_msg))
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        _progress.value = 0f
                        isDownloading.value = true
                        videoTitle.value = videoInfo.title
                        videoAuthor.value = videoInfo.uploader
                        with(videoInfo.thumbnail)
                        {
                            if (matches(Regex("^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?\$"))) {
                                videoThumbnailUrl.value = replace("http", "https")
                            } else videoThumbnailUrl.value = this.toString()
                        }
                    }
                    downloadResultTemp = DownloadUtil.downloadVideo(this@with, videoInfo)
                    { fl: Float, _: Long, _: String -> _progress.postValue(fl) }
                    //isDownloading.postValue(false)
                    if (downloadResultTemp.resultCode == DownloadUtil.ResultCode.EXCEPTION) {
                        showErrorMessage(context.getString(R.string.download_error_msg))
                    } else {
                        DatabaseUtil.insertInfo(
                            DownloadedVideoInfo(
                                0,
                                videoTitle.value.toString(),
                                videoAuthor.value.toString(),
                                url.value.toString(),
                                videoThumbnailUrl.value.toString(),
                                downloadResultTemp.filePath.toString()
                            )
                        )
                        withContext(Dispatchers.Main) {
                            _progress.value = 100f
                            if (PreferenceUtil.getValue("open_when_finish")) openFile(
                                downloadResultTemp
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun showErrorMessage(s: String) {
        withContext(Dispatchers.Main) {
            _progress.value = 0f
            downloadError.value = true
            errorMessage.value = s
            TextUtil.makeToast(s)
        }
    }

    fun openVideoFile() {
        if (progress.value == 100f)
            openFile(downloadResultTemp)
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}