package com.junkfood.seal.download

import android.app.PendingIntent
import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.NotificationUtil
import com.yausername.youtubedl_android.YoutubeDL
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

private const val TAG = "DownloaderV2"

private const val MAX_CONCURRENCY = 3

interface DownloaderV2 {
    fun getTaskStateMap(): SnapshotStateMap<Task, State>

    fun enqueue(task: Task)

    fun enqueue(taskList: List<Task>)

    fun cancel(task: Task)

    fun restart(task: Task)
}

/**
 * TODO:
 *     - Notification
 *     - Custom commands
 *     - States for ViewModels
 */
class DownloaderV2Impl(private val appContext: Context) : DownloaderV2, KoinComponent {
    private val scope = CoroutineScope(SupervisorJob())
    private val taskStateMap = mutableStateMapOf<Task, State>()

    init {
        scope.launch(Dispatchers.Default) {
            snapshotFlow { taskStateMap.toMap() }.collect { doYourWork() }
        }
    }

    private val runningTaskCount
        get() = taskStateMap.count { (_, state) -> state is Running || state is FetchingInfo }

    override fun getTaskStateMap(): SnapshotStateMap<Task, State> {
        return taskStateMap
    }

    override fun enqueue(task: Task) {
        val state: State = if (task.info != null) ReadyWithInfo else Idle
        taskStateMap += task to state
    }

    override fun enqueue(taskList: List<Task>) {
        taskList.forEach { enqueue(it) }
    }

    override fun cancel(task: Task) {
        task.cancelImpl()
    }

    override fun restart(task: Task) {
        task.restartImpl()
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
                        taskKey = id,
                    )
                    .onSuccess { info ->
                        taskStateMap.run {
                            put(task.attachInfo(info), ReadyWithInfo)
                            taskStateMap.remove(task)
                        }
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
                            report = throwable.stackTraceToString(),
                        )
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
                        progressCallback = { progressPercentage, _, text ->
                            val progress = progressPercentage / 100f
                            when (val preState = state) {
                                is Running -> {
                                    state = preState.copy(progress = progress, progressText = text)
                                    NotificationUtil.notifyProgress(
                                        notificationId = notificationId,
                                        progress = progressPercentage.toInt(),
                                        text = text,
                                        title = viewState.title,
                                        taskId = id,
                                    )
                                }
                                else -> {}
                            }
                        },
                    )
                    .onSuccess { pathList ->
                        state = Completed(pathList.firstOrNull())

                        val text =
                            appContext.getString(
                                if (pathList.isEmpty()) R.string.status_completed
                                else R.string.download_finish_notification
                            )
                        FileUtil.createIntentForOpeningFile(pathList.firstOrNull()).run {
                            NotificationUtil.finishNotification(
                                notificationId,
                                title = viewState.title,
                                text = text,
                                intent =
                                    if (this != null)
                                        PendingIntent.getActivity(
                                            appContext,
                                            0,
                                            this,
                                            PendingIntent.FLAG_IMMUTABLE,
                                        )
                                    else null,
                            )
                        }
                    }
                    .onFailure { throwable ->
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        state = Error(throwable = throwable, action = Download)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.fetch_info_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString(),
                        )
                    }
            }
            .also { job -> state = Running(job = job, taskId = id) }
    }

    private fun Task.cancelImpl() {
        when (val preState = state) {
            is State.Cancelable -> {
                val res = YoutubeDL.destroyProcessById(preState.taskId)
                if (res) {
                    preState.job.cancel()
                    val progress = if (preState is Running) preState.progress else null
                    state = State.Canceled(action = preState.action, progress = progress)
                }
            }
            Idle -> {
                state = State.Canceled(action = FetchInfo)
            }
            ReadyWithInfo -> {
                state = State.Canceled(action = Download)
            }

            else -> {
                throw IllegalStateException()
            }
        }
    }

    private fun Task.restartImpl() {
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
