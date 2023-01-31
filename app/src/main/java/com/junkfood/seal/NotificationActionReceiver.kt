package com.junkfood.seal

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.util.Log
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.ToastUtil
import com.yausername.youtubedl_android.YoutubeDL

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CancelReceiver"
        private const val PACKAGE_NAME_PREFIX = "com.junkfood.seal."

        const val ACTION_CANCEL_TASK = 0
        const val ACTION_ERROR_REPORT = 1

        const val ACTION_KEY = PACKAGE_NAME_PREFIX + "action"
        const val TASK_ID_KEY = PACKAGE_NAME_PREFIX + "taskId"

        const val NOTIFICATION_ID_KEY = PACKAGE_NAME_PREFIX + "notificationId"
        const val ERROR_REPORT_KEY = PACKAGE_NAME_PREFIX + "error_report"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, 0)
        val action = intent.getIntExtra(ACTION_KEY, ACTION_CANCEL_TASK)
        Log.d(TAG, "onReceive: $action")
        when (action) {
            ACTION_CANCEL_TASK -> {
                val taskId = intent.getStringExtra(TASK_ID_KEY)
                cancelTask(taskId, notificationId)
            }

            ACTION_ERROR_REPORT -> {
                val errorReport = intent.getStringExtra(ERROR_REPORT_KEY)
                if (!errorReport.isNullOrEmpty())
                    copyErrorReport(errorReport, notificationId)
            }
        }
    }

    private fun cancelTask(taskId: String?, notificationId: Int) {
        if (taskId.isNullOrEmpty()) return
        NotificationUtil.cancelNotification(notificationId)
        val result = YoutubeDL.getInstance().destroyProcessById(taskId)
        NotificationUtil.cancelNotification(notificationId)
        if (result) {
            Log.d(TAG, "Task (id:$taskId) was killed.")
            Downloader.onProcessCanceled(taskId)

        }
    }

    private fun copyErrorReport(error: String, notificationId: Int) {
        App.clipboard.setPrimaryClip(
            ClipData.newPlainText(null, error)
        )
        context.let { ToastUtil.makeToastSuspend(it.getString(R.string.error_copied)) }
        NotificationUtil.cancelNotification(notificationId)
    }

}