package com.junkfood.seal.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class HomeViewModel : ViewModel() {
    private val greeting = BaseApplication.res.getString(R.string.greeting)
    private val _text = MutableLiveData<String>().apply {
        value = greeting
    }
    private val _progress = MutableLiveData<Float>().apply {
        value = 0f
    }
    private val _audioSwitch = MutableLiveData<Boolean>().apply {
        value = true
    }
    private val _thumbnailSwitch = MutableLiveData<Boolean>().apply {
        value = true
    }
    private val _proxySwitch = MutableLiveData<Boolean>().apply {
        value = false
    }
    val text: LiveData<String> = _text
    val progress: LiveData<Float> = _progress
    val audioSwitch: LiveData<Boolean> = _audioSwitch
    val thumbnailSwitch: LiveData<Boolean> = _thumbnailSwitch
    val url: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val proxy: MutableLiveData<String> =
        MutableLiveData<String>().apply { value = "http://127.0.0.1:7890" }
    val proxySwitch:LiveData<Boolean> = _proxySwitch
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    fun updateTime() {
        Thread {
            while (true) {
                _text.postValue(greeting + "\n现在的时间是：" + sdf.format(System.currentTimeMillis()))
                Thread.sleep(1000)
            }
        }.start()
    }

    fun updateProgress(progressNum: Float) {
        with(_progress)
        {
            postValue(progressNum)
        }
    }

    fun audioSwitchChange(b: Boolean) {
        _audioSwitch.value = b
    }
    fun proxySwitchChange(b: Boolean) {
        _proxySwitch.value = b
    }

    fun thumbnailSwitchChange(b: Boolean) {
        _thumbnailSwitch.value = b
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}