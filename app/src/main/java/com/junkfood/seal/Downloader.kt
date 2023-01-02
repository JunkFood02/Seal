package com.junkfood.seal

import com.junkfood.seal.util.PlaylistResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Singleton Downloader for state holder & perform downloads, used by `Activity` & `Service`
 */
object Downloader {

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

    private val mutableDownloaderState: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val mutableTaskState = MutableStateFlow(DownloadTaskItem())
    val mutablePlaylistResult = MutableStateFlow(PlaylistResult())
    val mutableErrorState = MutableStateFlow(ErrorState())

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

    fun updateState(state: State) = mutableDownloaderState.update { state }

    fun clearErrorState() {
        mutableErrorState.update { it.copy(errorMessage = "", errorReport = "") }
    }


}


