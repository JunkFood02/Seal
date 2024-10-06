package com.junkfood.seal.download

import com.junkfood.seal.download.Task.ViewState
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import kotlin.math.roundToInt
import kotlinx.coroutines.Job

data class Task(
    val url: String,
    val playlistIndex: Int? = null,
    val preferences: DownloadUtil.DownloadPreferences,
    val id: String = makeId(url, playlistIndex, preferences),
) : Comparable<Task> {

    val timeCreated: Long = System.currentTimeMillis()

    override fun compareTo(other: Task): Int {
        return timeCreated.compareTo(other.timeCreated)
    }

    data class State(
        val downloadState: DownloadState,
        val videoInfo: VideoInfo?,
        val viewState: ViewState,
    )

    sealed interface DownloadState : Comparable<DownloadState> {

        interface Cancelable {
            val job: Job
            val taskId: String
            val action: RestartableAction
        }

        interface Restartable {
            val action: RestartableAction
        }

        data object Idle : DownloadState

        data class FetchingInfo(override val job: Job, override val taskId: String) :
            DownloadState, Cancelable {
            override val action: RestartableAction = RestartableAction.FetchInfo
        }

        data object ReadyWithInfo : DownloadState

        data class Running(
            override val job: Job,
            override val taskId: String,
            val progress: Float = PROGRESS_INDETERMINATE,
            val progressText: String = "",
        ) : DownloadState, Cancelable {
            override val action: RestartableAction = RestartableAction.Download
        }

        data class Canceled(override val action: RestartableAction, val progress: Float? = null) :
            DownloadState, Restartable

        data class Error(val throwable: Throwable, override val action: RestartableAction) :
            DownloadState, Restartable

        data class Completed(val filePath: String?) : DownloadState

        override fun compareTo(other: DownloadState): Int {
            return ordinal - other.ordinal
        }

        private val ordinal: Int
            get() =
                when (this) {
                    is Canceled -> 4
                    is Error -> 5
                    is Completed -> 6
                    Idle -> 3
                    is FetchingInfo -> 2
                    ReadyWithInfo -> 1
                    is Running -> 0
                }
    }

    sealed interface RestartableAction {
        data object FetchInfo : RestartableAction

        data object Download : RestartableAction
    }

    data class ViewState(
        val url: String = "https://www.example.com",
        val title: String = "",
        val uploader: String = "",
        val extractorKey: String = "",
        val duration: Int = 0,
        val fileSizeApprox: Double = .0,
        val thumbnailUrl: String? = null,
        val videoFormats: List<Format>? = null,
        val audioOnlyFormats: List<Format>? = null,
    ) {
        companion object {
            fun fromVideoInfo(info: VideoInfo): ViewState {
                val formats =
                    info.requestedFormats
                        ?: info.requestedDownloads?.map { it.toFormat() }
                        ?: emptyList()

                val videoFormats = formats.filter { it.vcodec != "none" }
                val audioOnlyFormats = formats.filter { it.acodec != "none" && it.vcodec == "none" }

                return ViewState(
                    url = info.originalUrl.toString(),
                    title = info.title,
                    uploader = info.uploader ?: info.channel ?: info.uploaderId.toString(),
                    extractorKey = info.extractorKey,
                    duration = info.duration?.roundToInt() ?: 0,
                    thumbnailUrl = info.thumbnail.toHttpsUrl(),
                    fileSizeApprox = info.fileSize ?: info.fileSizeApprox ?: .0,
                    videoFormats = videoFormats,
                    audioOnlyFormats = audioOnlyFormats,
                )
            }
        }
    }

    companion object {
        private const val PROGRESS_INDETERMINATE = -1f

        private fun makeId(
            url: String,
            playlistIndex: Int?,
            preferences: DownloadUtil.DownloadPreferences,
        ): String = "${url}_${playlistIndex}_${preferences.hashCode()}"
    }
}
