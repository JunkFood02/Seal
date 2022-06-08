package com.junkfood.seal.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R

@SuppressLint("StaticFieldLeak")
object NotificationUtil {
    private val notificationManager = NotificationManagerCompat.from(context)
    private const val PROGRESS_MAX = 100
    private const val PROGRESS_INITIAL = 0
    private const val CHANNEL_ID = "download_notification"
    private const val NOTIFICATION_ID = 100

    private val builder = NotificationCompat.Builder(context, CHANNEL_ID)

    fun createNotificationChannel() {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun makeNotification(notificationId: Int = NOTIFICATION_ID, title: String, text: String) {
        builder.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.seal)
            .setProgress(PROGRESS_MAX, PROGRESS_INITIAL, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        notificationManager.notify(notificationId, builder.build())
    }

    fun updateNotification(notificationId: Int = NOTIFICATION_ID, progress: Int, text: String) {
        builder.setProgress(PROGRESS_MAX, progress, false)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        notificationManager.notify(notificationId, builder.build())
    }

    fun finishNotification(
        notificationId: Int = NOTIFICATION_ID, title: String, text: String?, intent: PendingIntent?
    ) {
        builder.setContentTitle(title)
            .setContentText(text)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(intent)
            .setStyle(null)
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, builder.build())
    }
}