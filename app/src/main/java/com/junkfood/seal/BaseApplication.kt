package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.material.color.DynamicColors
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
                Toast.makeText(
                    context,
                    context.getString(R.string.yt_dlp_up_to_date),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.yt_dlp_update_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.start()

        with(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                getString(R.string.app_name)
            )
        ) {
            downloadDir = if (Build.VERSION.SDK_INT > 29 || canWrite()) absolutePath
            else "Unset"
        }
        context = applicationContext
    }


    companion object {
        private const val TAG = "BaseApplication"
        lateinit var clipboard: ClipboardManager
        lateinit var downloadDir: String
        fun updateDownloadDir() {
            downloadDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                context.getString(R.string.app_name)
            ).absolutePath
        }

        fun createLogFileOnDevice(th: Throwable) {
            with(context.getExternalFilesDir(null)) {
                if (this?.canWrite() == true) {
                    val timeNow: String =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        } else {
                            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
                        }
                    with(File(this, "log$timeNow.txt")) {
                        if (!exists())
                            createNewFile()
                        val logWriter = FileWriter(this, true)
                        val out = BufferedWriter(logWriter)
                        out.append(th.stackTraceToString())
                        out.close()
                    }
                }
            }
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}