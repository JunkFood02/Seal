package com.junkfood.seal

import com.junkfood.seal.Downloader.State
import com.junkfood.seal.util.PlaylistResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object StateHolder {

    val mutableDownloaderState: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val mutableTaskState = MutableStateFlow(DownloadTaskItem())
    val mutablePlaylistResult = MutableStateFlow(PlaylistResult())

    val taskState = mutableTaskState.asStateFlow()
    val downloaderState = mutableDownloaderState.asStateFlow()
    val playlistResult = mutablePlaylistResult.asStateFlow()



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
}