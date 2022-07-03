package com.junkfood.seal

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.os.*
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.service.Constants
import com.junkfood.seal.service.VideoDownloadService
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.AUDIO_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.VIDEO_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.YT_DLP
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File


@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        context = applicationContext
        applicationScope = CoroutineScope(SupervisorJob())
        DynamicColors.applyToActivitiesIfAvailable(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


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
        bindServiceInvoked()
        if (Build.VERSION.SDK_INT >= 26)
            NotificationUtil.createNotificationChannel()
    }

    //Receive messages from the server
    class ClientHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.What.WHAT_YTDLP_VERSION.ordinal -> {
                    Log.i(TAG, "ver ok")
                    val b = msg.peekData()
                    if (msg.arg1 != Constants.What.WHAT_ERROR.ordinal && b != null) {
                        val s = b.getString(Constants.What.WHAT_YTDLP_VERSION.name)!!
                        PreferenceUtil.updateString(YT_DLP, s)
                        ytdlpVersionM.update { s }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BaseApplication"
        private val mMessenger = Messenger(ClientHandler())
        var mService: Messenger? = null
        var messageToSend: Message? = null
        private val ytdlpVersionM = MutableStateFlow("")
        val ytdlpVersion = ytdlpVersionM.asStateFlow()
        lateinit var clipboard: ClipboardManager
        lateinit var videoDownloadDir: String
        lateinit var audioDownloadDir: String
        lateinit var applicationScope: CoroutineScope
        private var isBinding = false
        private val intentToService by lazy {
            Intent(context, VideoDownloadService::class.java)
        }
        fun updateytDlp(force: Boolean = false) {
            val s = PreferenceUtil.getString(YT_DLP)
            if (s.isNullOrEmpty() || force) {
                val b = Bundle()
                b.putString(Constants.What.WHAT_YTDLP_VERSION.name, "")
                val m = Message.obtain(null, Constants.What.WHAT_YTDLP_VERSION.ordinal)
                m.replyTo = mMessenger
                m.data = b
                sendMessage(m)
            }
            else
                ytdlpVersionM.update { s }
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

        fun sendMessage(m: Message) {
            if (mService == null) {
                messageToSend = m
                bindServiceInvoked()
            }
            else
                mService!!.send(m)
        }

        //Receive service connection and disconnection messages
        var serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Log.i(TAG, "service connected")
                isBinding = false
                mService = Messenger(service)

                //Since the server does not have the client's messenger after binding, send the client's messenger to the server after binding
                if (mService != null) {
                    try {
                        Log.d(TAG, "send messenger")
                        updateytDlp()
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
            if (!isBinding) {
                isBinding = true
                context.bindService(intentToService, serviceConnection, BIND_AUTO_CREATE)
            }
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}