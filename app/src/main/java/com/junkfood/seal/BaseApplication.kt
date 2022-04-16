package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Environment
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import java.io.File


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
        downloadDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            res.getString(R.string.app_name)
        ).absolutePath
        context = applicationContext
    }

    companion object {
        lateinit var res: Resources
        private const val TAG = "BaseApplication"
        lateinit var downloadDir: String
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}