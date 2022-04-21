package com.junkfood.seal.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R

class HomeViewModel : ViewModel() {
    private val greeting = context.getString(R.string.greeting)
    private val _text = MutableLiveData<String>().apply {
//        value = greeting
    }
    private val _progress = MutableLiveData<Float>().apply {
        value = 0f
    }

    val text: LiveData<String> = _text
    val progress: LiveData<Float> = _progress
    val url: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }


    fun updateProgress(progressNum: Float) {
        with(_progress)
        {
            postValue(progressNum)
        }
    }


    companion object {
        private const val TAG = "HomeViewModel"
    }
}