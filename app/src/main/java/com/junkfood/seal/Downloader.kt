package com.junkfood.seal

import android.app.PendingIntent
import android.util.Log
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.App.Companion.startService
import com.junkfood.seal.App.Companion.stopService
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.util.COMMAND_DIRECTORY
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PlaylistEntry
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import com.yausername.youtubedl_android.YoutubeDL
import java.util.concurrent.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Singleton Downloader for state holder & perform downloads, used by `Activity` & `Service` */
object Downloader {

    private const val TAG = "Downloader"

    sealed class State {
        data class DownloadingPlaylist(
            val currentItem: Int = 0,
            val itemCount: Int = 0,
        ) : State()

        data object DownloadingVideo : State()

        data object FetchingInfo : State()

        data object Idle : State()

        data object Updating : State()
    }

    sealed class ErrorState(
        open val url: String = "",
        open val report: String = "",
    ) {
        data class DownloadError(override val url: String, override val report: String) :
            ErrorState(url = url, report = report)

        data class FetchInfoError(override val url: String, override val report: String) :
            ErrorState(url = url, report = report)

        data object None : ErrorState()

        val title: String
            @Composable
            get() =
                when (this) {
                    is DownloadError -> stringResource(id = R.string.download_error_msg)
                    is FetchInfoError -> stringResource(id = R.string.fetch_info_error_msg)
                    None -> ""
                }
    }

    data class CustomCommandTask(
        val template: CommandTemplate,
        val url: String,
        val output: String,
        val state: State,
        val currentLine: String
    ) {
        fun toKey() = makeKey(url, template.name)

        sealed class State {
            data class Error(val errorReport: String) : State()

            object Completed : State()

            object Canceled : State()

            data class Running(val progress: Float) : State()
        }

        override fun hashCode(): Int {
            return (this.url + this.template.name + this.template.template).hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CustomCommandTask

            if (template != other.template) return false
            if (url != other.url) return false
            if (output != other.output) return false
            if (state != other.state) return false
            if (currentLine != other.currentLine) return false

            return true
        }

        fun onCopyLog(clipboardManager: ClipboardManager) {
            clipboardManager.setText(AnnotatedString(output))
        }

        fun onRestart() {
            applicationScope.launch(Dispatchers.IO) {
                DownloadUtil.executeCommandInBackground(url, template)
            }
        }

        fun onCopyError(clipboardManager: ClipboardManager) {
            clipboardManager.setText(AnnotatedString(currentLine))
            ToastUtil.makeToast(R.string.error_copied)
        }

        fun onCancel() {
            toKey().run {
                YoutubeDL.destroyProcessById(this)
                onProcessCanceled(this)
            }
        }
    }

    data class DownloadTaskItem(
        val webpageUrl: String = "",
        val title: String = "",
        val uploader: String = "",
        val duration: Int = 0,
        val fileSizeApprox: Double = .0,
        val progress: Float = 0f,
        val progressText: String = "",
        val thumbnailUrl: String = "",
        val taskId: String = "",
        val playlistIndex: Int = 0,
    )

    private var currentJob: Job? = null
    private var downloadResultTemp: Result<List<String>> = Result.failure(Exception())

    private val mutableDownloaderState: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    private val mutableTaskState = MutableStateFlow(DownloadTaskItem())
    private val mutablePlaylistResult = MutableStateFlow(PlaylistResult())
    private val mutableErrorState: MutableStateFlow<ErrorState> = MutableStateFlow(ErrorState.None)
    private val mutableProcessCount = MutableStateFlow(0)
    private val mutableQuickDownloadCount = MutableStateFlow(0)

    val mutableTaskList = mutableStateMapOf<String, CustomCommandTask>()

    val taskState = mutableTaskState.asStateFlow()
    val downloaderState = mutableDownloaderState.asStateFlow()
    val playlistResult = mutablePlaylistResult.asStateFlow()
    val errorState = mutableErrorState.asStateFlow()
    val processCount = mutableProcessCount.asStateFlow()

    init {
        applicationScope.launch {
            downloaderState
                .combine(processCount) { state, cnt ->
                    if (cnt > 0) true
                    else
                        when (state) {
                            is State.Idle -> false
                            else -> true
                        }
                }
                .combine(mutableQuickDownloadCount) { isRunning, cnt ->
                    if (!isRunning) cnt > 0 else true
                }
                .collect { if (it) startService() else stopService() }
        }
    }

    fun isDownloaderAvailable(): Boolean {
        return downloaderState.value is State.Idle
    }

    fun makeKey(url: String, templateName: String): String = "${templateName}_$url"

    fun onTaskStarted(template: CommandTemplate, url: String) =
        CustomCommandTask(
                template = template,
                url = url,
                output = "",
                state = CustomCommandTask.State.Running(0f),
                currentLine = "")
            .run { mutableTaskList.put(this.toKey(), this) }

    fun updateTaskOutput(template: CommandTemplate, url: String, line: String, progress: Float) {
        val key = makeKey(url, template.name)
        val oldValue = mutableTaskList[key] ?: return
        val newValue =
            oldValue.run {
                copy(
                    output = output + line + "\n",
                    currentLine = line,
                    state = CustomCommandTask.State.Running(progress))
            }
        mutableTaskList[key] = newValue
    }

    fun onTaskEnded(template: CommandTemplate, url: String, response: String? = null) {
        val key = makeKey(url, template.name)
        NotificationUtil.finishNotification(
            notificationId = key.toNotificationId(),
            title = key,
            text = context.getString(R.string.status_completed),
        )
        mutableTaskList.run {
            val oldValue = get(key) ?: return
            val newValue =
                oldValue.copy(state = CustomCommandTask.State.Completed).run {
                    response?.let { copy(output = response) } ?: this
                }
            this[key] = newValue
        }
        FileUtil.scanDownloadDirectoryToMediaLibrary(COMMAND_DIRECTORY.getString())
    }

    fun onProcessEnded() = mutableProcessCount.update { it - 1 }

    fun onProcessCanceled(taskId: String) =
        mutableTaskList.run {
            get(taskId)?.let { this.put(taskId, it.copy(state = CustomCommandTask.State.Canceled)) }
        }

    fun onTaskError(errorReport: String, template: CommandTemplate, url: String) =
        mutableTaskList.run {
            val key = makeKey(url, template.name)
            NotificationUtil.notifyError(
                title = "",
                notificationId = key.toNotificationId(), report = errorReport)
            val oldValue = mutableTaskList[key] ?: return
            mutableTaskList[key] =
                oldValue.copy(
                    state = CustomCommandTask.State.Error(errorReport),
                    currentLine = errorReport,
                    output = oldValue.output + "\n" + errorReport)
        }

    private fun VideoInfo.toTask(playlistIndex: Int = 0, preferencesHash: Int): DownloadTaskItem =
        DownloadTaskItem(
            webpageUrl = webpageUrl.toString(),
            title = title,
            uploader = uploader ?: channel ?: uploaderId.toString(),
            duration = duration?.roundToInt() ?: 0,
            taskId = id + preferencesHash,
            thumbnailUrl = thumbnail.toHttpsUrl(),
            fileSizeApprox = fileSize ?: fileSizeApprox ?: .0,
            playlistIndex = playlistIndex)

    fun updateState(state: State) = mutableDownloaderState.update { state }

    fun clearErrorState() {
        mutableErrorState.update { ErrorState.None }
    }

    private fun fetchInfoError(url: String, errorReport: String) {
        mutableErrorState.update { ErrorState.FetchInfoError(url, errorReport) }
    }

    private fun downloadError(url: String, errorReport: String) {
        mutableErrorState.update { ErrorState.DownloadError(url, errorReport) }
    }

    private fun clearProgressState(isFinished: Boolean) {
        mutableTaskState.update {
            it.copy(
                progress = if (isFinished) 100f else 0f,
                progressText = "",
            )
        }
        if (!isFinished) downloadResultTemp = Result.failure(Exception())
    }

    fun updatePlaylistResult(playlistResult: PlaylistResult = PlaylistResult()) =
        mutablePlaylistResult.update { playlistResult }

    fun getInfoAndDownload(
        url: String,
        preferences: DownloadUtil.DownloadPreferences =
            DownloadUtil.DownloadPreferences.createFromPreferences(),
    ) {
        currentJob =
            applicationScope.launch(Dispatchers.IO) {
                updateState(State.FetchingInfo)
                DownloadUtil.fetchVideoInfoFromUrl(url = url, preferences = preferences)
                    .onFailure {
                        manageDownloadError(
                            th = it, url = url, isFetchingInfo = true, isTaskAborted = true)
                    }
                    .onSuccess { info ->
                        downloadResultTemp =
                            downloadVideo(videoInfo = info, preferences = preferences)
                    }
            }
    }

    fun addToDownloadQueue(
        videoInfo: VideoInfo? = null,
        url: String = videoInfo?.originalUrl ?: "",
        preferences: DownloadUtil.DownloadPreferences =
            DownloadUtil.DownloadPreferences.createFromPreferences(),
    ) {
        require(url.isNotEmpty() || videoInfo != null)

        if (!isDownloaderAvailable()) {
            ToastUtil.makeToast(R.string.task_added)
            applicationScope
                .launch(Dispatchers.Default) {
                    while (!isDownloaderAvailable()) {
                        delay(3000)
                    }
                }
                .invokeOnCompletion {
                    videoInfo?.let {
                        downloadVideoWithInfo(info = videoInfo, preferences = preferences)
                    } ?: getInfoAndDownload(url, preferences)
                }
        } else {
            videoInfo?.let { downloadVideoWithInfo(info = videoInfo, preferences = preferences) }
                ?: getInfoAndDownload(url, preferences)
        }
    }

    fun downloadVideoWithInfo(
        info: VideoInfo,
        preferences: DownloadUtil.DownloadPreferences =
            DownloadUtil.DownloadPreferences.createFromPreferences()
    ) {
        currentJob =
            applicationScope.launch(Dispatchers.IO) {
                downloadResultTemp = downloadVideo(videoInfo = info, preferences = preferences)
            }
    }

    /**
     * This method is used for download a single video and multiple videos from playlist at the same
     * time.
     *
     * @see downloadVideoInPlaylistByIndexList
     * @see getInfoAndDownload
     */
    @CheckResult
    private suspend fun downloadVideo(
        playlistIndex: Int = 0,
        playlistUrl: String = "",
        videoInfo: VideoInfo,
        preferences: DownloadUtil.DownloadPreferences =
            DownloadUtil.DownloadPreferences.createFromPreferences()
    ): Result<List<String>> {

        Log.d(TAG, preferences.subtitleLanguage)
        mutableTaskState.update { videoInfo.toTask(preferencesHash = preferences.hashCode()) }

        val isDownloadingPlaylist = downloaderState.value is State.DownloadingPlaylist
        if (!isDownloadingPlaylist) updateState(State.DownloadingVideo)
        val taskId = videoInfo.id + preferences.hashCode()
        val notificationId = taskId.toNotificationId()
        Log.d(TAG, "downloadVideo: id=${videoInfo.id} " + videoInfo.title)
        Log.d(TAG, "notificationId: $notificationId")

        NotificationUtil.notifyProgress(notificationId = notificationId, title = videoInfo.title)
        return DownloadUtil.downloadVideo(
                videoInfo = videoInfo,
                playlistUrl = playlistUrl,
                playlistItem = playlistIndex,
                downloadPreferences = preferences,
                taskId = videoInfo.id + preferences.hashCode()) { progress, _, line ->
                    Log.d(TAG, line)
                    mutableTaskState.update { it.copy(progress = progress, progressText = line) }
                    NotificationUtil.notifyProgress(
                        notificationId = notificationId,
                        progress = progress.toInt(),
                        text = line,
                        title = videoInfo.title,
                        taskId = taskId)
                }
            .onFailure {
                manageDownloadError(
                    th = it,
                    url = videoInfo.originalUrl,
                    title = videoInfo.title,
                    isFetchingInfo = false,
                    notificationId = notificationId,
                    isTaskAborted = !isDownloadingPlaylist)
            }
            .onSuccess {
                if (!isDownloadingPlaylist) finishProcessing()
                val text =
                    context.getString(
                        if (it.isEmpty()) R.string.status_completed
                        else R.string.download_finish_notification)
                FileUtil.createIntentForOpeningFile(it.firstOrNull()).run {
                    NotificationUtil.finishNotification(
                        notificationId,
                        title = videoInfo.title,
                        text = text,
                        intent =
                            if (this != null)
                                PendingIntent.getActivity(
                                    context, 0, this, PendingIntent.FLAG_IMMUTABLE)
                            else null)
                }
            }
    }

    fun downloadVideoInPlaylistByIndexList(
        url: String,
        indexList: List<Int>,
        playlistItemList: List<PlaylistEntry> = emptyList(),
        preferences: DownloadUtil.DownloadPreferences =
            DownloadUtil.DownloadPreferences.createFromPreferences()
    ) {
        val itemCount = indexList.size

        if (!isDownloaderAvailable()) return

        mutableDownloaderState.update { State.DownloadingPlaylist() }

        currentJob =
            applicationScope.launch(Dispatchers.IO) {
                for (i in indexList.indices) {
                    mutableDownloaderState.update {
                        if (it is State.DownloadingPlaylist)
                            it.copy(currentItem = i + 1, itemCount = indexList.size)
                        else return@launch
                    }

                    NotificationUtil.updateServiceNotificationForPlaylist(
                        index = i + 1, itemCount = itemCount)

                    val playlistIndex = indexList[i]
                    val playlistEntry = playlistItemList.getOrNull(i)

                    Log.d(TAG, playlistEntry?.title.toString())

                    val title = playlistEntry?.title

                    DownloadUtil.fetchVideoInfoFromUrl(
                            url = url, playlistIndex = playlistIndex, preferences = preferences)
                        .onSuccess {
                            if (downloaderState.value !is State.DownloadingPlaylist) return@launch
                            downloadResultTemp =
                                downloadVideo(
                                        videoInfo = it,
                                        playlistIndex = playlistIndex,
                                        playlistUrl = url,
                                        preferences = preferences,
                                    )
                                    .onFailure { th ->
                                        manageDownloadError(
                                            th = th,
                                            url = it.originalUrl,
                                            title = it.title,
                                            isFetchingInfo = false,
                                            isTaskAborted = false)
                                    }
                        }
                        .onFailure { th ->
                            manageDownloadError(
                                th = th,
                                url = playlistEntry?.url,
                                title = title,
                                isFetchingInfo = true,
                                isTaskAborted = false)
                        }
                }
                finishProcessing()
            }
    }

    private fun finishProcessing() {
        if (downloaderState.value is State.Idle) return
        mutableTaskState.update { it.copy(progress = 100f, progressText = "") }
        clearProgressState(isFinished = true)
        updateState(State.Idle)
        clearErrorState()
    }

    /**
     * @param isTaskAborted Determines if the download task is aborted due to the given `Exception`
     */
    fun manageDownloadError(
        th: Throwable,
        url: String?,
        title: String? = null,
        isFetchingInfo: Boolean,
        isTaskAborted: Boolean = true,
        notificationId: Int? = null,
    ) {
        if (th is YoutubeDL.CanceledException) return
        th.printStackTrace()
        val resId =
            if (isFetchingInfo) R.string.fetch_info_error_msg else R.string.download_error_msg
        ToastUtil.makeToastSuspend(context.getString(resId))

        val notificationTitle = title ?: url

        if (isFetchingInfo) {
            fetchInfoError(url = url.toString(), errorReport = th.message.toString())
        } else {
            downloadError(url = url.toString(), errorReport = th.message.toString())
        }

        notificationId?.let {
            NotificationUtil.finishNotification(
                notificationId = notificationId,
                title = notificationTitle,
                text = context.getString(R.string.download_error_msg),
            )
        }
        if (isTaskAborted) {
            updateState(State.Idle)
            clearProgressState(isFinished = false)
        }
    }

    fun cancelDownload() {
        ToastUtil.makeToast(context.getString(R.string.task_canceled))
        currentJob?.cancel(CancellationException(context.getString(R.string.task_canceled)))
        updateState(State.Idle)
        clearProgressState(isFinished = false)
        taskState.value.taskId.run {
            YoutubeDL.destroyProcessById(this)
            NotificationUtil.cancelNotification(this.toNotificationId())
        }
    }

    fun executeCommandWithUrl(url: String) =
        applicationScope.launch(Dispatchers.IO) { DownloadUtil.executeCommandInBackground(url) }

    fun openDownloadResult() {
        if (taskState.value.progress == 100f) FileUtil.openFileFromResult(downloadResultTemp)
    }

    fun onProcessStarted() = mutableProcessCount.update { it + 1 }

    fun String.toNotificationId(): Int = this.hashCode()
}
