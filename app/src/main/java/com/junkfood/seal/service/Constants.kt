package com.junkfood.seal.service

object Constants {
    const val ACTION_NOTIFICATION_NAME = "ACTION_NOTIFICATION_NAME"
    const val ACTION_START = "START"
    const val ACTION_STOP = "STOP"
    const val ACTION_NOTIFICATION_KEY = "ACTION_NOTIFICATION_KEY"
    const val NOTIFICATION_ID = 123
    const val CHANNEL_ID = "SealVideoService"
    const val NO_ERROR = -1
    enum class What {
        WHAT_YTDLP_VERSION,
        WHAT_APPEND_TASK,
        WHAT_TASK_HALT,
        WHAT_APPEND_TASK_ASK,
        WHAT_TASK_PROGRESS,
        WHAT_ERROR,
    }
}