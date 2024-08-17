package com.junkfood.seal.download

import androidx.compose.runtime.mutableStateMapOf
import com.junkfood.seal.download.DownloaderV2.state
import com.junkfood.seal.download.Task.RestartableAction.Download
import com.junkfood.seal.download.Task.RestartableAction.FetchInfo
import com.junkfood.seal.download.Task.State
import com.junkfood.seal.download.Task.State.Completed
import com.junkfood.seal.download.Task.State.Error
import com.junkfood.seal.download.Task.State.FetchingInfo
import com.junkfood.seal.download.Task.State.Idle
import com.junkfood.seal.download.Task.State.ReadyWithInfo
import com.junkfood.seal.download.Task.State.Running
import com.junkfood.seal.util.DownloadUtil
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object DownloaderV2 {
    private val scope = CoroutineScope(SupervisorJob())

    init {
        scope.launch(Dispatchers.Default) { runningTaskFlow.collect {} }
    }

    private val mRunningTaskFlow = MutableStateFlow(0)
    val runningTaskFlow = mRunningTaskFlow.asStateFlow()

    val taskStateMap = mutableStateMapOf<Task, State>()

    var Task.state: State
        get() = taskStateMap[this]!!
        set(value) {
            taskStateMap[this] = value
        }

    fun Task.fetchInfo() {
        val task = this
        check(state == Idle)
        scope
            .launch {
                DownloadUtil.fetchVideoInfoFromUrl(
                        url = url, preferences = preferences, taskKey = id)
                    .onSuccess { info ->
                        taskStateMap.remove(task)
                        taskStateMap += task.copy(info = info) to ReadyWithInfo
                    }
                    .onFailure { throwable ->
                        task.state = Error(throwable = throwable, action = FetchInfo)
                    }
            }
            .also { job ->
                job.invokeOnCompletion { mRunningTaskFlow.update { i -> i - 1 } }
                state = FetchingInfo(job = job, taskId = id)
            }
        mRunningTaskFlow.update { i -> i + 1 }
    }

    fun Task.download() {
        check(state == ReadyWithInfo && info != null)
        scope
            .launch {
                DownloadUtil.downloadVideo(
                        videoInfo = info,
                        taskId = id,
                        downloadPreferences = preferences,
                        progressCallback = { progress, _, text ->
                            when (val preState = state) {
                                is Running ->
                                    state = preState.copy(progress = progress, progressText = text)
                                else -> {}
                            }
                        })
                    .onSuccess { pathList -> state = Completed(pathList.firstOrNull()) }
                    .onFailure { throwable ->
                        state = Error(throwable = throwable, action = Download)
                    }
            }
            .also { job ->
                job.invokeOnCompletion { mRunningTaskFlow.update { i -> i - 1 } }
                state = Running(job = job, taskId = id)
            }
        mRunningTaskFlow.update { i -> i + 1 }
    }

    fun Task.cancel() {
        check(state is State.Cancelable)
        when (val preState = state) {
            is State.Cancelable -> {
                val res = YoutubeDL.destroyProcessById(preState.taskId)
                if (res) {
                    preState.job.cancel()
                    state = State.Canceled(action = preState.action)
                    mRunningTaskFlow.update { i -> i - 1 }
                }
            }
            else -> {}
        }
    }

    fun Task.restart() {
        check(state is State.Restartable)
        when (val preState = state) {
            is State.Restartable -> {
                state =
                    when (preState.action) {
                        Download -> ReadyWithInfo
                        FetchInfo -> Idle
                    }
            }
            else -> {}
        }
    }
}
