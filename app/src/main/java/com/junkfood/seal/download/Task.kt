package com.junkfood.seal.download

import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import kotlin.math.roundToInt
import kotlinx.coroutines.Job

data class Task(
    val info: VideoInfo? = null,
    val url: String,
    val preferences: DownloadUtil.DownloadPreferences,
    val viewState: ViewState = ViewState.create(info, url),
    val id: String = makeId(url, preferences)
) {
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

        data class FetchingInfo(
            override val job: Job,
            override val taskId: String,
        ) : State, Cancelable {
            override val action: RestartableAction = RestartableAction.FetchInfo
        }

        data object ReadyWithInfo : State

        data class Running(
            override val job: Job,
            override val taskId: String,
            val progress: Float = PROGRESS_INDETERMINATE,
            val progressText: String = ""
        ) : State, Cancelable {
            override val action: RestartableAction = RestartableAction.Download
        }

        data class Canceled(override val action: RestartableAction) : State, Restartable

        data class Error(val throwable: Throwable, override val action: RestartableAction) :
            State, Restartable

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
        val thumbnailUrl: String = "",
    ) {
        companion object {
            fun create(info: VideoInfo?, url: String) =
                info?.let { fromVideoInfo(it) } ?: fromUrl(url)

            fun fromVideoInfo(info: VideoInfo): ViewState =
                info.run {
                    ViewState(
                        url = originalUrl.toString(),
                        title = title,
                        uploader = uploader ?: channel ?: uploaderId.toString(),
                        duration = duration?.roundToInt() ?: 0,
                        thumbnailUrl = thumbnail.toHttpsUrl(),
                        fileSizeApprox = fileSize ?: fileSizeApprox ?: .0,
                    )
                }

            fun fromUrl(url: String) = ViewState(url = url, title = url)
        }
    }

    companion object {
        private const val PROGRESS_INDETERMINATE = -1f

        fun createWithUrl(url: String, preferences: DownloadUtil.DownloadPreferences) =
            Task(info = null, url = url, preferences = preferences)

        fun createWithInfo(info: VideoInfo, preferences: DownloadUtil.DownloadPreferences) =
            Task(info = info, url = info.originalUrl.toString(), preferences = preferences)

        fun Task.attachInfo(info: VideoInfo): Task =
            this.copy(info = info, viewState = ViewState.fromVideoInfo(info))

        private fun makeId(url: String, preferences: DownloadUtil.DownloadPreferences): String =
            "${url}_${preferences.hashCode()}"
    }
}
