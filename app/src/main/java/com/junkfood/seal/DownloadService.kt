package com.junkfood.seal

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.junkfood.seal.util.DebugLogger
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.NotificationUtil.SERVICE_NOTIFICATION_ID

private const val TAG = "DownloadService"

class DownloadService : Service() {

    override fun onCreate() {
        super.onCreate()
        DebugLogger.log(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugLogger.log(TAG, "onStartCommand: startId=$startId")
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        val notification = NotificationUtil.makeServiceNotification(pendingIntent)
        startForeground(SERVICE_NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        DebugLogger.log(TAG, "onBind")
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        val notification = NotificationUtil.makeServiceNotification(pendingIntent)
        startForeground(SERVICE_NOTIFICATION_ID, notification)
        return DownloadServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        DebugLogger.log(TAG, "onUnbind")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        DebugLogger.log(TAG, "onDestroy")
        super.onDestroy()
    }

    inner class DownloadServiceBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }
}
