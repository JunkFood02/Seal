package com.junkfood.seal.service

object Constants {
    const val ACTION_STOP = "STOP"
    const val NOTIFICATION_ID = 123
    const val CHANNEL_ID = "SealVideoService"
    const val NO_ERROR = -1
    enum class What {
        WHAT_YTDLP_VERSION,
        WHAT_APPEND_TASK,
        WHAT_TASK_HALT,
        WHAT_APPEND_TASK_ASK,
        WHAT_TASK_PROGRESS,
        WHAT_EXIT_REQUEST,
        WHAT_ERROR,
    }
}