package com.junkfood.seal

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import com.junkfood.seal.service.VideoDownloadService
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.TextUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val downloadViewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(PreferenceUtil.getLanguageConfiguration()))
        }
        super.onCreate(savedInstanceState)
        instance = this
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

    override fun onStart() {
        super.onStart()
        bindServiceInvoked()
    }

    override fun onStop() {
        mService = null
        baseContext.unbindService(serviceConnection)
        super.onStop()
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
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
        var mService: Messenger? = null
        var messageToSend: Message? = null
        private var instance: MainActivity? = null
        private var isBinding = false

        fun sendMessage(m: Message) {
            if (mService == null) {
                messageToSend = m
                bindServiceInvoked()
            }
            else
                mService!!.send(m)
        }

        //Receive service connection and disconnection messages
        private val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Log.i(TAG, "service connected")
                isBinding = false
                mService = Messenger(service)

                //Since the server does not have the client's messenger after binding, send the client's messenger to the server after binding
                if (mService != null) {
                    try {
                        if (messageToSend != null) {
                            sendMessage(messageToSend!!)
                            messageToSend = null
                        }
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.i("client", "service disconnected")
                mService = null
                isBinding = false
            }

            override fun onBindingDied(name: ComponentName?) {
                mService = null
                isBinding = false
            }
        }

        private fun bindServiceInvoked() {
            if (!isBinding && instance != null && instance!!.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                isBinding = true
                instance!!.baseContext.bindService(Intent(instance!!.baseContext, VideoDownloadService::class.java), serviceConnection, BIND_AUTO_CREATE)
            }
        }
        fun setLanguage(locale: String) {
            if (locale.isEmpty()) return
            BaseApplication.applicationScope.launch(Dispatchers.Main) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
            }
        }
        fun exit(code: Int = 0) {
            BaseApplication.applicationScope.launch(Dispatchers.Main) {
                if (instance != null)
                    instance!!.finish()
                exitProcess(code)
            }
        }
    }

}





