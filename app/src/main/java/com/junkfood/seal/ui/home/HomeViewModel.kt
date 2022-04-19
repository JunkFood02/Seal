package com.junkfood.seal.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R

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

    val text: LiveData<String> = _text
    val progress: LiveData<Float> = _progress
    val audioSwitch: LiveData<Boolean> = _audioSwitch
    val thumbnailSwitch: LiveData<Boolean> = _thumbnailSwitch
    val url: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }


    fun updateProgress(progressNum: Float) {
        with(_progress)
        {
            postValue(progressNum)
        }
    }

    fun audioSwitchChange(b: Boolean) {
        _audioSwitch.value = b
    }

    fun thumbnailSwitchChange(b: Boolean) {
        _thumbnailSwitch.value = b
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}