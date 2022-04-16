package com.junkfood.seal

import android.app.Application
import android.content.res.Resources
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        res = resources
        DynamicColors.applyToActivitiesIfAvailable(this)
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "failed to initialize youtubedl-android", e)
        }
    }

    companion object {
        lateinit var res: Resources
        private const val TAG = "BaseApplication"
    }
}