package com.junkfood.seal.ui.page.videolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveItemDialog(
    videoListViewModel: VideoListViewModel = hiltViewModel(),
) {
    val deleteFile = remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = { videoListViewModel.hideDialog() },
        title = {
            Text(text = stringResource(R.string.delete_info))
        }, text = {
            Column() {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.delete_info_msg),// textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { deleteFile.value = !deleteFile.value },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = deleteFile.value,
                        onCheckedChange = { deleteFile.value = it })
                    Text(
                        text = stringResource(R.string.delete_file),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

        }, confirmButton = {
            TextButton(onClick = {
                videoListViewModel.hideDialog()
                videoListViewModel.removeItem(deleteFile.value)
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, dismissButton = {
            TextButton(onClick = { videoListViewModel.hideDialog() }) {
                Text(text = stringResource(R.string.dismiss))
            }
        })
}