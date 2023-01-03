package com.junkfood.seal.ui.page.download

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.Downloader
import com.junkfood.seal.Downloader.DownloadTaskItem
import com.junkfood.seal.Downloader.State
import com.junkfood.seal.Downloader.manageDownloadError
import com.junkfood.seal.Downloader.mutablePlaylistResult
import com.junkfood.seal.Downloader.mutableTaskState
import com.junkfood.seal.Downloader.showErrorMessage
import com.junkfood.seal.Downloader.taskState
import com.junkfood.seal.R
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil.openFile
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.FORMAT_SELECTION
import com.junkfood.seal.util.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalMaterialApi::class)

// TODO: Refactoring for introducing multitasking and download queue management
class DownloadViewModel @Inject constructor() : ViewModel() {


    private val mutableViewStateFlow = MutableStateFlow(ViewState())
    val viewStateFlow = mutableViewStateFlow.asStateFlow()

    val videoInfoFlow = MutableStateFlow(VideoInfo())

    data class ViewState(
        val showPlaylistSelectionDialog: Boolean = false,
        val url: String = "",
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true
        ),
        val showDownloadSettingDialog: Boolean = false,
        val showFormatSelectionPage: Boolean = false,
        val isUrlSharingTriggered: Boolean = false,
        val debugMode: Boolean = false
    )

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) =
        mutableViewStateFlow.update {
            it.copy(
                url = url, isUrlSharingTriggered = isUrlSharingTriggered
            )
        }

    fun hideDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            if (isDialog) mutableViewStateFlow.update { it.copy(showDownloadSettingDialog = false) }
            else viewStateFlow.value.drawerState.hide()
        }
    }

    fun showDialog(scope: CoroutineScope, isDialog: Boolean) {
        scope.launch {
            if (isDialog) mutableViewStateFlow.update { it.copy(showDownloadSettingDialog = true) }
            else viewStateFlow.value.drawerState.show()
        }
    }

    fun startDownloadVideo() {
        val url = viewStateFlow.value.url

        if (!PreferenceUtil.isNetworkAvailableForDownload()) {
            showErrorMessage(context.getString(R.string.download_disabled_with_cellular))
            return
        }
        if (PreferenceUtil.getValue(CUSTOM_COMMAND)) {
            applicationScope.launch(Dispatchers.IO) { executeCustomCommand() }
            return
        }
        if (url.isBlank()) {
            showErrorMessage(context.getString(R.string.url_empty))
            return
        }
        if (PreferenceUtil.getValue(PreferenceUtil.PLAYLIST)) {
            parsePlaylistInfo(url)
            return
        }

        if (PreferenceUtil.getValue(FORMAT_SELECTION, true)) {
            viewModelScope.launch(Dispatchers.IO) { fetchInfoForFormatSelection(url) }
            return
        }

        currentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!checkStateBeforeDownload()) return@launch
            try {
                fetchVideoInfo(url = url)?.let {
                    downloadVideo(videoInfo = it)
                }
            } catch (e: Exception) {
                manageDownloadError(e)
                return@launch
            }
        }
    }


    private suspend fun executeCustomCommand() {
        DownloadUtil.executeCommandInBackground(viewStateFlow.value.url)
    }


    private fun fetchInfoForFormatSelection(url: String) {
        Downloader.updateState(State.FetchingInfo)
        try {
            DownloadUtil.fetchVideoInfoFromUrl(url = url).run {
                if (formats.isNullOrEmpty()) {
                    throw Exception(context.getString(R.string.fetch_info_error_msg))
                }
                showFormatSelectionPage(this)
            }
        } catch (e: Exception) {
            manageDownloadError(e)
        }
        Downloader.updateState(State.Idle)
    }



    private fun parsePlaylistInfo(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!Downloader.checkStateBeforeDownload()) return@launch

            Downloader.run {
                clearErrorState()
                updateState(State.FetchingInfo)
            }

            try {
                val info = DownloadUtil.getPlaylistOrVideoInfo(url)
                Downloader.updateState(State.Idle)
                when (info) {
                    is PlaylistResult -> {
                        showPlaylistPage(info)
                    }

                    is VideoInfo -> {
                        if (PreferenceUtil.getValue(FORMAT_SELECTION, true)) {
                            showFormatSelectionPage(info)
                        } else if (Downloader.checkStateBeforeDownload()) {
                            Downloader.downloadVideo(videoInfo = info)
                        }
                    }
                }
            } catch (e: Exception) {
                manageDownloadError(e)
            }
        }

    }


    fun openVideoFile() {
        if (taskState.value.progress == 100f) openFile(downloadResultTemp)
    }

    private fun showPlaylistPage(playlistResult: PlaylistResult) {
        mutablePlaylistResult.update { playlistResult }
        mutableViewStateFlow.update {
            it.copy(
                showPlaylistSelectionDialog = true,
            )
        }
    }

    private fun showFormatSelectionPage(info: VideoInfo) {
        videoInfoFlow.update { info }
        mutableViewStateFlow.update {
            it.copy(
                showFormatSelectionPage = true,
            )
        }

    }

    fun hidePlaylistDialog() {
        mutableViewStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun hideFormatPage() {
        mutableViewStateFlow.update { it.copy(showFormatSelectionPage = false) }
    }

    fun onShareIntentConsumed() {
        mutableViewStateFlow.update { it.copy(isUrlSharingTriggered = false) }
    }

    fun clearPlaylistResult() {
        mutablePlaylistResult.update { PlaylistResult() }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}