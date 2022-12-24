package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil.createEmptyFile
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.AUDIO_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.COOKIES_PROFILE_ID
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE_EXAMPLE
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE_INDEX
import com.junkfood.seal.util.PreferenceUtil.VIDEO_DIRECTORY
import com.tencent.mmkv.MMKV
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        context = applicationContext
        packageInfo = packageManager.run {
            if (Build.VERSION.SDK_INT >= 33) getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0)
            ) else
                getPackageInfo(packageName, 0)
        }
        applicationScope = CoroutineScope(SupervisorJob())
        DynamicColors.applyToActivitiesIfAvailable(this)

        clipboard = getSystemService(ClipboardManager::class.java)
        connectivityManager = getSystemService(ConnectivityManager::class.java)

        applicationScope.launch((Dispatchers.IO)) {
            if (!PreferenceUtil.containsKey(TEMPLATE_INDEX)) {
                PreferenceUtil.updateInt(TEMPLATE_INDEX, 0)
                DatabaseUtil.insertTemplate(
                    CommandTemplate(
                        id = 0,
                        name = context.getString(R.string.custom_command_template),
                        template = TEMPLATE_EXAMPLE
                    )
                )
            }
            PreferenceUtil.selectCookieProfile(PreferenceUtil.getInt(COOKIES_PROFILE_ID, 0))
            try {
                YoutubeDL.getInstance().init(this@App)
                FFmpeg.getInstance().init(this@App)
                Aria2c.getInstance().init(this@App)
            } catch (e: YoutubeDLException) {
                e.printStackTrace()
                Toast.makeText(this@App, e.message, Toast.LENGTH_LONG).show()
            }
        }


        with(PreferenceUtil.getString(VIDEO_DIRECTORY)) {
            videoDownloadDir = if (isNullOrEmpty()) File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                getString(R.string.app_name)
            ).absolutePath
            else this
        }

        with(PreferenceUtil.getString(AUDIO_DIRECTORY)) {
            audioDownloadDir = if (isNullOrEmpty()) File(videoDownloadDir, "Audio").absolutePath
            else this
        }
        if (Build.VERSION.SDK_INT >= 26) NotificationUtil.createNotificationChannel()
    }


    companion object {
        private const val PRIVATE_DIRECTORY_SUFFIX = ".Seal"
        lateinit var clipboard: ClipboardManager
        lateinit var videoDownloadDir: String
        lateinit var audioDownloadDir: String
        lateinit var applicationScope: CoroutineScope
        lateinit var connectivityManager: ConnectivityManager
        lateinit var packageInfo: PackageInfo
        const val userAgentHeader =
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36 Edg/105.0.1343.53"

        fun getPrivateDownloadDirectory(): String =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).resolve(
                PRIVATE_DIRECTORY_SUFFIX
            ).run {
                createEmptyFile(".nomedia")
                absolutePath
            }


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