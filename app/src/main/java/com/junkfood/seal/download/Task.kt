package com.junkfood.seal.download

import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.VideoInfo
import kotlinx.coroutines.Job

data class Task(
    val url: String,
    val info: VideoInfo? = null,
    val preferences: DownloadUtil.DownloadPreferences,
    val id: String
) {
    sealed interface State {

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
    }

    sealed interface RestartableAction {
        data object FetchInfo : RestartableAction

        data object Download : RestartableAction
    }

    companion object {
        const val PROGRESS_INDETERMINATE = -1f

        fun createWithURL(
            url: String,
            preferences: DownloadUtil.DownloadPreferences =
                DownloadUtil.DownloadPreferences.createFromPreferences()
        ): Task {
            return Task(url = url, preferences = preferences, id = makeId(url, preferences))
        }

        fun createWithInfo(
            info: VideoInfo,
            url: String = info.originalUrl.toString(),
            preferences: DownloadUtil.DownloadPreferences =
                DownloadUtil.DownloadPreferences.createFromPreferences()
        ): Task {
            return Task(
                url = url, preferences = preferences, info = info, id = makeId(url, preferences))
        }

        private fun makeId(url: String, preferences: DownloadUtil.DownloadPreferences): String =
            "${url}_${preferences.hashCode()}"
    }
}
