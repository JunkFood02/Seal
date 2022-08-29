package com.junkfood.seal.ui.page.videolist

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.DatabaseUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

const val AUDIO_REGEX = "(\\.mp3)|(\\.aac)|(\\.opus)|(\\.m4a)"
private const val TAG = "VideoListViewModel"

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class VideoListViewModel @Inject constructor() : ViewModel() {
    private val mediaInfoFlow: Flow<List<DownloadedVideoInfo>> =
        DatabaseUtil.getMediaInfo()
    val filterSetFlow = DatabaseUtil.getMediaInfo()
        .map { infoList -> mutableSetOf<String>().apply { infoList.forEach { this.add(it.extractor) } } }

    private val mutableStateFlow = MutableStateFlow(VideoListViewState())
    val stateFlow = mutableStateFlow.asStateFlow()

    val videoListFlow = mediaInfoFlow
    fun clickVideoFilter() {
        if (mutableStateFlow.value.videoFilter)
            mutableStateFlow.update { it.copy(videoFilter = false) }
        else
            mutableStateFlow.update { it.copy(videoFilter = true, audioFilter = false) }
    }

    fun clickAudioFilter() {
        if (mutableStateFlow.value.audioFilter)
            mutableStateFlow.update { it.copy(audioFilter = false) }
        else
            mutableStateFlow.update { it.copy(audioFilter = true, videoFilter = false) }
    }

    fun clickExtractorFilter(index: Int) {
        if (mutableStateFlow.value.activeFilterIndex == index)
            mutableStateFlow.update { it.copy(activeFilterIndex = -1) }
        else
            mutableStateFlow.update { it.copy(activeFilterIndex = index) }
    }

    data class VideoListViewState(
        val activeFilterIndex: Int = -1,
        val videoFilter: Boolean = false,
        val audioFilter: Boolean = false
    )

    data class VideoDetailViewState(
        val id: Int = 0,
        val title: String = "",
        val author: String = "",
        val url: String = "",
        val path: String = "",
        val drawerState: ModalBottomSheetState = ModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            isSkipHalfExpanded = true
        ),
        val showDialog: Boolean = false,
    ) {
        constructor(info: DownloadedVideoInfo) : this(
            info.id,
            info.videoTitle,
            info.videoAuthor,
            info.videoUrl,
            info.videoPath
        )
    }

    fun hideDrawer(scope: CoroutineScope): Boolean {
        if (_detailViewState.value.drawerState.isVisible) {
            scope.launch {
                _detailViewState.value.drawerState.hide()
            }
            return true
        }
        return false
    }

    fun showDrawer(scope: CoroutineScope, item: DownloadedVideoInfo) {
        scope.launch {
            _detailViewState.update {
                VideoDetailViewState(item)
            }
            _detailViewState.value.drawerState.show()
        }
    }

    fun showDialog() {
        _detailViewState.update { it.copy(showDialog = true) }
    }

    fun hideDialog() {
        _detailViewState.update { it.copy(showDialog = false) }
    }

    fun removeItem(delete: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (delete) {
                    val info = DatabaseUtil.getInfoById(_detailViewState.value.id)
                    File(info.videoPath).delete()
                }
                DatabaseUtil.deleteInfoById(_detailViewState.value.id)
            }
        }
    }

    private val _detailViewState = MutableStateFlow(VideoDetailViewState())
    val detailViewState = _detailViewState.asStateFlow()
}