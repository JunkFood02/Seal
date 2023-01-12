package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.util.AUDIO_DIRECTORY
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil.createEmptyFile
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.TEMPLATE_EXAMPLE
import com.junkfood.seal.util.TEMPLATE_ID
import com.junkfood.seal.util.VIDEO_DIRECTORY
import com.tencent.mmkv.MMKV
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
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

        clipboard = getSystemService()!!
        connectivityManager = getSystemService()!!

        applicationScope.launch((Dispatchers.IO)) {
            try {
                if (!PreferenceUtil.containsKey(TEMPLATE_ID)) {
                    PreferenceUtil.encodeInt(
                        TEMPLATE_ID, DatabaseUtil.insertTemplate(
                            CommandTemplate(
                                id = 0,
                                name = context.getString(R.string.custom_command_template),
                                template = TEMPLATE_EXAMPLE
                            )
                        ).toInt()
                    )
                }
                YoutubeDL.init(this@App)
                FFmpeg.init(this@App)
                Aria2c.init(this@App)
            } catch (e: Exception) {
                e.printStackTrace()
                clipboard.setPrimaryClip(ClipData.newPlainText(null, e.message))
                Toast.makeText(this@App, e.message, Toast.LENGTH_LONG).show()
            }
        }

        videoDownloadDir = VIDEO_DIRECTORY.getString(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                getString(R.string.app_name)
            ).absolutePath
        )

        audioDownloadDir = AUDIO_DIRECTORY.getString(File(videoDownloadDir, "Audio").absolutePath)

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

        var isServiceRunning = false

        private val connection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as DownloadService.DownloadServiceBinder
                isServiceRunning = true
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
            }
        }

        fun startService() {
            if (isServiceRunning) return
            Intent(context.applicationContext, DownloadService::class.java).also { intent ->
                context.applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        fun stopService() {
            if (!isServiceRunning) return
            try {
                isServiceRunning = false
                context.applicationContext.run {
                    unbindService(connection)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


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
                PreferenceUtil.encodeString(AUDIO_DIRECTORY, path)
            } else {
                videoDownloadDir = path
                PreferenceUtil.encodeString(VIDEO_DIRECTORY, path)
            }
        }

        fun getVersionReport(): String {
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            val release = if (Build.VERSION.SDK_INT >= 30) {
                Build.VERSION.RELEASE_OR_CODENAME
            } else {
                Build.VERSION.RELEASE
            }
            return StringBuilder().append("App version: $versionName ($versionCode)\n")
                .append("Device information: Android $release (API ${Build.VERSION.SDK_INT})\n")
                .append("Supported ABIs: ${Build.SUPPORTED_ABIS.contentToString()}\n")
                .append("Yt-dlp version: ${YoutubeDL.version(context.applicationContext)}\n")
                .toString()
        }

        fun isFDroidBuild(): Boolean = packageInfo.versionName.contains("F-Droid")

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}