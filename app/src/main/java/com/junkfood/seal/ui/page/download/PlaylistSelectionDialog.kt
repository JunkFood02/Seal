@file:OptIn(ExperimentalMaterial3Api::class)

package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.PlaylistItem


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistSelectionDialog(downloadViewModel: DownloadViewModel) {
    val viewState = downloadViewModel.stateFlow.collectAsState().value
    val onDismissRequest = { downloadViewModel.hidePlaylistDialog() }
    val playlistItemCount = viewState.downloadItemCount
    val playlistInfo = downloadViewModel.playlistResult.collectAsState().value

    var error by remember { mutableStateOf(false) }
    val (item1, item2) = remember { FocusRequester.createRefs() }

    if (viewState.showPlaylistSelectionDialog) {

        AlertDialog(onDismissRequest = {}, icon = { Icon(Icons.Outlined.PlaylistPlay, null) },
            title = { Text(stringResource(R.string.download_range_selection)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.download_range_desc).format(
                            1,
                            playlistItemCount,
                            playlistInfo.title,
                        )
                    )


                    LazyColumn {
                        itemsIndexed(items = playlistInfo.entries) { index, entries ->
                            PlaylistItem(
                                imageModel = entries.thumbnails.lastOrNull()?.url ?: "",
                                title = entries.title.toString(),
                                author = entries.uploader.toString()
                            )
                        }
                    }
                }
            },
            dismissButton = {
                DismissButton {
                    onDismissRequest()
                    MainActivity.stopService()
                }
            },
            confirmButton = {
                TextButton(onClick = {

                    downloadViewModel.downloadVideoInPlaylistByIndexList()
                    onDismissRequest()

                }) {
                    Text(text = stringResource(R.string.start_download))
                }
            })
    }
}