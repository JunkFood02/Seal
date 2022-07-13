package com.junkfood.seal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(PreferenceUtil.getLanguageConfiguration()))
        }
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        BaseApplication.context = this.baseContext
        setContent {
            HomeEntry(downloadViewModel)
        }
        handleShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        intent?.let { handleShareIntent(it) }
        super.onNewIntent(intent)
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
        private var bound = false
        private val connection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as DownloadService.DownloadServiceBinder
                bound = true
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
            }
        }

        fun startService() {
            if (bound) return
            Intent(context, DownloadService::class.java).also { intent ->
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        fun stopService() {
            context.unbindService(connection)
            bound = false
        }

        fun setLanguage(locale: String) {
            if (locale.isEmpty()) return
            BaseApplication.applicationScope.launch(Dispatchers.Main) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
            }
        }

    }

}





