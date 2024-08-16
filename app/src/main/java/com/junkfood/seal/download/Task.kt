package com.junkfood.seal.download

import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.VideoInfo
import kotlinx.coroutines.Job

data class Task(
    val url: String,
    val state: State = State.Idle,
    val info: VideoInfo? = null,
    private val preferences: DownloadUtil.DownloadPreferences,
) {
    sealed interface State {

        data object Idle : State

        data class FetchingInfo(val job: Job, val taskId: String) : State

        data object ReadyWithInfo : State

        data class Running(val job: Job, val taskId: String, val progress: Float) : State

        data object Canceled : State

        data class Error(val throwable: Throwable) : State

        data class Completed(val filePath: String) : State
    }
}
