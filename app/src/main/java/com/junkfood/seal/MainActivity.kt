package com.junkfood.seal

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalSeedColor
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Method

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        runBlocking {
            if (Build.VERSION.SDK_INT < 33)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(PreferenceUtil.getLanguageConfiguration())
                )
        }
        context = this.baseContext
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            SettingsProvider(windowSizeClass.widthSizeClass) {
                val darkTheme = LocalDarkTheme.current.isDarkTheme()
                SealTheme(
                    darkTheme = darkTheme,
                    seedColor = LocalSeedColor.current
                ) {
                    HomeEntry(downloadViewModel)
                    checkIfMiui()
                }
            }
        }
        handleShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        intent?.let { handleShareIntent(it) }
        super.onNewIntent(intent)
    }

    @SuppressLint("PrivateApi")
    private fun checkIfMiui() {
        //Check if it's MIUI to apply a mini-patch to the Xiaomi problem https://stackoverflow.com/questions/47610456/how-to-detect-miui-rom-programmatically-in-android
        val c = Class.forName("android.os.SystemProperties")
        val get: Method = c.getMethod("get", String::class.java)
        val miui = get.invoke(c, "ro.miui.ui.version.code") as String

        Log.d("Message", "Miui version: $miui")

        // if string miui is not empty, execute the code
        if(miui.isNotEmpty()){
            lifecycleScope.launch {
                delay(50)
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }
    private fun handleShareIntent(intent: Intent) {
        Log.d(TAG, "handleShareIntent: $intent")
        if (Intent.ACTION_SEND == intent.action)
            intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.let { it ->
                    TextUtil.matchUrlFromSharedText(it)
                        ?.let { it1 ->
                            if (sharedUrl != it1) {
                                sharedUrl = it1
                                downloadViewModel.updateUrl(sharedUrl)
                            }
                        }
                }
    }

    companion object {
        private const val TAG = "MainActivity"
        private var sharedUrl = ""
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
            Intent(context, DownloadService::class.java).also { intent ->
                context.applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        fun stopService() {
            if (!isServiceRunning) return
            context.applicationContext.unbindService(connection)
            isServiceRunning = false
        }

        fun setLanguage(locale: String) {
            Log.d(TAG, "setLanguage: $locale")
            val localeListCompat =
                if (locale.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(locale)
            BaseApplication.applicationScope.launch(Dispatchers.Main) {
                AppCompatDelegate.setApplicationLocales(localeListCompat)
            }
        }

    }

}





