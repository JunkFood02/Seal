package com.junkfood.seal.download

import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import kotlin.math.roundToInt
import kotlinx.coroutines.Job

data class Task(
    val info: VideoInfo?,
    val url: String,
    val playlistIndex: Int? = null,
    val preferences: DownloadUtil.DownloadPreferences,
    val viewState: ViewState = ViewState.create(info, url),
    val id: String = makeId(url, playlistIndex, preferences),
) {
    constructor(
        url: String,
        preferences: DownloadUtil.DownloadPreferences,
    ) : this(info = null, url = url, preferences = preferences)

    constructor(
        info: VideoInfo,
        preferences: DownloadUtil.DownloadPreferences,
    ) : this(info = info, url = info.originalUrl.toString(), preferences = preferences)

    sealed interface State : Comparable<State> {

        interface Cancelable {
            val job: Job
            val taskId: String
            val action: RestartableAction
        }

        interface Restartable {
            val action: RestartableAction
        }

        data object Idle : State

        data class FetchingInfo(override val job: Job, override val taskId: String) :
            State, Cancelable {
            override val action: RestartableAction = RestartableAction.FetchInfo
        }

        data object ReadyWithInfo : State

        data class Running(
            override val job: Job,
            override val taskId: String,
            val progress: Float = PROGRESS_INDETERMINATE,
            val progressText: String = "",
        ) : State, Cancelable {
            override val action: RestartableAction = RestartableAction.Download
        }

        data class Canceled(
            override val action: RestartableAction,
            val progress: Float? = null,
        ) : State, Restartable

        data class Error(
            val throwable: Throwable,
            override val action: RestartableAction,
        ) : State, Restartable

        data class Completed(val filePath: String?) : State

        override fun compareTo(other: State): Int {
            return ordinal - other.ordinal
        }

        private val ordinal: Int
            get() =
                when (this) {
                    is Canceled -> 6
                    is Error -> 5
                    is Completed -> 4
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
        val url: String = "",
        val title: String = "",
        val uploader: String = "",
        val duration: Int = 0,
        val fileSizeApprox: Double = .0,
        val thumbnailUrl: String? = null,
    ) {

        constructor(
            info: VideoInfo
        ) : this(
            url = info.originalUrl.toString(),
            title = info.title,
            uploader = info.uploader ?: info.channel ?: info.uploaderId.toString(),
            duration = info.duration?.roundToInt() ?: 0,
            thumbnailUrl = info.thumbnail.toHttpsUrl(),
            fileSizeApprox = info.fileSize ?: info.fileSizeApprox ?: .0,
        )

        companion object {
            fun create(info: VideoInfo?, url: String) =
                if (info != null) ViewState(info) else ViewState(url = url, title = url)
        }
    }

    companion object {
        private const val PROGRESS_INDETERMINATE = -1f

        fun Task.attachInfo(info: VideoInfo): Task =
            this.copy(info = info, viewState = ViewState(info))

        private fun makeId(
            url: String,
            playlistIndex: Int?,
            preferences: DownloadUtil.DownloadPreferences,
        ): String = "${url}_${playlistIndex}_${preferences.hashCode()}"
    }
}
