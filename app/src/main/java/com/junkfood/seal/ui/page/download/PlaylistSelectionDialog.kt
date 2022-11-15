@file:OptIn(ExperimentalMaterial3Api::class)

package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.PlaylistItem


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistSelectionDialog(downloadViewModel: DownloadViewModel) {
    val viewState = downloadViewModel.stateFlow.collectAsState().value
    val onDismissRequest = {
        downloadViewModel.hidePlaylistDialog()
        MainActivity.stopService()
    }
    val playlistInfo = downloadViewModel.playlistResult.collectAsState().value
    var error by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<Int>() }
    if (viewState.showPlaylistSelectionDialog) {
        val properties = DialogProperties(
            dismissOnClickOutside = false
        )
        AlertDialog(properties = properties, onDismissRequest = { onDismissRequest() },
            icon = { Icon(Icons.Outlined.PlaylistPlay, null) },
            title = { Text(stringResource(R.string.download_range_selection)) },
            text = {
                Column {
                    Text(
                        modifier = Modifier.padding(bottom = 12.dp),
                        text = stringResource(R.string.download_selection_desc).format(playlistInfo.title)
                    )
                    LazyColumn {
                        itemsIndexed(items = playlistInfo.entries) { index, entries ->
                            PlaylistItem(imageModel = entries.thumbnails.lastOrNull()?.url ?: "",
                                title = entries.title ?: index.toString(),
                                author = entries.uploader.toString(),
                                selected = selectedItems.contains(index),
                                onClick = {
                                    if (selectedItems.contains(index)) selectedItems.remove(index)
                                    else selectedItems.add(index)
                                })
                        }
                    }
                }
            },
            dismissButton = {
                DismissButton {
                    onDismissRequest()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedItems.isEmpty()) {
                        error = true
                    }
                    downloadViewModel.downloadVideoInPlaylistByIndexList(indexList = selectedItems)
                    onDismissRequest()
                }) {
                    Text(text = stringResource(R.string.start_download))
                }
            })
    }
}