package com.junkfood.seal

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
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
    )

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
    val mutableTaskState = MutableStateFlow(DownloadTaskItem())
    val mutablePlaylistResult = MutableStateFlow(PlaylistResult())
    private val mutableErrorState = MutableStateFlow(ErrorState())

    val taskState = mutableTaskState.asStateFlow()
    val downloaderState = mutableDownloaderState.asStateFlow()
    val playlistResult = mutablePlaylistResult.asStateFlow()
    val errorState = mutableErrorState.asStateFlow()

    init {
        App.applicationScope.launch {
            downloaderState.collect {
                when (it) {
                    is State.Idle -> App.stopService()
                    else -> App.startService()
                }
            }
        }
    }

    fun checkStateBeforeDownload(): Boolean {
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

    fun clearProgressState(isFinished: Boolean) {
        mutableTaskState.update {
            it.copy(
                progress = if (isFinished) 100f else 0f,
                progressText = "",
            )
        }
    }

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
            }
                .onFailure { manageDownloadError(it, isFetchingInfo = true) }
                .onSuccess {
                    runCatching {
                        downloadVideo(
                            videoInfo = it,
                            downloadPreferences = downloadPreferences
                        )
                    }.onFailure { manageDownloadError(it, isFetchingInfo = false) }
                        .onSuccess { downloadResultTemp = it }
                }
        }
    }

    fun downloadVideoWithFormatId(videoInfo: VideoInfo, formatList: List<Format>) {
        viewModelScope.launch(Dispatchers.IO) {
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
            updateDownloadTask(
                info,
                DownloadTaskItem()
            )
            kotlin.runCatching {
                downloadVideo(
                    videoInfo = info,
                    downloadPreferences = downloadPreferences
                )
            }.onFailure { manageDownloadError(it) }
        }
    }

    /**
     * This method is used for download a single video and multiple videos from playlist at the same time.
     * @see downloadVideoInPlaylistByIndexList
     * @see getInfoAndDownload
     * @see downloadVideoWithFormatId
     */
    fun downloadVideo(
        playlistIndex: Int = 0,
        playlistUrl: String = "",
        videoInfo: VideoInfo,
        downloadPreferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences()
    ): Result<List<String>> {

        with(downloaderState) {
            if (value !is State.DownloadingPlaylist)
                updateState(State.DownloadingVideo)

            Log.d(TAG, "downloadVideo: ${videoInfo.id}" + videoInfo.title)

            val notificationId = videoInfo.id.hashCode()
            Log.d(TAG, notificationId.toString())
            val intent: Intent?
            applicationScope.launch(Dispatchers.Main) {
                TextUtil.makeToast(
                    context.getString(R.string.download_start_msg).format(videoInfo.title)
                )
            }

            NotificationUtil.notifyProgress(
                notificationId = notificationId, title = videoInfo.title
            )
            try {
                val downloadResult = DownloadUtil.downloadVideo(
                    videoInfo = videoInfo,
                    playlistUrl = playlistUrl,
                    playlistItem = playlistIndex,
                    downloadPreferences = downloadPreferences
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
                }
                intent = FileUtil.createIntentForOpenFile(downloadResult)
                if (value !is State.DownloadingPlaylist) finishProcessing()
                NotificationUtil.finishNotification(
                    notificationId,
                    title = videoInfo.title,
                    text = context.getString(R.string.download_finish_notification),
                    intent = if (intent != null) PendingIntent.getActivity(
                        App.context,
                        0,
                        FileUtil.createIntentForOpenFile(downloadResult),
                        PendingIntent.FLAG_IMMUTABLE
                    ) else null
                )

            } catch (e: Exception) {
                manageDownloadError(e, false, notificationId)
                return Result.failure(e)
            }
        }

    }

    fun downloadVideoInPlaylistByIndexList(
        playlistResult: PlaylistResult = Downloader.playlistResult.value,
        url: String = playlistResult.webpageUrl.toString(),
        indexList: List<Int>
    ) {
        currentJob = applicationScope.launch(Dispatchers.IO) {
            val itemCount = indexList.size
            if (!checkStateBeforeDownload()) return@launch
            Downloader.updateState(
                State.DownloadingPlaylist(
                    currentItem = 1,
                    itemCount = itemCount
                )
            )

            for (i in indexList.indices) {
                if (downloaderState.value !is State.DownloadingPlaylist) break
                with(playlistResult) {
                    val task = DownloadTaskItem(
                        webpageUrl = url,
                        title = title.toString(),
                        uploader = uploader ?: channel ?: "null",
                        playlistIndex = indexList[i]
                    )
                    downloaderState.value.run {
                        if (this is State.DownloadingPlaylist)
                            Downloader.updateState(copy(currentItem = i + 1))
                    }

                    if (App.isServiceRunning) NotificationUtil.updateServiceNotification(
                        index = i + 1, itemCount = itemCount
                    )
                    fetchVideoInfo(url, task)?.let {
                        downloadVideo(
                            playlistIndex = task.playlistIndex,
                            playlistUrl = playlistResult.webpageUrl ?: "",
                            videoInfo = it
                        )
                    }
                }
            }
            finishProcessing()
        }
    }

    fun finishProcessing() {
        if (downloaderState.value is State.Idle) return
        mutableTaskState.update {
            it.copy(progress = 100f, progressText = "")
        }
        updateState(State.Idle)
        clearErrorState()
        mutablePlaylistResult.update { PlaylistResult() }
    }

    fun manageDownloadError(
        th: Throwable, isFetchingInfo: Boolean = true, notificationId: Int? = null
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

    }

    fun cancelDownload() {
        TextUtil.makeToast(context.getString(R.string.task_canceled))
        currentJob?.cancel(CancellationException(context.getString(R.string.task_canceled)))
        updateState(State.Idle)
        clearProgressState(isFinished = false)
        mutablePlaylistResult.update { PlaylistResult() }

        taskState.value.taskId.run {
            YoutubeDL.destroyProcessById(this)
            NotificationUtil.cancelNotification(this.hashCode())
        }

    }
}


