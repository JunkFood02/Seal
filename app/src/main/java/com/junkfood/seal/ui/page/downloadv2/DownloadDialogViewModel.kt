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
        data object Hidden : State
        data object Configure : State
        data class Loading(val taskKey: String, val job: Job) : State
        data class PlaylistSelection(val result: PlaylistResult) : State
        data class FormatSelection(val info: VideoInfo) : State
        data class Error(val throwable: Throwable) : State
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

    private val mStateFlow: MutableStateFlow<State> = MutableStateFlow(State.Hidden)
    val stateFlow = mStateFlow.asStateFlow()
    val state get() = stateFlow.value

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
        mStateFlow.update { State.Loading(taskKey = "FetchPlaylist_$url", job = job) }
    }

    private fun fetchFormat(url: String, preferences: DownloadUtil.DownloadPreferences) {
        // TODO: handle downloader state

        Downloader.isDownloaderAvailable()

        val job = viewModelScope.launch(Dispatchers.IO) {
            DownloadUtil.fetchVideoInfoFromUrl(url = url, preferences = preferences)
                .onSuccess { info ->
                    withContext(Dispatchers.Main) {
                        mStateFlow.update { State.FormatSelection(info = info) }
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                    }
                    // TODO: Handle error state
                }
        }

        mStateFlow.update { State.Loading(taskKey = "FetchFormat_$url", job = job) }


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
        mStateFlow.update { State.Hidden }
    }

    private fun showDialog() {
        mStateFlow.update { State.Configure }
    }

    private fun cancel(): Boolean {
        val res = when (val state = state) {
            is State.Loading -> {
                state.job.cancel()
                YoutubeDL.destroyProcessById(id = state.taskKey)
            }

            else -> false
        }

        if (res) {
            mStateFlow.update { State.Configure }
        }
        return res
    }

}