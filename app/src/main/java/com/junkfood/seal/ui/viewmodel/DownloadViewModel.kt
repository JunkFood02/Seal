package com.junkfood.seal.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadViewModel : ViewModel() {
    private val greeting = context.getString(R.string.greeting)
    private val _text = MutableLiveData<String>().apply {
        value = greeting
    }
    private val _progress = MutableLiveData<Float>().apply {
        value = 0f
    }
    val isDownloading: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val text: LiveData<String> = _text
    val progress: LiveData<Float> = _progress
    val url: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }


    fun updateProgress(progressNum: Float) {
        with(_progress)
        {
            postValue(progressNum)
        }
    }

    fun startDownloadVideo() {
        _progress.value = 0f
        isDownloading.value = true
        with(url.value) {
            if (isNullOrBlank()) TextUtil.makeToast(context.getString(R.string.url_empty))
            else {
                viewModelScope.launch(Dispatchers.IO) {
                    val downloadResult = DownloadUtil.downloadVideo(
                        this@with
                    ) { fl: Float, _: Long, _: String -> updateProgress(fl) }
                    isDownloading.postValue(false)
                    if (downloadResult.resultCode != DownloadUtil.ResultCode.EXCEPTION)
                        withContext(Dispatchers.Main) {
                            updateProgress(100f)
                            if (PreferenceUtil.getValue("open_when_finish")
                            ) openFile(
                                context,
                                downloadResult
                            )
                        }
                }
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}