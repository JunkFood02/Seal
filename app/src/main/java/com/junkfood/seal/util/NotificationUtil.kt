package com.junkfood.seal.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil.NOTIFICATION

@SuppressLint("StaticFieldLeak")
object NotificationUtil {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private const val PROGRESS_MAX = 100
    private const val PROGRESS_INITIAL = 0
    private const val CHANNEL_ID = "download_notification"
    private const val SERVICE_CHANNEL_ID = "download_service"
    private const val NOTIFICATION_GROUP_ID = "seal.download.notification"
    private const val DEFAULT_NOTIFICATION_ID = 100
    const val SERVICE_NOTIFICATION_ID = 123
    private lateinit var serviceNotification: Notification

    private var builder =
        NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_stat_seal)


    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channelGroup =
            NotificationChannelGroup(NOTIFICATION_GROUP_ID, context.getString(R.string.download))
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            group = NOTIFICATION_GROUP_ID
        }
        val serviceChannel = NotificationChannel(SERVICE_CHANNEL_ID, name, importance).apply {
            description = context.getString(R.string.service_title)
            group = NOTIFICATION_GROUP_ID
        }
        notificationManager.createNotificationChannelGroup(channelGroup)
        notificationManager.createNotificationChannel(channel)
        notificationManager.createNotificationChannel(serviceChannel)
    }

    fun makeNotification(
        notificationId: Int = DEFAULT_NOTIFICATION_ID,
        title: String,
        text: String? = null
    ) {
        builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_seal)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(PROGRESS_MAX, PROGRESS_INITIAL, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        if (!PreferenceUtil.getValue(NOTIFICATION)) return
        notificationManager.notify(notificationId, builder.build())
    }

    fun updateNotification(
        notificationId: Int = DEFAULT_NOTIFICATION_ID,
        progress: Int,
        text: String
    ) {
        if (!PreferenceUtil.getValue(NOTIFICATION)) return
        builder.setProgress(PROGRESS_MAX, progress, progress == -1)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        notificationManager.notify(notificationId, builder.build())
    }

    fun finishNotification(
        notificationId: Int = DEFAULT_NOTIFICATION_ID,
        title: String? = null,
        text: String? = null,
        intent: PendingIntent? = null
    ) {
        if (!PreferenceUtil.getValue(NOTIFICATION)) return
        title?.let { builder.setContentTitle(title) }
        builder
            .setContentText(text)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .setOngoing(false)
            .setStyle(null)
        intent?.let { builder.setContentIntent(it) }
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, builder.build())
    }

    fun makeServiceNotification(intent: PendingIntent): Notification {
        serviceNotification = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_seal)
            .setContentTitle(context.getString(R.string.service_title))
            .setOngoing(true)
            .setContentIntent(intent)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        return serviceNotification
    }

    fun updateServiceNotification(index: Int, itemCount: Int) {
        serviceNotification = NotificationCompat.Builder(context, serviceNotification)
            .setContentTitle(context.getString(R.string.service_title) + " ($index/$itemCount)")
            .build()
        notificationManager.notify(SERVICE_NOTIFICATION_ID, serviceNotification)
    }

    fun cancelNotification(notificationId: Int) {
        /*builder
            .setContentText(context.getText(R.string.task_cancelled))
            .setOngoing(false)
            .setAutoCancel(true)
            .setProgress(0, 0, false)
        notificationManager.notify(notificationId, builder.build())*/
        notificationManager.cancel(notificationId)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}