package com.junkfood.seal.download

import com.junkfood.seal.download.Task.ViewState
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.roundToInt

@Serializable
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

    @Serializable
    data class State(
        val downloadState: DownloadState,
        val videoInfo: VideoInfo?,
        val viewState: ViewState,
    )

    @Serializable
    sealed interface DownloadState : Comparable<DownloadState> {

        interface Cancelable {
            val job: Job
            val taskId: String
            val action: RestartableAction
        }

        interface Restartable {
            val action: RestartableAction
        }

        @Serializable data object Idle : DownloadState

        @Serializable
        data class FetchingInfo(
            @Transient override val job: Job = Job(),
            override val taskId: String,
        ) : DownloadState, Cancelable {
            override val action: RestartableAction = RestartableAction.FetchInfo
        }

        @Serializable data object ReadyWithInfo : DownloadState

        @Serializable
        data class Running(
            @Transient override val job: Job = Job(),
            override val taskId: String,
            val progress: Float = PROGRESS_INDETERMINATE,
            val progressText: String = "",
        ) : DownloadState, Cancelable {
            override val action: RestartableAction = RestartableAction.Download
        }

        @Serializable
        data class Canceled(override val action: RestartableAction, val progress: Float? = null) :
            DownloadState, Restartable

        @Serializable
        data class Error(
            @Transient val throwable: Throwable = Throwable(),
            override val action: RestartableAction,
        ) : DownloadState, Restartable

        @Serializable data class Completed(val filePath: String?) : DownloadState

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

    @Serializable
    sealed interface RestartableAction {
        @Serializable data object FetchInfo : RestartableAction

        @Serializable data object Download : RestartableAction
    }

    @Serializable
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

                val videoFormats = formats.filter { it.containsVideo() }
                val audioOnlyFormats = formats.filter { it.isAudioOnly() }

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
