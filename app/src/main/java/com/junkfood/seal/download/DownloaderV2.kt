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
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
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

data class PlaylistEnqueueResult(val enqueued: Int, val total: Int) {
    val skipped: Int get() = (total - enqueued).coerceAtLeast(0)
}

interface DownloaderV2 {
    fun getTaskStateMap(): SnapshotStateMap<Task, Task.State>

    fun cancel(task: Task): Boolean

    fun cancel(taskId: String): Boolean {
        return getTaskStateMap().keys.find { it.id == taskId }?.let { cancel(it) } ?: false
    }

    fun restart(task: Task)

    /** Enqueue a [Task] with an empty [Task.State] */
    fun enqueue(task: Task)

    fun enqueue(task: Task, state: Task.State)

    fun enqueue(taskWithState: TaskFactory.TaskWithState) {
        val (task, state) = taskWithState
        enqueue(task, state)
    }

    fun downloadPlaylistItems(
        playlistResult: PlaylistResult,
        preferences: DownloadUtil.DownloadPreferences =
            DownloadUtil.DownloadPreferences.createFromPreferences(),
    ): PlaylistEnqueueResult

    fun downloadImages(imageUrls: List<String>, quality: com.junkfood.seal.ui.page.downloadv2.configure.ImageQuality = com.junkfood.seal.ui.page.downloadv2.configure.ImageQuality.ORIGINAL): Int

    fun remove(task: Task): Boolean
}

internal object FakeDownloaderV2 : DownloaderV2 {
    override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
        return mutableStateMapOf()
    }

    override fun cancel(task: Task): Boolean {
        return false
    }

    override fun restart(task: Task) {}

    override fun enqueue(task: Task) {}

    override fun enqueue(task: Task, state: Task.State) {}

    override fun downloadPlaylistItems(
        playlistResult: PlaylistResult,
        preferences: DownloadUtil.DownloadPreferences,
    ): PlaylistEnqueueResult = PlaylistEnqueueResult(0, playlistResult.entries?.size ?: 0)

    override fun downloadImages(imageUrls: List<String>, quality: com.junkfood.seal.ui.page.downloadv2.configure.ImageQuality): Int = 0

    override fun remove(task: Task): Boolean {
        return true
    }
}

/**
 * TODO:
 *     - Notification
 *     - Custom commands
 *     - States for ViewModels
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
                .map { it.countRunning() }
                .distinctUntilChanged()
                .collect { if (it > 0) App.startService() else App.stopService() }
        }

        scope.launch(Dispatchers.IO) {
            // don't write before we read
            enqueueFromBackup()

            snapshotFlow
                .map { it.filter { it.value.downloadState !is Completed } }
                .distinctUntilChanged()
                .collect {
                    it.forEach { Log.d(TAG, it.value.viewState.title) }
                    PreferenceUtil.encodeTaskListBackup(it)
                }
        }
    }

    private fun enqueueFromBackup() {
        val taskList =
            PreferenceUtil.decodeTaskListBackup()
                .filter { it.value.downloadState !is Completed }
                .mapValues { (_, state) ->
                    val preState = state.downloadState
                    val downloadState =
                        when (preState) {
                            is FetchingInfo,
                            Idle -> {
                                Canceled(action = FetchInfo)
                            }
                            is Running -> {
                                Canceled(action = Download, progress = preState.progress)
                            }

                            ReadyWithInfo -> {
                                Canceled(action = Download, progress = null)
                            }
                            else -> {
                                preState
                            }
                        }
                    state.copy(downloadState = downloadState)
                }
        taskList.forEach(::enqueue)
    }

    private fun Map<Task, Task.State>.countRunning(): Int = count { (_, state) ->
        state.downloadState is Running || state.downloadState is FetchingInfo
    }

    override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
        return taskStateMap
    }

    override fun enqueue(task: Task) {
        taskStateMap +=
            task to Task.State(Idle, null, Task.ViewState(url = task.url, title = task.url))
    }

    override fun enqueue(task: Task, state: Task.State) {
        taskStateMap += task to state
    }

    override fun downloadPlaylistItems(
        playlistResult: PlaylistResult,
        preferences: DownloadUtil.DownloadPreferences,
    ): PlaylistEnqueueResult {
        val entries = playlistResult.entries.orEmpty()
        val total = entries.size
        if (total == 0) {
            Log.w(TAG, "Playlist has no entries: ${playlistResult.title}")
            return PlaylistEnqueueResult(enqueued = 0, total = 0)
        }

        val playlistUrl = playlistResult.originalUrl ?: playlistResult.webpageUrl
        if (playlistUrl.isNullOrBlank()) {
            Log.w(TAG, "Unable to resolve playlist URL for ${playlistResult.title}")
            return PlaylistEnqueueResult(enqueued = 0, total = total)
        }

        val playableIndexes =
            entries.mapIndexedNotNull { index, entry ->
                if (entry.url.isNullOrBlank()) {
                    null
                } else {
                    index + 1
                }
            }

        if (playableIndexes.isEmpty()) {
            Log.w(TAG, "No playable entries detected for playlist ${playlistResult.title}")
            return PlaylistEnqueueResult(enqueued = 0, total = total)
        }

        val resolvedPreferences =
            if (preferences.downloadPlaylist) preferences else preferences.copy(downloadPlaylist = true)

        TaskFactory.createWithPlaylistResult(
                playlistUrl = playlistUrl,
                indexList = playableIndexes,
                playlistResult = playlistResult,
                preferences = resolvedPreferences,
            )
            .forEach(::enqueue)

        return PlaylistEnqueueResult(enqueued = playableIndexes.size, total = total)
    }

    override fun downloadImages(imageUrls: List<String>, quality: com.junkfood.seal.ui.page.downloadv2.configure.ImageQuality): Int {
        if (imageUrls.isEmpty()) {
            return 0
        }

        var enqueuedCount = 0
        scope.launch(Dispatchers.IO) {
            imageUrls.forEach { baseUrl ->
                // Pick best available URL before enqueuing
                val urlAndExt = DownloadUtil.pickBestImageUrl(baseUrl, quality)
                if (urlAndExt == null) {
                    Log.w(TAG, "No valid image URL found for $baseUrl")
                    return@forEach
                }

                val (finalUrl, extension) = urlAndExt
                
                // Generate safe filename: extract video ID or use timestamp
                val videoId = baseUrl.substringAfter("/vi/", "").substringBefore("/", "")
                val baseName = if (videoId.isNotEmpty()) {
                    "${videoId}_${quality.name.lowercase()}"
                } else {
                    "image_${System.currentTimeMillis()}"
                }
                val filename = "$baseName.$extension"
                
                val task = Task(
                    url = finalUrl,
                    type = TypeInfo.ImageDownload(filename),
                    preferences = DownloadUtil.DownloadPreferences.EMPTY
                )
                val viewState = Task.ViewState(
                    url = finalUrl,
                    title = filename,
                    uploader = "",
                    thumbnailUrl = finalUrl
                )
                val state = Task.State(
                    downloadState = Idle,
                    videoInfo = null,
                    viewState = viewState
                )
                withContext(Dispatchers.Main) {
                    enqueue(task, state)
                }
                enqueuedCount++
            }
        }

        return imageUrls.size
    }

    /**
     * Noted the caller is responsible for stopping the [task] before removing it
     *
     * @return true if the task was removed
     */
    override fun remove(task: Task): Boolean {
        if (taskStateMap.contains(task)) {
            taskStateMap.remove(task)
            return true
        }
        return false
    }

    override fun cancel(task: Task): Boolean = task.cancelImpl()

    override fun restart(task: Task) {
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
        if (type is TypeInfo.CustomCommand) {
            execute()
        } else if (type is TypeInfo.ImageDownload) {
            // Images don't need info fetching, go straight to download
            downloadState = ReadyWithInfo
        } else {
            fetchInfo()
        }
    }

    private fun Task.fetchInfo() {
        check(downloadState == Idle)
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
                        info = it
                        downloadState = ReadyWithInfo
                        viewState = Task.ViewState.fromVideoInfo(it)
                    }
                    .onFailure { throwable ->
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
        check(downloadState == ReadyWithInfo)
        if (type is TypeInfo.CustomCommand) {
            execute()
            return
        }
        if (type is TypeInfo.ImageDownload) {
            downloadImage()
            return
        }
        check(info != null)
        scope
            .launch(Dispatchers.Default) {
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
            .also { downloadState = Running(job = it, taskId = id) }
    }

    private fun Task.downloadImage() {
        check(type is TypeInfo.ImageDownload)
        val task = this
        
        scope
            .launch(Dispatchers.Default) {
                val imageUrl = task.url
                val fileName = (task.type as TypeInfo.ImageDownload).filename

                if (imageUrl.isEmpty()) {
                    task.downloadState = Error(Throwable("Invalid image URL"), Download)
                    return@launch
                }

                try {
                    val outputFile = FileUtil.getPicturesSealFile(fileName)

                    DownloadUtil.downloadImageFromUrl(
                        url = imageUrl,
                        outputFile = outputFile,
                        onProgress = { progress ->
                            when (val preState = downloadState) {
                                is Running -> {
                                    downloadState = preState.copy(progress = progress)
                                    NotificationUtil.notifyProgress(
                                        notificationId = notificationId,
                                        progress = (progress * 100).toInt(),
                                        text = "Downloading...",
                                        title = viewState.title,
                                        taskId = id,
                                    )
                                }
                                else -> {}
                            }
                        }
                    )

                    downloadState = Completed(outputFile.absolutePath)
                    
                    // Trigger media scan so gallery sees the new image
                    android.media.MediaScannerConnection.scanFile(
                        appContext,
                        arrayOf(outputFile.absolutePath),
                        null,
                        null
                    )
                    
                    val text = appContext.getString(R.string.download_finish_notification)
                    FileUtil.createIntentForOpeningFile(outputFile.absolutePath).run {
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
                } catch (e: Exception) {
                    downloadState = Error(e, Download)
                    NotificationUtil.notifyError(
                        title = viewState.title,
                        textId = R.string.fetch_info_error_msg,
                        notificationId = notificationId,
                        report = e.stackTraceToString(),
                    )
                }
            }
            .also { job -> downloadState = Running(job = job, taskId = id) }
    }

    private fun Task.cancelImpl(): Boolean {
        when (val preState = downloadState) {
            is DownloadState.Cancelable -> {
                val res = YoutubeDL.destroyProcessById(preState.taskId)
                if (res) {
                    preState.job.cancel()
                    val progress = if (preState is Running) preState.progress else null
                    NotificationUtil.cancelNotification(notificationId)
                    downloadState =
                        DownloadState.Canceled(action = preState.action, progress = progress)
                }
                return res
            }
            Idle -> {
                downloadState = DownloadState.Canceled(action = FetchInfo)
            }
            ReadyWithInfo -> {
                downloadState = DownloadState.Canceled(action = Download)
            }

            else -> {
                return false
            }
        }
        return true
    }

    private fun Task.restartImpl() {
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
