package com.junkfood.seal.ui.page.downloadv2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.Downloader
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.Entries
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadDialogViewModel : ViewModel() {

    sealed interface State {
        data object Idle : State
        data object ShowDialog : State
        data class PlaylistSelection(val result: PlaylistResult) : State
        data class FormatSelection(val info: VideoInfo) : State
    }

    sealed interface DialogState {
        data object Configure : DialogState
        data class Loading(val taskKey: String, val job: Job) : DialogState
        data class Error(val throwable: Throwable) : DialogState
    }

    sealed interface Action {
        data object Hide : Action
        data object Show : Action

        data class FetchPlaylist(
            val url: String,
            val preferences: DownloadUtil.DownloadPreferences
        ) : Action

        data class DownloadItemsWithPreset(
            val url: String,
            val indexList: List<Int>,
            val playlistItemList: List<Entries> = emptyList(),
            val preferences: DownloadUtil.DownloadPreferences = DownloadUtil.DownloadPreferences.createFromPreferences()
        ) : Action

        data class FetchFormats(
            val url: String,
            val audioOnly: Boolean,
            val preferences: DownloadUtil.DownloadPreferences
        ) : Action

        data class DownloadWithInfoAndConfiguration(
            val url: String,
            val info: VideoInfo,
            val preferences: DownloadUtil.DownloadPreferences
        ) : Action

        data class DownloadWithPreset(
            val url: String,
            val preferences: DownloadUtil.DownloadPreferences
        ) : Action

        data class RunCommand(val url: String, val template: CommandTemplate) : Action

        data object Cancel : Action
    }

    private val mStateFlow: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    private val mDialogStateFlow: MutableStateFlow<DialogState> =
        MutableStateFlow(DialogState.Configure)
    val dialogStateFlow = mDialogStateFlow.asStateFlow()
    val stateFlow = mStateFlow.asStateFlow()
    private val state get() = stateFlow.value
    private val dialogState get() = dialogStateFlow.value

    fun postAction(action: Action) {
        with(action) {
            when (this) {
                is Action.FetchFormats -> fetchFormat(url, preferences)
                is Action.FetchPlaylist -> fetchPlaylist(url)
                is Action.DownloadWithPreset -> downloadWithPreset(url)
                is Action.RunCommand -> runCommand(url, template)
                Action.Hide -> hideDialog()
                Action.Show -> showDialog()
                is Action.DownloadItemsWithPreset -> TODO()
                is Action.DownloadWithInfoAndConfiguration -> TODO()
                Action.Cancel -> TODO()
            }
        }
    }

    private fun fetchPlaylist(url: String) {
        // TODO: handle downloader state
        Downloader.clearErrorState()

        val job = viewModelScope.launch(Dispatchers.IO) {
            DownloadUtil.getPlaylistOrVideoInfo(playlistURL = url).onSuccess { info ->
                withContext(Dispatchers.Main) {
                    when (info) {
                        is PlaylistResult -> {
                            mStateFlow.update { State.PlaylistSelection(result = info) }
                        }

                        is VideoInfo -> {
                            mStateFlow.update { State.FormatSelection(info = info) }
                        }
                    }
                }
            }.onFailure {
//            Downloader.manageDownloadError(
//                th = it, url = url, isFetchingInfo = true, isTaskAborted = true
//            )
                // TODO: Handle error state
            }
        }
        mDialogStateFlow.update { DialogState.Loading(taskKey = "FetchPlaylist_$url", job = job) }
    }

    private fun fetchFormat(url: String, preferences: DownloadUtil.DownloadPreferences) {
        if (!Downloader.isDownloaderAvailable()) {
            postAction(Action.Hide)
            return
        }

        val job = viewModelScope.launch(Dispatchers.IO) {
            DownloadUtil.fetchVideoInfoFromUrl(url = url, preferences = preferences)
                .onSuccess { info ->
                    withContext(Dispatchers.Main) {
                        mStateFlow.update { State.FormatSelection(info = info) }
                    }
                }.onFailure { th ->
                    withContext(Dispatchers.Main) {
                        mDialogStateFlow.update { DialogState.Error(th) }
                    }
                }
        }

        mDialogStateFlow.update { DialogState.Loading(taskKey = "FetchFormat_$url", job = job) }
    }

    private fun downloadWithPreset(url: String) {
        Downloader.getInfoAndDownload(url)
    }

    private fun downloadPlaylistItemsWithPreset(url: String) {

    }

    private fun downloadWithInfoAndConfiguration(info: VideoInfo) {

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
        mStateFlow.update { State.Idle }
    }

    private fun showDialog() {
        mStateFlow.update { State.ShowDialog }
    }

    private fun cancel(): Boolean {
        val res = when (val state = dialogState) {
            is DialogState.Loading -> {
                state.job.cancel()
                YoutubeDL.destroyProcessById(id = state.taskKey)
            }

            else -> false
        }

        if (res) {
            mDialogStateFlow.update { DialogState.Configure }
        }
        return res
    }

}