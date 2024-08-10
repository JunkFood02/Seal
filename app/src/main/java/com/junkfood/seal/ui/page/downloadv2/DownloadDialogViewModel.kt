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

    sealed interface SelectionState {
        data object Idle : SelectionState

        data class PlaylistSelection(val result: PlaylistResult) : SelectionState

        data class FormatSelection(val info: VideoInfo) : SelectionState
    }

    sealed interface SheetState {
        data object Configure : SheetState

        data class Loading(val taskKey: String, val job: Job) : SheetState

        data class Error(val throwable: Throwable) : SheetState
    }

    sealed interface SheetValue {
        data object Expanded : SheetValue

        data object Hidden : SheetValue
    }

    sealed interface Action {
        data object HideSheet : Action

        data object ShowSheet : Action

        data object Reset : Action

        data class FetchPlaylist(
            val url: String,
            val preferences: DownloadUtil.DownloadPreferences
        ) : Action

        data class DownloadItemsWithPreset(
            val url: String,
            val indexList: List<Int>,
            val playlistItemList: List<Entries> = emptyList(),
            val preferences: DownloadUtil.DownloadPreferences =
                DownloadUtil.DownloadPreferences.createFromPreferences()
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

    private val mSelectionStateFlow: MutableStateFlow<SelectionState> =
        MutableStateFlow(SelectionState.Idle)
    private val mSheetStateFlow: MutableStateFlow<SheetState> =
        MutableStateFlow(SheetState.Configure)
    private val mSheetValueFlow: MutableStateFlow<SheetValue> = MutableStateFlow(SheetValue.Hidden)

    val selectionStateFlow = mSelectionStateFlow.asStateFlow()
    val sheetStateFlow = mSheetStateFlow.asStateFlow()
    val sheetValueFlow = mSheetValueFlow.asStateFlow()

    private val sheetState
        get() = sheetStateFlow.value

    fun postAction(action: Action) {
        with(action) {
            when (this) {
                is Action.FetchFormats -> fetchFormat(url, preferences)
                is Action.FetchPlaylist -> fetchPlaylist(url)
                is Action.DownloadWithPreset -> downloadWithPreset(url)
                is Action.RunCommand -> runCommand(url, template)
                Action.HideSheet -> hideDialog()
                Action.ShowSheet -> showDialog()
                is Action.DownloadItemsWithPreset -> TODO()
                is Action.DownloadWithInfoAndConfiguration -> TODO()
                Action.Cancel -> cancel()
                Action.Reset -> resetStates()
            }
        }
    }

    private fun fetchPlaylist(url: String) {
        // TODO: handle downloader state
        Downloader.clearErrorState()

        val job =
            viewModelScope.launch(Dispatchers.IO) {
                DownloadUtil.getPlaylistOrVideoInfo(playlistURL = url)
                    .onSuccess { info ->
                        withContext(Dispatchers.Main) {
                            when (info) {
                                is PlaylistResult -> {
                                    mSelectionStateFlow.update {
                                        SelectionState.PlaylistSelection(result = info)
                                    }
                                }

                                is VideoInfo -> {
                                    mSelectionStateFlow.update {
                                        SelectionState.FormatSelection(info = info)
                                    }
                                }
                            }
                        }
                    }
                    .onFailure { th -> mSheetStateFlow.update { SheetState.Error(th) } }
            }
        mSheetStateFlow.update { SheetState.Loading(taskKey = "FetchPlaylist_$url", job = job) }
    }

    private fun fetchFormat(url: String, preferences: DownloadUtil.DownloadPreferences) {

        val job =
            viewModelScope.launch(Dispatchers.IO) {
                DownloadUtil.fetchVideoInfoFromUrl(
                        url = url, preferences = preferences, taskKey = "FetchFormat_$url")
                    .onSuccess { info ->
                        withContext(Dispatchers.Main) {
                            mSelectionStateFlow.update {
                                SelectionState.FormatSelection(info = info)
                            }
                            hideDialog()
                        }
                    }
                    .onFailure { th ->
                        withContext(Dispatchers.Main) {
                            mSheetStateFlow.update { SheetState.Error(th) }
                        }
                    }
            }

        mSheetStateFlow.update { SheetState.Loading(taskKey = "FetchFormat_$url", job = job) }
    }

    private fun downloadWithPreset(url: String) {
        Downloader.getInfoAndDownload(url)
    }

    private fun downloadPlaylistItemsWithPreset(url: String) {}

    private fun downloadWithInfoAndConfiguration(info: VideoInfo) {}

    private fun runCommand(url: String, template: CommandTemplate) {
        applicationScope.launch(Dispatchers.IO) {
            DownloadUtil.executeCommandInBackground(url = url, template = template)
        }
    }

    private fun hideDialog() {
        mSheetValueFlow.update { SheetValue.Hidden }
        when (sheetState) {
            is SheetState.Error -> {
                resetStates()
            }
            is SheetState.Loading -> {
                cancel()
            }
            else -> {}
        }
    }

    private fun showDialog() {
        mSheetValueFlow.update { SheetValue.Expanded }
    }

    private fun cancel(): Boolean {
        return when (val state = sheetState) {
            is SheetState.Loading -> {
                val res = YoutubeDL.destroyProcessById(id = state.taskKey)
                if (res) {
                    state.job.cancel()
                    resetStates()
                }
                return res
            }
            else -> false
        }
    }

    private fun resetStates() {
        mSheetStateFlow.update { SheetState.Configure }
        mSelectionStateFlow.update { SelectionState.Idle }
        mSheetValueFlow.update { SheetValue.Hidden }
    }
}
