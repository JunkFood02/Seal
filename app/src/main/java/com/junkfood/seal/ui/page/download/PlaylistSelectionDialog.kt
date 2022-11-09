@file:OptIn(ExperimentalMaterial3Api::class)

package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.PlaylistItem
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.isNumberInRange


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistSelectionDialog(downloadViewModel: DownloadViewModel) {
    val viewState = downloadViewModel.stateFlow.collectAsState().value
    val onDismissRequest = { downloadViewModel.hidePlaylistDialog() }
    val playlistItemCount = viewState.downloadItemCount
    val playlistInfo = viewState.playlistInfo
    var from by remember { mutableStateOf(1.toString()) }
    var to by remember { mutableStateOf(viewState.downloadItemCount.toString()) }

    var error by remember { mutableStateOf(false) }
    val (item1, item2) = remember { FocusRequester.createRefs() }

    if (viewState.showPlaylistSelectionDialog) {
        from = "1"
        to = viewState.downloadItemCount.toString()
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
                    Row(modifier = Modifier.padding(top = 12.dp)) {

                        OutlinedTextField(modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                            .focusable()
                            .focusProperties { next = item2 }
                            .focusRequester(item1),
                            value = from,
                            onValueChange = {
                                if (it.isDigitsOnly())
                                    from = it
                                error = false
                            },
                            label = { Text(stringResource(R.string.from)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = error
                        )

                        OutlinedTextField(modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                            .focusable()
                            .focusProperties { previous = item1 }
                            .focusRequester(item2),
                            value = to,
                            onValueChange = {
                                if (it.isDigitsOnly())
                                    to = it
                                error = false
                            },
                            label = { Text(stringResource(R.string.to)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = error
                        )
                    }

                    LazyColumn {
                        itemsIndexed(items = playlistInfo.entries) {index,entries->
                            PlaylistItem(
                                imageModel = entries.thumbnails.lastOrNull()?.url ?: "",
                                index = index,
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
                    error = !from.isNumberInRange(1, playlistItemCount) or !to.isNumberInRange(
                        1, playlistItemCount
                    ) || from.toInt() > to.toInt()
                    if (error) TextUtil.makeToast(R.string.invalid_index_range)
                    else {
                        downloadViewModel.downloadVideoInPlaylistByIndexRange(indexRange = from.toInt()..to.toInt())
                        onDismissRequest()
                    }
                }) {
                    Text(text = stringResource(R.string.start_download))
                }
            })
    }
}