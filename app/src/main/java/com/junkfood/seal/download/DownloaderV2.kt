package com.junkfood.seal.download

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.download.Task.DownloadState
import com.junkfood.seal.download.Task.DownloadState.Canceled
import com.junkfood.seal.download.Task.DownloadState.Completed
import com.junkfood.seal.download.Task.DownloadState.Error
import com.junkfood.seal.download.Task.DownloadState.FetchingInfo
import com.junkfood.seal.download.Task.DownloadState.Idle
import com.junkfood.seal.download.Task.DownloadState.ReadyWithInfo
import com.junkfood.seal.download.Task.DownloadState.Running
import com.junkfood.seal.download.Task.RestartableAction.Download
import com.junkfood.seal.download.Task.RestartableAction.FetchInfo
import com.junkfood.seal.download.Task.TypeInfo
import com.junkfood.seal.util.DebugLogger
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

private const val TAG = "DownloaderV2"

private const val MAX_CONCURRENCY = 3

interface DownloaderV2 {
    fun getTaskStateMap(): SnapshotStateMap<Task, Task.State>

    fun hasActiveTasks(): Boolean = false

    fun cancel(task: Task): Boolean

    fun cancel(taskId: String): Boolean {
        return getTaskStateMap().keys.find { it.id == taskId }?.let { cancel(it) } ?: false
    }

    fun restart(task: Task)

    fun enqueue(task: Task)

    fun enqueue(task: Task, state: Task.State)

    fun enqueue(taskWithState: TaskFactory.TaskWithState) {
        val (task, state) = taskWithState
        enqueue(task, state)
    }

    fun remove(task: Task): Boolean
}

internal object FakeDownloaderV2 : DownloaderV2 {
    override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
        return mutableStateMapOf()
    }

    override fun hasActiveTasks(): Boolean = false

    override fun cancel(task: Task): Boolean {
        return false
    }

    override fun restart(task: Task) {}

    override fun enqueue(task: Task) {}

    override fun enqueue(task: Task, state: Task.State) {}

    override fun remove(task: Task): Boolean {
        return true
    }
}

/**
 * DownloaderV2 implementation with proper foreground service management,
 * throttled disk persistence and notifications, and crash resilience.
 */
@OptIn(FlowPreview::class)
class DownloaderV2Impl(private val appContext: Context) : DownloaderV2, KoinComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val taskStateMap = mutableStateMapOf<Task, Task.State>()
    private val snapshotFlow = snapshotFlow { taskStateMap.toMap() }

    init {
        scope.launch(Dispatchers.Default) {
            snapshotFlow
                .onEach { doYourWork() }
                .map { it.hasActiveTasks() }
                .distinctUntilChanged()
                .collect { hasActive ->
                    DebugLogger.log(TAG, "Service lifecycle check: hasActiveTasks=$hasActive")
                    if (hasActive) App.startService() else App.stopService()
                }
        }

        scope.launch(Dispatchers.IO) {
            enqueueFromBackup()

            snapshotFlow
                .map { map ->
                    // Throttled signature: only trigger disk backup on structural state/lifecycle transitions,
                    // NOT on every fractional percentage progress tick
                    map.mapValues { (_, state) ->
                        val ds = state.downloadState
                        when (ds) {
                            is Running -> "Running"
                            is FetchingInfo -> "FetchingInfo"
                            Idle -> "Idle"
                            ReadyWithInfo -> "ReadyWithInfo"
                            is Completed -> "Completed"
                            is Canceled -> "Canceled"
                            is Error -> "Error"
                        } to (state.videoInfo?.id ?: state.viewState.title)
                    }
                }
                .distinctUntilChanged()
                .collect {
                    val map = taskStateMap.toMap()
                    DebugLogger.log(TAG, "Persisting task list backup (${map.size} tasks)")
                    PreferenceUtil.encodeTaskListBackup(map)
                }
        }
    }

    private suspend fun enqueueFromBackup() {
        val restoredTasks = withContext(Dispatchers.IO) {
            val taskList = PreferenceUtil.decodeTaskListBackup()
            DebugLogger.log(TAG, "enqueueFromBackup: loaded ${taskList.size} tasks")
            taskList.mapValues { (task, state) ->
                val preState = state.downloadState
                val downloadState = when (preState) {
                    is Completed -> preState
                    is FetchingInfo,
                    Idle -> Canceled(action = FetchInfo)
                    is Running,
                    ReadyWithInfo -> {
                        // Check if the downloaded file already exists on disk before assuming canceled
                        val title = state.viewState.title
                        val videoId = state.videoInfo?.id
                        val dir = if (task.preferences.extractAudio) App.audioDownloadDir else App.videoDownloadDir
                        val existingFile =
                            if (title.isNotBlank()) {
                                runCatching {
                                    File(dir).walkTopDown().firstOrNull {
                                        it.isFile &&
                                            it.length() > 0 &&
                                            (it.nameWithoutExtension.equals(title, ignoreCase = true) ||
                                                (videoId != null && it.name.contains(videoId)))
                                    }
                                }.getOrNull()
                            } else {
                                null
                            }

                        if (existingFile != null) {
                            DebugLogger.log(TAG, "Restored task ${task.id} found finished on disk: ${existingFile.name}")
                            Completed(existingFile.absolutePath)
                        } else {
                            Canceled(
                                action = Download,
                                progress = (preState as? Running)?.progress,
                            )
                        }
                    }
                    else -> preState
                }
                state.copy(downloadState = downloadState)
            }
        }

        // Apply to taskStateMap on Main dispatcher to ensure thread safety with Compose UI
        withContext(Dispatchers.Main) {
            restoredTasks.forEach { (task, state) ->
                taskStateMap[task] = state
            }
        }
    }

    private fun Map<Task, Task.State>.countRunning(): Int = count { (_, state) ->
        state.downloadState is Running || state.downloadState is FetchingInfo
    }

    private fun Map<Task, Task.State>.hasActiveTasks(): Boolean = any { (_, state) ->
        when (state.downloadState) {
            is Idle,
            is FetchingInfo,
            is ReadyWithInfo,
            is Running -> true
            else -> false
        }
    }

    override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
        return taskStateMap
    }

    override fun hasActiveTasks(): Boolean = taskStateMap.hasActiveTasks()

    override fun enqueue(task: Task) {
        DebugLogger.log(TAG, "enqueue: url=${task.url}, id=${task.id}")
        taskStateMap +=
            task to Task.State(Idle, null, Task.ViewState(url = task.url, title = task.url))
    }

    override fun enqueue(task: Task, state: Task.State) {
        DebugLogger.log(TAG, "enqueue with state: url=${task.url}, state=${state.downloadState::class.simpleName}")
        taskStateMap += task to state
    }

    override fun remove(task: Task): Boolean {
        DebugLogger.log(TAG, "remove: id=${task.id}")
        if (taskStateMap.contains(task)) {
            taskStateMap.remove(task)
            return true
        }
        return false
    }

    override fun cancel(task: Task): Boolean {
        DebugLogger.log(TAG, "cancel: id=${task.id}")
        return task.cancelImpl()
    }

    override fun restart(task: Task) {
        DebugLogger.log(TAG, "restart: id=${task.id}")
        task.restartImpl()
    }

    private var Task.state: Task.State
        get() = taskStateMap[this]!!
        set(value) {
            taskStateMap[this] = value
        }

    private var Task.downloadState: DownloadState
        get() = state.downloadState
        set(value) {
            val prevState = state
            taskStateMap[this] = prevState.copy(downloadState = value)
        }

    private var Task.info: VideoInfo?
        get() = state.videoInfo
        set(value) {
            val prevState = state
            taskStateMap[this] = prevState.copy(videoInfo = value)
        }

    private var Task.viewState: Task.ViewState
        get() = state.viewState
        set(value) {
            val prevState = state
            taskStateMap[this] = prevState.copy(viewState = value)
        }

    private val Task.notificationId: Int
        get() = id.hashCode()

    /** Processes pending tasks, prioritizing downloads. */
    private fun doYourWork() {
        if (taskStateMap.countRunning() >= MAX_CONCURRENCY) return

        taskStateMap.entries
            .sortedBy { (_, state) -> state.downloadState }
            .firstOrNull { (_, state) ->
                state.downloadState == ReadyWithInfo || state.downloadState == Idle
            }
            ?.let { (task, state) ->
                when (state.downloadState) {
                    Idle -> task.prepare()
                    ReadyWithInfo -> task.download()
                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
    }

    private fun Task.prepare() {
        check(downloadState == Idle)
        DebugLogger.log(TAG, "prepare: id=$id, type=${type::class.simpleName}")
        if (type is TypeInfo.CustomCommand) {
            execute()
        } else {
            fetchInfo()
        }
    }

    private fun Task.fetchInfo() {
        check(downloadState == Idle)
        DebugLogger.log(TAG, "fetchInfo starting: id=$id, url=$url")
        val task = this
        val taskInfo = task.type
        val playlistIndex = if (taskInfo is TypeInfo.Playlist) taskInfo.index else null
        scope
            .launch(Dispatchers.Default) {
                DownloadUtil.fetchVideoInfoFromUrl(
                        url = url,
                        playlistIndex = playlistIndex,
                        preferences = preferences,
                        taskKey = id,
                    )
                    .onSuccess {
                        DebugLogger.log(TAG, "fetchInfo success: id=$id, title=${it.title}")
                        info = it
                        downloadState = ReadyWithInfo
                        viewState = Task.ViewState.fromVideoInfo(it)
                    }
                    .onFailure { throwable ->
                        DebugLogger.log(TAG, "fetchInfo failed: id=$id, error=${throwable.message}", throwable)
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        task.downloadState = Error(throwable = throwable, action = FetchInfo)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.download_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString(),
                        )
                    }
            }
            .also { job -> downloadState = FetchingInfo(job = job, taskId = id) }
    }

    private fun Task.download() {
        check(downloadState == ReadyWithInfo && info != null)
        DebugLogger.log(TAG, "download starting: id=$id, title=${viewState.title}")
        if (type is TypeInfo.CustomCommand) {
            execute()
            return
        }
        scope
            .launch(Dispatchers.Default) {
                var lastNotificationTime = 0L
                var lastReportedProgress = -1
                var lastReportedText = ""
                DownloadUtil.downloadVideo(
                        videoInfo = info,
                        taskId = id,
                        downloadPreferences = preferences,
                        progressCallback = { progressPercentage, _, text ->
                            val progress = progressPercentage / 100f
                            when (val preState = downloadState) {
                                is Running -> {
                                    downloadState =
                                        preState.copy(progress = progress, progressText = text)
                                    val now = System.currentTimeMillis()
                                    val progressInt = progressPercentage.toInt()
                                    val timeElapsed = now - lastNotificationTime >= 500L
                                    val isFinished = progressPercentage >= 100f
                                    if ((timeElapsed && (progressInt != lastReportedProgress || text != lastReportedText)) || isFinished) {
                                        lastNotificationTime = now
                                        lastReportedProgress = progressInt
                                        lastReportedText = text
                                        NotificationUtil.notifyProgress(
                                            notificationId = notificationId,
                                            progress = progressInt,
                                            text = text,
                                            title = viewState.title,
                                            taskId = id,
                                        )
                                    }
                                }
                                else -> {}
                            }
                        },
                    )
                    .onSuccess { pathList ->
                        DebugLogger.log(TAG, "download finished successfully: id=$id, paths=$pathList")
                        downloadState = Completed(pathList.firstOrNull())

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
                        DebugLogger.log(TAG, "download failed: id=$id, error=${throwable.message}", throwable)
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        downloadState = Error(throwable = throwable, action = Download)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.fetch_info_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString(),
                        )
                    }
            }
            .also { job -> downloadState = Running(job = job, taskId = id) }
    }

    private fun Task.cancelImpl(): Boolean {
        DebugLogger.log(TAG, "cancelImpl: id=$id, state=${downloadState::class.simpleName}")
        when (val preState = downloadState) {
            is DownloadState.Cancelable -> {
                val res = YoutubeDL.destroyProcessById(preState.taskId)
                if (res) {
                    preState.job.cancel()
                    val progress = if (preState is Running) preState.progress else null
                    NotificationUtil.cancelNotification(notificationId)
                    downloadState =
                        Canceled(action = preState.action, progress = progress)
                }
                return res
            }
            Idle -> {
                downloadState = Canceled(action = FetchInfo)
            }
            ReadyWithInfo -> {
                downloadState = Canceled(action = Download)
            }

            else -> {
                return false
            }
        }
        return true
    }

    private fun Task.restartImpl() {
        DebugLogger.log(TAG, "restartImpl: id=$id, state=${downloadState::class.simpleName}")
        when (val preState = downloadState) {
            is DownloadState.Restartable -> {
                downloadState =
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

    /**
     * Execute a custom command task
     *
     * @see Task.TypeInfo.CustomCommand
     */
    private fun Task.execute() {
        check(downloadState == Idle)
        check(type is TypeInfo.CustomCommand)
        val template = type.template
        DebugLogger.log(TAG, "execute custom command: id=$id, template=${template.name}")
        scope
            .launch {
                DownloadUtil.executeCustomCommandTask(url, id, template, preferences) {
                        progressPercentage,
                        _,
                        text ->
                        val progress = progressPercentage / 100f
                        when (val preState = downloadState) {
                            is Running -> {
                                downloadState =
                                    preState.copy(progress = progress, progressText = text)
                                NotificationUtil.makeNotificationForCustomCommand(
                                    notificationId = notificationId,
                                    taskId = id,
                                    progress = progressPercentage.toInt(),
                                    templateName = template.name,
                                    taskUrl = url,
                                    text = text,
                                )
                            }
                            else -> {}
                        }
                    }
                    .onFailure { throwable ->
                        DebugLogger.log(TAG, "custom command failed: id=$id, error=${throwable.message}", throwable)
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        downloadState = Error(throwable = throwable, action = Download)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.fetch_info_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString(),
                        )
                    }
                    .onSuccess {
                        DebugLogger.log(TAG, "custom command finished: id=$id")
                        downloadState = Completed(null)

                        val text = appContext.getString(R.string.status_completed)

                        NotificationUtil.finishNotification(
                            notificationId = notificationId,
                            title = viewState.title,
                            text = text,
                            intent = null,
                        )
                    }
            }
            .also { downloadState = Running(job = it, taskId = id) }
    }
}
