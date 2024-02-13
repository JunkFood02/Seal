package com.junkfood.seal.ui.page.videolist

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.DatabaseUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "VideoListViewModel"

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class VideoListViewModel @Inject constructor() : ViewModel() {


    private val mutableStateFlow = MutableStateFlow(VideoListViewState())
    val stateFlow = mutableStateFlow.asStateFlow()
    private val viewState get() = stateFlow.value

    private val _mediaInfoFlow = DatabaseUtil.getMediaInfo()

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

    init {
        viewModelScope.launch {
            _mediaInfoFlow.collect { list ->
                if (list.isEmpty()) {
                    mutableStateFlow.update { it.copy(isVideoListNotEmpty = false) }
                }
            }
        }
    }

    val filterSetFlow = _mediaInfoFlow.map { infoList ->
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

    data class VideoListViewState(
        val activeFilterIndex: Int = -1,
        val videoFilter: Boolean = false,
        val audioFilter: Boolean = false,
        val isSearching: Boolean = false,
        val searchText: String = "",
        val isVideoListNotEmpty: Boolean = true,
    )

}