package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.*
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
        MMKV.initialize(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        CoroutineScope(Job()).launch {
            withContext(Dispatchers.IO) {
                try {
                    YoutubeDL.getInstance().init(this@BaseApplication)
                    FFmpeg.getInstance().init(this@BaseApplication)
                } catch (e: YoutubeDLException) {
                    Log.e(TAG, "failed to initialize youtubedl-android", e)
                }
            }
        }

        ytdlpVersion =
            YoutubeDL.getInstance().version(this) ?: resources.getString(R.string.ytdlp_update)

        with(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                getString(R.string.app_name)
            )
        ) {
            downloadDir = if (Build.VERSION.SDK_INT > 29 || canWrite()) absolutePath
            else "Not set"
        }
        context = applicationContext
    }


    companion object {
        private const val TAG = "BaseApplication"
        lateinit var clipboard: ClipboardManager
        lateinit var downloadDir: String
        var ytdlpVersion = ""

        fun updateDownloadDir() {
            downloadDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                context.getString(R.string.app_name)
            ).absolutePath
        }

        fun updateDownloadDir(path: String) {
            downloadDir = path
        }

        fun createLogFileOnDevice(th: Throwable) {
            CoroutineScope(Job()).launch {
                withContext(Dispatchers.IO) {
                    with(context.getExternalFilesDir(null)) {
                        if (this?.canWrite() == true) {
                            val timeNow: String =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                } else {
                                    SimpleDateFormat(
                                        "yyyyMMdd_HHmmss",
                                        Locale.ENGLISH
                                    ).format(Date())
                                }
                            with(File(this, "log$timeNow.txt")) {
                                if (!exists()) createNewFile()
                                val logWriter = FileWriter(this, true)
                                val out = BufferedWriter(logWriter)
                                out.append(th.stackTraceToString())
                                out.close()
                            }
                        }
                    }
                }
            }
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}