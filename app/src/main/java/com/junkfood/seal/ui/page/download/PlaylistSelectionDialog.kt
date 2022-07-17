package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.isNumberInRange


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistSelectionDialog(downloadViewModel: DownloadViewModel) {

    val viewState = downloadViewModel.stateFlow.collectAsState().value
    val onDismissRequest = { downloadViewModel.hidePlaylistDialog() }
    val playlistItemCount = viewState.downloadItemCount
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
                            playlistItemCount
                        )
                    )
                    Row(modifier = Modifier.padding(top = 12.dp)) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        ) {
                            OutlinedTextField(modifier = Modifier
                                .focusable()
                                .focusProperties { next = item2 }
                                .focusRequester(item1),
                                value = from,
                                onValueChange = {
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
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        ) {
                            OutlinedTextField(modifier = Modifier
                                .focusable()
                                .focusProperties { previous = item1 }
                                .focusRequester(item2),
                                value = to,
                                onValueChange = {
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
/*                            Text(
                                stringResource(R.string.max_value).format(playlistItemCount),
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )*/
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