package com.junkfood.seal.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R

object NotificationUtil {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @SuppressLint("StaticFieldLeak")
    lateinit var builder: NotificationCompat.Builder
    private const val PROGRESS_MAX = 100
    private const val PROGRESS_INITIAL = 0

    fun createNotificationChannel() {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(BaseApplication.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun makeNotification(notificationId: Int = 1, title: String, text: String) {
        builder = NotificationCompat.Builder(context, BaseApplication.CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(R.drawable.seal)
            priority = NotificationCompat.PRIORITY_LOW
        }
        NotificationManagerCompat.from(context).apply {
            builder.setProgress(PROGRESS_MAX, PROGRESS_INITIAL, false)
            notify(notificationId, builder.build())
        }
    }

    fun updateNotification(notificationId: Int = 1, progress: Int, text: String) {
        builder.setProgress(PROGRESS_MAX, progress, false).setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        notificationManager.notify(notificationId, builder.build())
    }

    fun finishNotification(
        notificationId: Int = 1,
        title: String,
        text: String?,
        intent: PendingIntent?
    ) {
        builder.setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText(text)
            .setProgress(0, 0, false).setAutoCancel(true)
            .setContentIntent(intent)
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, builder.build())
    }
}