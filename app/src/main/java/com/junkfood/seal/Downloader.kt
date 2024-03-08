package com.junkfood.seal

import android.app.PendingIntent
import android.util.Log
import androidx.annotation.CheckResult
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.App.Companion.startService
import com.junkfood.seal.App.Companion.stopService
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.util.COMMAND_DIRECTORY
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.VideoClip
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import kotlin.math.roundToInt


/**
 * Singleton Downloader for state holder & perform downloads, used by `Activity` & `Service`
 */
object Downloader {

    private const val TAG = "Downloader"

    sealed class State {
        data class DownloadingPlaylist(
            val currentItem: Int = 0,
            val itemCount: Int = 0,
        ) : State()

        object DownloadingVideo : State()
        object FetchingInfo : State()
        object Idle : State()
    }

    data class ErrorState(
        val errorReport: String = "",
        val errorMessageResId: Int = R.string.unknown_error,
    ) {
        fun isErrorOccurred(): Boolean =
            errorMessageResId != R.string.unknown_error || errorReport.isNotEmpty()
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
    private val mutableErrorState = MutableStateFlow(ErrorState())
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
            downloaderState.combine(processCount) { state, cnt ->
                if (cnt > 0) true
                else when (state) {
                    is State.Idle -> false
                    else -> true
                }
            }.combine(mutableQuickDownloadCount) { isRunning, cnt ->
                if (!isRunning) cnt > 0 else true
            }.collect {
                if (it) startService()
                else stopService()
            }

        }
    }

    fun isDownloaderAvailable(): Boolean {
        if (downloaderState.value !is State.Idle) {
            ToastUtil.makeToastSuspend(context.getString(R.string.task_running))
            return false
        }
        return true
    }


    fun makeKey(url: String, templateName: String): String = "${templateName}_$url"

    fun onTaskStarted(template: CommandTemplate, url: String) =
        CustomCommandTask(
            template = template,
            url = url,
            output = "",
            state = CustomCommandTask.State.Running(0f),
            currentLine = ""
        ).run {
            mutableTaskList.put(this.toKey(), this)
        }


    fun updateTaskOutput(template: CommandTemplate, url: String, line: String, progress: Float) {
        val key = makeKey(url, template.name)
        val oldValue = mutableTaskList[key] ?: return
        val newValue = oldValue.run {
            copy(
                output = output + line + "\n",
                currentLine = line,
                state = CustomCommandTask.State.Running(progress)
            )
        }
        mutableTaskList[key] = newValue
    }


    fun onTaskEnded(
        template: CommandTemplate,
        url: String,
        response: String? = null
    ) {
        val key = makeKey(url, template.name)
        NotificationUtil.finishNotification(
            notificationId = key.toNotificationId(),
            title = key,
            text = context.getString(R.string.status_completed),
        )
        mutableTaskList.run {
            val oldValue = get(key) ?: return
            val newValue = oldValue.copy(state = CustomCommandTask.State.Completed).run {
                response?.let { copy(output = response) } ?: this
            }
            this[key] = newValue
        }
        FileUtil.scanDownloadDirectoryToMediaLibrary(COMMAND_DIRECTORY.getString())
    }


    fun onProcessEnded() =
        mutableProcessCount.update { it - 1 }


    fun onProcessCanceled(taskId: String) =
        mutableTaskList.run {
            get(taskId)?.let {
                this.put(
                    taskId,
                    it.copy(state = CustomCommandTask.State.Canceled)
                )
            }
        }

    fun onTaskError(errorReport: String, template: CommandTemplate, url: String) =
        mutableTaskList.run {
            val key = makeKey(url, template.name)
            NotificationUtil.makeErrorReportNotification(
                notificationId = key.toNotificationId(),
                error = errorReport
            )
            val oldValue = mutableTaskList[key] ?: return
            mutableTaskList[key] = oldValue.copy(
                state = CustomCommandTask.State.Error(
                    errorReport
                ), currentLine = errorReport, output = oldValue.output + "\n" + errorReport
            )
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
            playlistIndex = playlistIndex
        )

    fun updateState(state: State) = mutableDownloaderState.update { state }

    fun clearErrorState() {
        mutableErrorState.update { ErrorState() }
    }

    fun showErrorMessage(resId: Int) {
        ToastUtil.makeToastSuspend(context.getString(resId))
        mutableErrorState.update { ErrorState(errorMessageResId = resId) }
    }

    private fun clearProgressState(isFinished: Boolean) {
        mutableTaskState.update {
            it.copy(
                progress = if (isFinished) 100f else 0f,
                progressText = "",
            )
        }
        if (!isFinished)
            downloadResultTemp = Result.failure(Exception())
    }

    fun updatePlaylistResult(playlistResult: PlaylistResult = PlaylistResult()) =
        mutablePlaylistResult.update { playlistResult }

    fun quickDownload(
        url: String,
        downloadPreferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ) {
        applicationScope.launch(Dispatchers.IO) {
            mutableQuickDownloadCount.update { it + 1 }
            DownloadUtil.fetchVideoInfoFromUrl(
                url = url,
                preferences = downloadPreferences
            )
                .onFailure {
                    manageDownloadError(
                        it,
                        isFetchingInfo = true,
                        isTaskAborted = true
                    )
                }
                .onSuccess { videoInfo ->
                    val taskId = videoInfo.id + downloadPreferences.hashCode()
                    val notificationId = taskId.toNotificationId()
                    ToastUtil.makeToastSuspend(
                        context.getString(R.string.download_start_msg)
                            .format(videoInfo.title)
                    )
                    DownloadUtil.downloadVideo(
                        videoInfo = videoInfo,
                        downloadPreferences = downloadPreferences,
                        taskId = taskId
                    ) { progress, _, line ->
                        NotificationUtil.notifyProgress(
                            notificationId = notificationId,
                            progress = progress.toInt(),
                            text = line,
                            title = videoInfo.title,
                            taskId = taskId
                        )
                    }.onFailure {
                        NotificationUtil.cancelNotification(notificationId)
                        if (it is YoutubeDL.CanceledException) return@onFailure
                        NotificationUtil.makeErrorReportNotification(
                            title = videoInfo.title, notificationId = notificationId,
                            error = it.message.toString()
                        )
                    }.onSuccess {
                        val text =
                            context.getString(if (it.isEmpty()) R.string.status_completed else R.string.download_finish_notification)

                        FileUtil.createIntentForOpeningFile(it.firstOrNull()).run {
                            NotificationUtil.finishNotification(
                                notificationId,
                                title = videoInfo.title,
                                text = text,
                                intent = if (this != null) PendingIntent.getActivity(
                                    context,
                                    0,
                                    this,
                                    PendingIntent.FLAG_IMMUTABLE
                                ) else null
                            )
                        }
                    }
                }
            mutableQuickDownloadCount.update { it - 1 }
        }
    }

    fun getInfoAndDownload(
        url: String,
        downloadPreferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ) {
        currentJob = applicationScope.launch(Dispatchers.IO) {
            updateState(State.FetchingInfo)
            DownloadUtil.fetchVideoInfoFromUrl(
                url = url,
                preferences = downloadPreferences
            )
                .onFailure {
                    manageDownloadError(
                        it,
                        isFetchingInfo = true,
                        isTaskAborted = true
                    )
                }
                .onSuccess { info ->
                    downloadResultTemp = downloadVideo(
                        videoInfo = info,
                        preferences = downloadPreferences
                    )
                }
        }
    }

    /**
     * Triggers a download with extra configurations made by user in the custom format selection page
     */
    fun downloadVideoWithConfigurations(
        videoInfo: VideoInfo,
        formatList: List<Format>,
        videoClips: List<VideoClip>,
        splitByChapter: Boolean,
        newTitle: String,
        selectedSubtitleCodes: List<String>,
    ) {
        currentJob = applicationScope.launch(Dispatchers.IO) {
            val fileSize = formatList.fold(.0) { acc, format ->
                acc + (format.fileSize ?: format.fileSizeApprox ?: .0)
            }

            val info = videoInfo
                .run { if (fileSize != .0) copy(fileSize = fileSize) else this }
                .run { if (newTitle.isNotEmpty()) copy(title = newTitle) else this }

            val audioOnly =
                formatList.isNotEmpty() && formatList.fold(true) { acc: Boolean, format: Format ->
                    acc && (format.vcodec == "none" && format.acodec != "none")
                }

            val mergeAudioStream = formatList.count { format ->
                format.vcodec == "none" && format.acodec != "none"
            } > 1

            val formatId = formatList.joinToString(separator = "+") { it.formatId.toString() }

            val downloadPreferences = DownloadUtil.DownloadPreferences(
                formatIdString = formatId,
                videoClips = videoClips,
                splitByChapter = splitByChapter,
                newTitle = newTitle,
                mergeAudioStream = mergeAudioStream
            ).run {
                copy(extractAudio = extractAudio || audioOnly)
            }.run {
                selectedSubtitleCodes.takeIf { it.isNotEmpty() }
                    ?.let {
                        val autoSubtitle = !info.subtitles.keys.containsAll(selectedSubtitleCodes)
                        copy(
                            downloadSubtitle = true,
                            autoSubtitle = autoSubtitle,
                            subtitleLanguage = selectedSubtitleCodes.joinToString(separator = ",") { it }
                        )
                    }
                    ?: this
            }
            downloadResultTemp = downloadVideo(
                videoInfo = info,
                preferences = downloadPreferences
            )
        }
    }

    fun downloadVideoWithInfo(info: VideoInfo) {
        currentJob = applicationScope.launch(Dispatchers.IO) {
            downloadResultTemp = downloadVideo(videoInfo = info)
        }
    }

    /**
     * This method is used for download a single video and multiple videos from playlist at the same time.
     * @see downloadVideoInPlaylistByIndexList
     * @see getInfoAndDownload
     * @see downloadVideoWithConfigurations
     */
    @CheckResult
    private suspend fun downloadVideo(
        playlistIndex: Int = 0,
        playlistUrl: String = "",
        videoInfo: VideoInfo,
        preferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ): Result<List<String>> {

        Log.d(TAG, preferences.subtitleLanguage)
        mutableTaskState.update { videoInfo.toTask(preferencesHash = preferences.hashCode()) }

        val isDownloadingPlaylist = downloaderState.value is State.DownloadingPlaylist
        if (!isDownloadingPlaylist)
            updateState(State.DownloadingVideo)
        val taskId = videoInfo.id + preferences.hashCode()
        val notificationId = taskId.toNotificationId()
        Log.d(TAG, "downloadVideo: id=${videoInfo.id} " + videoInfo.title)
        Log.d(TAG, "notificationId: $notificationId")

//        TextUtil.makeToastSuspend(
//            context.getString(R.string.download_start_msg).format(videoInfo.title)
//        )

        NotificationUtil.notifyProgress(
            notificationId = notificationId, title = videoInfo.title
        )
        return DownloadUtil.downloadVideo(
            videoInfo = videoInfo,
            playlistUrl = playlistUrl,
            playlistItem = playlistIndex,
            downloadPreferences = preferences,
            taskId = videoInfo.id + preferences.hashCode()
        ) { progress, _, line ->
            Log.d(TAG, line)
            mutableTaskState.update {
                it.copy(progress = progress, progressText = line)
            }
            NotificationUtil.notifyProgress(
                notificationId = notificationId,
                progress = progress.toInt(),
                text = line,
                title = videoInfo.title
            )
        }.onFailure {
            manageDownloadError(
                it,
                false,
                notificationId = notificationId,
                isTaskAborted = !isDownloadingPlaylist
            )
        }.onSuccess {
            if (!isDownloadingPlaylist) finishProcessing()
            val text =
                context.getString(if (it.isEmpty()) R.string.status_completed else R.string.download_finish_notification)
            FileUtil.createIntentForOpeningFile(it.firstOrNull()).run {
                NotificationUtil.finishNotification(
                    notificationId,
                    title = videoInfo.title,
                    text = text,
                    intent = if (this != null) PendingIntent.getActivity(
                        context,
                        0,
                        this,
                        PendingIntent.FLAG_IMMUTABLE
                    ) else null
                )
            }
        }
    }

    fun downloadVideoInPlaylistByIndexList(
        url: String,
        indexList: List<Int>,
        preferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ) {
        val itemCount = indexList.size

        if (!isDownloaderAvailable()) return

        mutableDownloaderState.update { State.DownloadingPlaylist() }

        currentJob = applicationScope.launch(Dispatchers.IO) {
            for (i in indexList.indices) {
                mutableDownloaderState.update {
                    if (it is State.DownloadingPlaylist)
                        it.copy(currentItem = i + 1, itemCount = indexList.size)
                    else return@launch
                }

                NotificationUtil.updateServiceNotification(
                    index = i + 1, itemCount = itemCount
                )

                val playlistIndex = indexList[i]

                DownloadUtil.fetchVideoInfoFromUrl(
                    url = url,
                    playlistItem = playlistIndex,
                    preferences = preferences
                ).onSuccess {
                    if (downloaderState.value !is State.DownloadingPlaylist)
                        return@launch
                    downloadResultTemp =
                        downloadVideo(
                            videoInfo = it,
                            playlistIndex = playlistIndex,
                            playlistUrl = url,
                            preferences = preferences,
                        ).onFailure { th ->
                            manageDownloadError(
                                th,
                                isFetchingInfo = false,
                                isTaskAborted = false
                            )
                        }
                }.onFailure { th ->
                    manageDownloadError(
                        th,
                        isFetchingInfo = true,
                        isTaskAborted = false
                    )
                }
            }
            finishProcessing()
        }
    }

    private fun finishProcessing() {
        if (downloaderState.value is State.Idle) return
        mutableTaskState.update {
            it.copy(progress = 100f, progressText = "")
        }
        clearProgressState(isFinished = true)
        updateState(State.Idle)
        clearErrorState()
    }

    /**
     * @param isTaskAborted Determines if the download task is aborted due to the given `Exception`
     */
    fun manageDownloadError(
        th: Throwable,
        isFetchingInfo: Boolean,
        isTaskAborted: Boolean = true,
        notificationId: Int? = null,
    ) {
        if (th is YoutubeDL.CanceledException) return
        th.printStackTrace()
        val resId =
            if (isFetchingInfo) R.string.fetch_info_error_msg else R.string.download_error_msg
        ToastUtil.makeToastSuspend(context.getString(resId))

        mutableErrorState.update {
            ErrorState(
                errorReport = th.message.toString()
            )
        }
        notificationId?.let {
            NotificationUtil.finishNotification(
                notificationId = notificationId,
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
        applicationScope.launch(Dispatchers.IO) {
            DownloadUtil.executeCommandInBackground(
                url
            )
        }

    fun openDownloadResult() {
        if (taskState.value.progress == 100f) FileUtil.openFileFromResult(downloadResultTemp)
    }

    fun onProcessStarted() = mutableProcessCount.update { it + 1 }
    fun String.toNotificationId(): Int = this.hashCode()
}


