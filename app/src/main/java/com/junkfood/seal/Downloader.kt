package com.junkfood.seal

import android.app.PendingIntent
import android.util.Log
import androidx.annotation.CheckResult
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.toHttpsUrl
import com.junkfood.seal.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val errorMessage: String = "",
    ) {
        fun isErrorOccurred(): Boolean = errorMessage.isNotEmpty() || errorReport.isNotEmpty()
    }

    data class DownloadTaskItem(
        val webpageUrl: String = "",
        val title: String = "",
        val uploader: String = "",
        val duration: Int = 0,
        val fileSizeApprox: Long = 0,
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

    val taskState = mutableTaskState.asStateFlow()
    val downloaderState = mutableDownloaderState.asStateFlow()
    val playlistResult = mutablePlaylistResult.asStateFlow()
    val errorState = mutableErrorState.asStateFlow()

    init {
        applicationScope.launch {
            downloaderState.collect {
                when (it) {
                    is State.Idle -> App.stopService()
                    else -> App.startService()
                }
            }
        }
    }

    fun isDownloaderAvailable(): Boolean {
        if (downloaderState.value !is State.Idle) {
            TextUtil.makeToastSuspend(context.getString(R.string.task_running))
            return false
        }
        return true
    }


    private fun VideoInfo.toTask(playlistIndex: Int = 0): DownloadTaskItem =
        DownloadTaskItem(
            webpageUrl = webpageUrl.toString(),
            title = title,
            uploader = uploader ?: channel.toString(),
            duration = duration?.roundToInt() ?: 0,
            taskId = id,
            thumbnailUrl = thumbnail.toHttpsUrl(),
            fileSizeApprox = fileSize ?: fileSizeApprox ?: 0,
            playlistIndex = playlistIndex
        )

    fun updateState(state: State) = mutableDownloaderState.update { state }

    fun clearErrorState() {
        mutableErrorState.update { it.copy(errorMessage = "", errorReport = "") }
    }

    fun showErrorMessage(msg: String) {
        mutableErrorState.update { ErrorState(errorReport = "", errorMessage = msg) }
    }

    private fun clearProgressState(isFinished: Boolean) {
        mutableTaskState.update {
            it.copy(
                progress = if (isFinished) 100f else 0f,
                progressText = "",
            )
        }
    }

    fun updatePlaylistResult(playlistResult: PlaylistResult = PlaylistResult()) =
        mutablePlaylistResult.update { playlistResult }

    fun getInfoAndDownload(
        url: String,
        downloadPreferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ) {
        currentJob = applicationScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                DownloadUtil.fetchVideoInfoFromUrl(
                    url = url,
                    preferences = downloadPreferences
                )
            }.onFailure { manageDownloadError(it, isFetchingInfo = true, isTaskAborted = true) }
                .onSuccess {
                    it.onSuccess { info ->
                        downloadResultTemp = downloadVideo(
                            videoInfo = info,
                            preferences = downloadPreferences
                        )
                    }
                }
        }
    }

    fun downloadVideoWithFormatId(videoInfo: VideoInfo, formatList: List<Format>) {
        currentJob = applicationScope.launch(Dispatchers.IO) {
            val fileSize = formatList.fold(0L) { acc, format ->
                acc + (format.fileSize ?: format.fileSizeApprox ?: 0L)
            }

            val info = videoInfo.run { if (fileSize != 0L) copy(fileSize = fileSize) else this }

            val audioOnly =
                formatList.isNotEmpty() && formatList.fold(true) { acc: Boolean, format: Format ->
                    acc && (format.vcodec == "none" && format.acodec != "none")
                }
            val formatId = formatList.fold("") { s, format ->
                s + "+" + format.formatId
            }.removePrefix("+")

            val downloadPreferences = DownloadUtil.DownloadPreferences().run {
                copy(extractAudio = extractAudio || audioOnly, formatId = formatId)
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
     * @see downloadVideoWithFormatId
     */
    @CheckResult
    private suspend fun downloadVideo(
        playlistIndex: Int = 0,
        playlistUrl: String = "",
        videoInfo: VideoInfo,
        preferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ): Result<List<String>> {

        mutableTaskState.update { videoInfo.toTask() }

        val isDownloadingPlaylist = downloaderState.value is State.DownloadingPlaylist
        if (!isDownloadingPlaylist)
            updateState(State.DownloadingVideo)
        val notificationId = videoInfo.id.toNotificationId()
        Log.d(TAG, "downloadVideo: id=${videoInfo.id} " + videoInfo.title)
        Log.d(TAG, "notificationId: $notificationId")

        TextUtil.makeToastSuspend(
            context.getString(R.string.download_start_msg).format(videoInfo.title)
        )

        NotificationUtil.notifyProgress(
            notificationId = notificationId, title = videoInfo.title
        )
        return DownloadUtil.downloadVideo(
            videoInfo = videoInfo,
            playlistUrl = playlistUrl,
            playlistItem = playlistIndex,
            downloadPreferences = preferences
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
        }
            .onSuccess {
                if (!isDownloadingPlaylist) finishProcessing()
                FileUtil.createIntentForFile(it).run {
                    NotificationUtil.finishNotification(
                        notificationId,
                        title = videoInfo.title,
                        text = context.getString(R.string.download_finish_notification),
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
        currentJob = applicationScope.launch(Dispatchers.IO) {
            val itemCount = indexList.size

            if (!isDownloaderAvailable()) return@launch

            for (i in indexList.indices) {
                mutableDownloaderState.update {
                    State.DownloadingPlaylist(currentItem = i + 1, itemCount = indexList.size)
                }

                if (App.isServiceRunning) NotificationUtil.updateServiceNotification(
                    index = i + 1, itemCount = itemCount
                )
                val playlistIndex = indexList[i]

                DownloadUtil.fetchVideoInfoFromUrl(
                    url = url,
                    playlistItem = playlistIndex,
                    preferences = preferences
                ).onSuccess {
                    downloadResultTemp =
                        downloadVideo(
                            videoInfo = it,
                            playlistIndex = playlistIndex, preferences = preferences
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
        mutableErrorState.update {
            it.copy(
                errorMessage = context.getString(if (isFetchingInfo) R.string.fetch_info_error_msg else R.string.download_error_msg),
                errorReport = th.message ?: context.getString(R.string.unknown_error)
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
        TextUtil.makeToast(context.getString(R.string.task_canceled))
        currentJob?.cancel(CancellationException(context.getString(R.string.task_canceled)))
        updateState(State.Idle)
        clearProgressState(isFinished = false)
        taskState.value.taskId.run {
            YoutubeDL.destroyProcessById(this)
            NotificationUtil.cancelNotification(this.toNotificationId())
        }

    }

    fun openDownloadResult() {
        if (taskState.value.progress == 100f) FileUtil.openFile(downloadResultTemp)
    }

    fun String.toNotificationId(): Int = this.hashCode()
}


