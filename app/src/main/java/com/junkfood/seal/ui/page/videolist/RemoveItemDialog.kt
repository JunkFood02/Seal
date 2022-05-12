package com.junkfood.seal.ui.page.videolist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R

@Composable
fun RemoveItemDialog(
    videoListViewModel: VideoListViewModel = hiltViewModel(),
) {
    AlertDialog(onDismissRequest = { videoListViewModel.hideDialog() }, icon = {
        Icon(
            Icons.Outlined.Delete,
            contentDescription = "delete"
        )
    }, title = {
        Text(text = stringResource(R.string.delete_info))
    }, text = { Text(text = stringResource(R.string.delete_info_msg)) }, confirmButton = {
        TextButton(onClick = { videoListViewModel.removeItem() }) {
            Text(text = stringResource(R.string.confirm))
        }
    }, dismissButton = {
        TextButton(onClick = { videoListViewModel.hideDialog() }) {
            Text(text = stringResource(R.string.dismiss))
        }
    })
}