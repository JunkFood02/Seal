package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DOWNLOAD_DIRECTORY
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


        with(PreferenceUtil.getString(DOWNLOAD_DIRECTORY)) {
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
        lateinit var applicationScope: CoroutineScope

        fun updateDownloadDir(path: String) {
            downloadDir = path
            PreferenceUtil.updateString(DOWNLOAD_DIRECTORY, path)
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}