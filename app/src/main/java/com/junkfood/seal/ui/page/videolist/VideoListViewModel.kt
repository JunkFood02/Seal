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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class VideoListViewModel @Inject constructor() : ViewModel() {

    data class VideoListViewState constructor(
        val videoListFlow: Flow<List<DownloadedVideoInfo>> = DatabaseUtil.getVideoInfo(),
        val audioListFlow: Flow<List<DownloadedVideoInfo>> = DatabaseUtil.getAudioInfo(),
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
                if (delete)
                    with(File(_detailViewState.value.path)) {
                        if (exists() and canWrite()) delete()
                    }
                DatabaseUtil.deleteInfoById(_detailViewState.value.id)
            }
        }

    }

    private val _detailViewState = MutableStateFlow(VideoDetailViewState())
    private val _viewState = MutableStateFlow(VideoListViewState())
    val viewState = _viewState.asStateFlow()
    val detailViewState = _detailViewState.asStateFlow()
}