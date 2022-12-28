package com.junkfood.seal

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalDynamicColorSwitch
import com.junkfood.seal.ui.common.LocalSeedColor
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


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
            if(Build.VERSION.SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    val getFullAcessPermission = Intent()
                    getFullAcessPermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivity(getFullAcessPermission)
                }
            }
        }
        context = this.baseContext
        requestExternalStoragePermission(this)
        setContent {
            val isUrlSharingTriggered =
                downloadViewModel.viewStateFlow.collectAsState().value.isUrlSharingTriggered
            val windowSizeClass = calculateWindowSizeClass(this)
            SettingsProvider(windowSizeClass.widthSizeClass) {
                SealTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    seedColor = LocalSeedColor.current,
                    isDynamicColorEnabled = LocalDynamicColorSwitch.current,
                ) {
                    HomeEntry(
                        downloadViewModel,
                        isUrlSharingTriggered
                    )
                }
            }
        }
        handleShareIntent(intent)
    }

    fun requestExternalStoragePermission(activity: Activity) {
        // Check if the app has the necessary permissions
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permissions if they are not granted
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE
            )
        } else {
            Log.d(TAG, "requestExternalStoragePermission: Some of the permissions weren't granted")
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleShareIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleShareIntent(intent: Intent) {
        Log.d(TAG, "handleShareIntent: $intent")

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString?.let {
                    sharedUrl = it
                    downloadViewModel.updateUrl(sharedUrl, true)
                }
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?.let { sharedContent ->
                        intent.removeExtra(Intent.EXTRA_TEXT)
                        TextUtil.matchUrlFromSharedText(sharedContent)
                            .let { matchedUrl ->
                                if (sharedUrl != matchedUrl) {
                                    sharedUrl = matchedUrl
                                    downloadViewModel.updateUrl(sharedUrl, true)
                                }
                            }
                    }
            }
        }

    }

    companion object {
        private const val TAG = "MainActivity"
        private var sharedUrl = ""
        private val REQUEST_CODE = 1
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

        fun setLanguage(locale: String) {
            Log.d(TAG, "setLanguage: $locale")
            val localeListCompat =
                if (locale.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(locale)
            App.applicationScope.launch(Dispatchers.Main) {
                AppCompatDelegate.setApplicationLocales(localeListCompat)
            }
        }

    }

}





