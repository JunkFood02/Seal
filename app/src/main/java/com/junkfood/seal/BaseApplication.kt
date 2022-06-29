package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Environment
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.AUDIO_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.VIDEO_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.YT_DLP
import com.tencent.mmkv.MMKV
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import java.io.File

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        applicationScope = CoroutineScope(SupervisorJob())
        DynamicColors.applyToActivitiesIfAvailable(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        applicationScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    YoutubeDL.getInstance().init(this@BaseApplication)
                    FFmpeg.getInstance().init(this@BaseApplication)
                    if (PreferenceUtil.getString(YT_DLP).isNullOrEmpty()) {
                        DownloadUtil.updateYtDlp()
                    }
                } catch (e: YoutubeDLException) {
                    e.printStackTrace()
                }
            }
        }
        ytdlpVersion =
            YoutubeDL.getInstance().version(this) ?: resources.getString(R.string.ytdlp_update)


        with(PreferenceUtil.getString(VIDEO_DIRECTORY)) {
            videoDownloadDir = if (isNullOrEmpty())
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                    getString(R.string.app_name)
                ).absolutePath
            else this
        }

        with(PreferenceUtil.getString(AUDIO_DIRECTORY)) {
            audioDownloadDir = if (isNullOrEmpty()) File(videoDownloadDir, "Audio").absolutePath
            else this
        }
        context = applicationContext
        if (Build.VERSION.SDK_INT >= 26)
            NotificationUtil.createNotificationChannel()
    }


    companion object {
        private const val TAG = "BaseApplication"
        lateinit var clipboard: ClipboardManager
        lateinit var videoDownloadDir: String
        lateinit var audioDownloadDir: String
        var ytdlpVersion = ""
        lateinit var applicationScope: CoroutineScope
        fun updateDownloadDir(path: String, isAudio: Boolean = false) {
            if (isAudio) {
                audioDownloadDir = path
                PreferenceUtil.updateString(AUDIO_DIRECTORY, path)
            } else {
                videoDownloadDir = path
                PreferenceUtil.updateString(VIDEO_DIRECTORY, path)
            }
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}