package com.junkfood.seal.ui.page

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.VideoInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object StateHolder {

    val mutableStateFlow = MutableStateFlow(DownloadViewState())
    val mutableTaskState = MutableStateFlow(DownloadTaskItem())
    val mutablePlaylistResult = MutableStateFlow(PlaylistResult())
    val taskState = mutableTaskState.asStateFlow()
    val stateFlow = mutableStateFlow.asStateFlow()
    val playlistResult = mutablePlaylistResult.asStateFlow()

    data class DownloadViewState @OptIn(ExperimentalMaterialApi::class) constructor(
        val showDownloadProgress: Boolean = false,
        val showPlaylistSelectionDialog: Boolean = false,
        val url: String = "",
        val isDownloadError: Boolean = false,
        val errorMessage: String = "",
        val isInCustomCommandMode: Boolean = false,
        val isFetchingInfo: Boolean = false,
        val isProcessRunning: Boolean = false,
        val isCancelled: Boolean = false,
        val debugMode: Boolean = false,
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true
        ),
        val showDownloadSettingDialog: Boolean = false,
        val isDownloadingPlaylist: Boolean = false,
        val downloadItemCount: Int = 0,
        val currentItem: Int = 0,
        val isUrlSharingTriggered: Boolean = false,
        val isShowingErrorReport: Boolean = false
    )


    data class DownloadTaskItem(
        var videoInfo: VideoInfo? = null,
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