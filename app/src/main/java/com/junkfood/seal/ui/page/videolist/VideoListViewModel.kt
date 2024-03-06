package com.junkfood.seal.ui.page.videolist

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.BackupUtil.toJson
import com.junkfood.seal.util.BackupUtil.toURLs
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val TAG = "VideoListViewModel"

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class VideoListViewModel @Inject constructor() : ViewModel() {


    private val mutableStateFlow = MutableStateFlow(VideoListViewState())
    val stateFlow = mutableStateFlow.asStateFlow()
    private val viewState get() = stateFlow.value

    private val _mediaInfoFlow = DatabaseUtil.getDownloadHistoryFlow()

    val videoListFlow: Flow<List<DownloadedVideoInfo>> =
        _mediaInfoFlow.map { it.reversed().sortedBy { info -> info.filterByType() } }

    val searchedVideoListFlow = videoListFlow.combine(stateFlow) { list, state ->
        if (!state.isSearching || state.searchText.isBlank()) list
        else list.filter {
            state.searchText.let { text ->
                with(it) {
                    videoTitle.contains(text, ignoreCase = true)
                            || videoAuthor.contains(text, ignoreCase = true)
                            || extractor.contains(text, ignoreCase = true)
                            || videoPath.contains(text, ignoreCase = true)
                }
            }
        }
    }

    val filterSetFlow = searchedVideoListFlow.map { infoList ->
        mutableSetOf<String>().apply {
            infoList.forEach {
                this.add(it.extractor)
            }
        }
    }


    fun clickVideoFilter() {
        if (mutableStateFlow.value.videoFilter) mutableStateFlow.update { it.copy(videoFilter = false) }
        else mutableStateFlow.update { it.copy(videoFilter = true, audioFilter = false) }
    }

    fun clickAudioFilter() {
        if (mutableStateFlow.value.audioFilter) mutableStateFlow.update { it.copy(audioFilter = false) }
        else mutableStateFlow.update { it.copy(audioFilter = true, videoFilter = false) }
    }

    fun clickExtractorFilter(index: Int) {
        if (mutableStateFlow.value.activeFilterIndex == index) mutableStateFlow.update {
            it.copy(
                activeFilterIndex = -1
            )
        }
        else mutableStateFlow.update { it.copy(activeFilterIndex = index) }
    }

    fun toggleSearch(isSearching: Boolean = !viewState.isSearching) {
        mutableStateFlow.update { it.copy(isSearching = isSearching, searchText = "") }
    }

    fun updateSearchText(text: String) {
        mutableStateFlow.update { it.copy(searchText = text) }
    }

    fun List<DownloadedVideoInfo>.backupToString(
        type: BackupType,
    ): String {
        return if (type == BackupType.Full) toJson() else toURLs()
    }

    @Composable
    fun String.backupTo(destination: BackupDestination): Result<Unit> {
        val clipboardManager = LocalClipboardManager.current
        return when (destination) {
            BackupDestination.File -> {
                FileUtil.createTextFile(
                    fileName = FileUtil.getDownloadHistoryExportFilename(),
                    fileContent = this
                )
            }

            BackupDestination.Clipboard -> {
                runCatching { clipboardManager.setText(AnnotatedString(this)) }
            }
        }
    }

    data class VideoListViewState(
        val activeFilterIndex: Int = -1,
        val videoFilter: Boolean = false,
        val audioFilter: Boolean = false,
        val isSearching: Boolean = false,
        val searchText: String = "",
    )

    enum class BackupType {
        Full, URL
    }

    enum class BackupDestination {
        File, Clipboard
    }

}