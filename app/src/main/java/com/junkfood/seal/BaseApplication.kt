package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.material.color.DynamicColors
import com.yausername.ffmpeg.FFmpeg
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
            FFmpeg.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "failed to initialize youtubedl-android", e)
        }
        Thread {
            Looper.prepare()
            try {
                YoutubeDL.getInstance().updateYoutubeDL(this)
                Toast.makeText(context, res.getString(R.string.yt_dlp_up_to_date), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    res.getString(R.string.yt_dlp_update_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.start()

        with(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                res.getString(R.string.app_name)
            )
        ) {
            downloadDir = if (Build.VERSION.SDK_INT > 29 || canWrite()) absolutePath
            else "Unset"
        }
        context = applicationContext
    }


    companion object {
        lateinit var res: Resources
        private const val TAG = "BaseApplication"
        lateinit var downloadDir: String
        fun updateDownloadDir() {
            downloadDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                res.getString(R.string.app_name)
            ).absolutePath
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}