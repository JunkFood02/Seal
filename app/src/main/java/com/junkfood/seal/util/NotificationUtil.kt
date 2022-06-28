package com.junkfood.seal.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil.NOTIFICATION

@SuppressLint("StaticFieldLeak")
object NotificationUtil {
    private val notificationManager = NotificationManagerCompat.from(context)
    private const val PROGRESS_MAX = 100
    private const val PROGRESS_INITIAL = 0
    private const val CHANNEL_ID = "download_notification"
    private const val NOTIFICATION_GROUP_ID = "seal.download.notification"
    private const val NOTIFICATION_ID = 100

    private var builder =
        NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.seal)


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
        notificationManager.createNotificationChannelGroup(channelGroup)
        notificationManager.createNotificationChannel(channel)
    }

    fun makeNotification(
        notificationId: Int = NOTIFICATION_ID,
        title: String,
        text: String? = null
    ) {
        builder.setContentTitle(title)
            .setContentText(text)
            .setProgress(PROGRESS_MAX, PROGRESS_INITIAL, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        if (!PreferenceUtil.getValue(NOTIFICATION)) return
        notificationManager.notify(notificationId, builder.build())
    }

    fun updateNotification(notificationId: Int = NOTIFICATION_ID, progress: Int, text: String) {
        if (!PreferenceUtil.getValue(NOTIFICATION)) return
        builder.setProgress(PROGRESS_MAX, progress, false)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        notificationManager.notify(notificationId, builder.build())
    }

    fun finishNotification(
        notificationId: Int = NOTIFICATION_ID,
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
}