package com.junkfood.seal.download

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import com.junkfood.seal.R
import com.junkfood.seal.download.Task.Companion.attachInfo
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
import com.junkfood.seal.util.NotificationUtil
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "DownloaderV2"

/**
 * TODO:
 *     - Notification
 *     - Custom commands
 *     - States for ViewModels
 */
object DownloaderV2 {
    private val scope = CoroutineScope(SupervisorJob())
    private const val MAX_CONCURRENCY = 3

    init {
        scope.launch(Dispatchers.Default) {
            snapshotFlow { taskStateMap.toMap() }
                .collect {
                    doYourWork()
                    Log.d(TAG, "map changed!")
                    it.forEach { (task, state) ->
                        Log.d(TAG, "${state::class.simpleName}, ${task.id}")
                    }
                }
        }
    }

    val taskStateMap = mutableStateMapOf<Task, State>()

    private val runningTaskCount
        get() = taskStateMap.count { (_, state) -> state is Running || state is FetchingInfo }

    fun enqueue(task: Task) {
        val state: State = if (task.info != null) ReadyWithInfo else Idle
        taskStateMap += task to state
    }

    fun enqueue(taskList: List<Task>) {
        taskList.forEach { enqueue(it) }
    }

    private var Task.state: State
        get() = taskStateMap[this]!!
        set(value) {
            taskStateMap[this] = value
        }

    private val Task.notificationId: Int
        get() = id.hashCode()

    private fun doYourWork() {
        if (runningTaskCount >= MAX_CONCURRENCY) return

        taskStateMap.entries
            .sortedBy { (_, state) -> state }
            .firstOrNull { (_, state) -> state == Idle || state == ReadyWithInfo }
            ?.let { (task, state) ->
                when (state) {
                    Idle -> task.fetchInfo()
                    ReadyWithInfo -> task.download()
                    else -> {}
                }
            }
    }

    private fun Task.fetchInfo() {
        check(state == Idle)
        val task = this
        scope
            .launch(Dispatchers.Default) {
                DownloadUtil.fetchVideoInfoFromUrl(
                        url = url,
                        playlistIndex = playlistIndex,
                        preferences = preferences,
                        taskKey = id)
                    .onSuccess { info ->
                        taskStateMap.remove(task)
                        taskStateMap += task.attachInfo(info) to ReadyWithInfo
                    }
                    .onFailure { throwable ->
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        task.state = Error(throwable = throwable, action = FetchInfo)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.download_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString())
                    }
            }
            .also { job -> state = FetchingInfo(job = job, taskId = id) }
    }

    private fun Task.download() {
        check(state == ReadyWithInfo && info != null)
        scope
            .launch(Dispatchers.Default) {
                DownloadUtil.downloadVideo(
                        videoInfo = info,
                        taskId = id,
                        downloadPreferences = preferences,
                        progressCallback = { progress, _, text ->
                            when (val preState = state) {
                                is Running -> {
                                    state = preState.copy(progress = progress, progressText = text)
                                    NotificationUtil.notifyProgress(
                                        notificationId = notificationId,
                                        progress = progress.toInt(),
                                        text = text,
                                        title = viewState.title,
                                        taskId = id)
                                }
                                else -> {}
                            }
                        })
                    .onSuccess { pathList -> state = Completed(pathList.firstOrNull()) }
                    .onFailure { throwable ->
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        state = Error(throwable = throwable, action = Download)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.fetch_info_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString())
                    }
            }
            .also { job -> state = Running(job = job, taskId = id) }
    }

    fun Task.cancel() {
        when (val preState = state) {
            is State.Cancelable -> {
                val res = YoutubeDL.destroyProcessById(preState.taskId)
                if (res) {
                    preState.job.cancel()
                    state = State.Canceled(action = preState.action)
                }
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    fun Task.restart() {
        when (val preState = state) {
            is State.Restartable -> {
                state =
                    when (preState.action) {
                        Download -> ReadyWithInfo
                        FetchInfo -> Idle
                    }
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }
}
