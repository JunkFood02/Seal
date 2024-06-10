package com.junkfood.seal.ui.page.downloadv2

import androidx.lifecycle.ViewModel
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.Downloader
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadDialogViewModel : ViewModel() {

    sealed interface State {
        data object Hidden : State
        data object Configure : State
        data object Loading : State
        data class PlaylistSelection(val result: PlaylistResult) : State
        data class FormatSelection(val info: VideoInfo) : State
        data class Error(val throwable: Throwable) : State
    }

    sealed interface Action {
        data object Hide : Action
        data object Show : Action
        data class FetchPlaylist(val url: String) : Action
        data object DownloadItemsWithPreset : Action
        data class FetchFormats(val url: String, val audioOnly: Boolean) : Action
        data class DownloadWithInfo(val url: String, val info: VideoInfo) : Action
        data class DownloadWithPreset(val url: String, val audioOnly: Boolean) : Action
        data class RunCommand(val url: String, val template: CommandTemplate) : Action
        data object Cancel : Action
    }

    private val mStateFlow: MutableStateFlow<State> = MutableStateFlow(State.Hidden)
    val stateFlow = mStateFlow.asStateFlow()
    val state get() = stateFlow.value

    fun postAction(action: Action) {
        with(action) {
            when (this) {
                is Action.FetchFormats -> fetchFormat(action = this)
                is Action.FetchPlaylist -> fetchPlaylist(url = url)
                is Action.DownloadWithPreset -> downloadWithPreset(url= url)
                is Action.RunCommand -> runCommand(url, template)
                Action.Hide -> hideDialog()
                Action.Show -> showDialog()
                Action.DownloadItemsWithPreset -> TODO()
                is Action.DownloadWithInfo -> TODO()
                Action.Cancel -> TODO()
            }
        }
    }

    private fun fetchPlaylist(url: String) {
        mStateFlow.update { State.Loading }
        // TODO: handle downloader state
//        Downloader.updateState(Downloader.State.FetchingInfo)
        Downloader.clearErrorState()

        // TODO: Make it cancellable
        DownloadUtil.getPlaylistOrVideoInfo(playlistURL = url).onSuccess { info ->
            when (info) {
                is PlaylistResult -> {
                    mStateFlow.update { State.PlaylistSelection(result = info) }
                }

                is VideoInfo -> {
                    mStateFlow.update { State.FormatSelection(info = info) }
                }
            }
        }.onFailure {
//            Downloader.manageDownloadError(
//                th = it, url = url, isFetchingInfo = true, isTaskAborted = true
//            )
            // TODO: Handle error state
        }
    }

    private fun fetchFormat(action: Action.FetchFormats) {
        mStateFlow.update { State.Loading }
        // TODO: handle downloader state

        DownloadUtil.fetchVideoInfoFromUrl(url = action.url).onSuccess { info ->
            mStateFlow.update { State.FormatSelection(info = info) }
        }.onFailure {
            // TODO: Handle error state
        }

    }

    private fun downloadWithPreset(url: String) {
        Downloader.getInfoAndDownload(url)
    }

    private fun runCommand(url: String, template: CommandTemplate) {
        applicationScope.launch(Dispatchers.IO) {
            DownloadUtil.executeCommandInBackground(
                url = url,
                template = template
            )
        }
        hideDialog()
    }

    private fun hideDialog() {
        mStateFlow.update { State.Hidden }
    }

    private fun showDialog() {
        mStateFlow.update { State.Configure }
    }


}