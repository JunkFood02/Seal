package com.junkfood.seal.ui.page.videolist

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.util.DatabaseUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class VideoListViewModel @Inject constructor() : ViewModel() {
    data class VideoListViewState constructor(
        val listFlow: Flow<List<DownloadedVideoInfo>> = DatabaseUtil.getInfo(),
    )

    data class VideoDetailViewState(
        val title: String = "",
        val author: String = "",
        val url: String = "",
        val path: String = "",
        var drawerState: ModalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    ) {
        constructor(info: DownloadedVideoInfo) : this(
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


    private val _detailViewState = MutableStateFlow(VideoDetailViewState())
    private val _viewState = MutableStateFlow(VideoListViewState())
    val viewState = _viewState.asStateFlow()
    val detailViewState = _detailViewState.asStateFlow()
}