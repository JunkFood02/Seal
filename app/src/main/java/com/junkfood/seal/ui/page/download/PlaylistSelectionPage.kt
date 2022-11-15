package com.junkfood.seal.ui.page.download

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun PlaylistSelectionPage(downloadViewModel: DownloadViewModel) {
    val viewState = downloadViewModel.stateFlow.collectAsState().value
    val onDismissRequest = { downloadViewModel.hidePlaylistDialog() }
    val playlistInfo = downloadViewModel.playlistResult.collectAsState().value
    var error by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<Int>() }
    if (viewState.showPlaylistSelectionDialog) {

    }
}



