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
    private val scheduler = Executors.newScheduledThreadPool(5);
    private val greeting=BaseApplication.res.getString(R.string.greeting)
    private val _text = MutableLiveData<String>().apply {
        value = greeting
    }
    val text: LiveData<String> = _text
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
    fun updateTime() {
        Thread {
            while (true) {
                _text.postValue(greeting + "\n现在的时间是：" + sdf.format(System.currentTimeMillis()))
                Thread.sleep(1000)
            }
        }.start()
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}