package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil
import com.tencent.mmkv.MMKV
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.*
import java.io.File


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        CoroutineScope(Job()).launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "onCreate: Init")
                    YoutubeDL.getInstance().init(this@BaseApplication)
                    FFmpeg.getInstance().init(this@BaseApplication)
                    if (PreferenceUtil.getString("yt-dlp_init").isNullOrEmpty()) {
                        DownloadUtil.updateYtDlp()
                    }
                    Log.d(TAG, "onCreate: Init Finish")
                } catch (e: YoutubeDLException) {
                    Log.e(TAG, "failed to initialize youtubedl-android", e)
                }
            }
        }

        ytdlpVersion =
            YoutubeDL.getInstance().version(this) ?: resources.getString(R.string.ytdlp_update)


        with(PreferenceUtil.getString("download_dir")) {
            downloadDir = if (isNullOrEmpty())
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                    getString(R.string.app_name)
                ).absolutePath
            else this

        }

        context = applicationContext
    }


    companion object {
        private const val TAG = "BaseApplication"
        lateinit var clipboard: ClipboardManager
        lateinit var downloadDir: String
        var ytdlpVersion = ""


        fun updateDownloadDir(path: String) {
            downloadDir = path
            PreferenceUtil.updateString("download_dir", path)
        }


        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}