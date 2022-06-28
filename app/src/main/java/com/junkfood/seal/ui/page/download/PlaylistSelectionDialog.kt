package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun PlaylistSelectionDialog(playlistItemCount: Int = 16) {

    val downloadViewModel: DownloadViewModel = hiltViewModel()
    var showPlaylistSelectionDialog by remember { mutableStateOf(true) }
    val onDismissRequest = { showPlaylistSelectionDialog = false }
    var from by remember { mutableStateOf(1.toString()) }
    var to by remember { mutableStateOf(playlistItemCount.toString()) }
    var error by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val (item1, item2) = remember { FocusRequester.createRefs() }

    if (showPlaylistSelectionDialog)
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.download_range_selection)) },
            text = {
                Column() {
                    Text(text = stringResource(id = R.string.download_range_desc))
                    Row(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                                .focusRequester(item1)
                                .focusProperties { next = item2 }
                                .focusable(),
                            value = from,
                            onValueChange = {
                                from = it
                                error = false
                            },
                            label = { Text("From") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            keyboardActions = KeyboardActions(onNext = {
                                focusManager.moveFocus(FocusDirection.Next)
                            })

                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                                .focusable()
                                .focusRequester(item2),
                            value = to,
                            onValueChange = {
                                to = it
                                error = false
                            },
                            label = { Text("To") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })

                        )
                    }
                }
            },
            dismissButton = { DismissButton { onDismissRequest() } },
            confirmButton = {
                TextButton(onClick = {
                    error = true
                }) {
                    Text(text = stringResource(R.string.start_download))
                }
            })
}