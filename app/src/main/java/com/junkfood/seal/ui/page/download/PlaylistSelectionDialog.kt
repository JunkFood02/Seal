package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.isNumberInRange


@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun PlaylistSelectionDialog(playlistItemCount: Int = 16) {

    val downloadViewModel: DownloadViewModel = hiltViewModel()
    val viewState = downloadViewModel.stateFlow.collectAsState().value
    val onDismissRequest = { downloadViewModel.hidePlaylistDialog() }

    var from by remember { mutableStateOf(1.toString()) }
    var to by remember { mutableStateOf(playlistItemCount.toString()) }

    var error by remember { mutableStateOf(false) }
    val (item1, item2) = remember { FocusRequester.createRefs() }

    if (viewState.showPlaylistSelectionDialog) {
        AlertDialog(onDismissRequest = { },
            title = { Text(stringResource(R.string.download_range_selection)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.download_range_desc).format(
                            playlistItemCount
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
                }
            },
            dismissButton = { DismissButton { onDismissRequest() } },
            confirmButton = {
                TextButton(onClick = {
                    error = !from.isNumberInRange(1, playlistItemCount) or !to.isNumberInRange(
                        1, playlistItemCount
                    ) || from.toInt() > to.toInt()
                    if (error) TextUtil.makeToast(R.string.invalid_index_range)
                }) {
                    Text(text = stringResource(R.string.start_download))
                }
            })
    }
}