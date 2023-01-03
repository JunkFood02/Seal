@file:OptIn(ExperimentalMaterial3Api::class)

package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlaylistAdd
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
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.Downloader
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.TextUtil.isNumberInRange


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistSelectionDialog(
    playlistInfo: PlaylistResult,
    onDismissRequest: () -> Unit = {},
    onConfirm: (IntRange) -> Unit = {}
) {
    val playlistCount = playlistInfo.entries?.size ?: 0
    var from by remember { mutableStateOf(1.toString()) }
    var to by remember { mutableStateOf(playlistCount.toString()) }
    var error by remember { mutableStateOf(false) }
    val (item1, item2) = remember { FocusRequester.createRefs() }

    AlertDialog(onDismissRequest = { onDismissRequest() },
        icon = { Icon(Icons.Outlined.PlaylistAdd, null) },
        title = { Text(stringResource(R.string.download_range_selection)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.download_range_desc).format(
                        1,
                        playlistCount,
                        playlistInfo.title,
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
                }
            }
        },
        dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        },
        confirmButton = {
            ConfirmButton(onClick = {
                error =
                    !from.isNumberInRange(1, playlistCount) or !to.isNumberInRange(
                        1, playlistCount
                    ) || from.toInt() > to.toInt()
                if (error) TextUtil.makeToast(R.string.invalid_index_range)
                else {
                    onConfirm(from.toInt()..to.toInt())
                    onDismissRequest()
                }
            })
        })

}
