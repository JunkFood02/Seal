package com.junkfood.seal.ui.page.videolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.MultiChoiceItem

@Composable
fun RemoveItemDialog(
    videoListViewModel: VideoListViewModel = hiltViewModel(),
) {
    val deleteFile = remember { mutableStateOf(false) }
    val detailState = videoListViewModel.detailViewState.collectAsState()
    if (detailState.value.showDialog) {
//        deleteFile.value = false
        AlertDialog(onDismissRequest = { videoListViewModel.hideDialog() },
            title = {
                Text(text = stringResource(R.string.delete_info))
            }, text = {
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        text = stringResource(R.string.delete_info_msg)
                            .format(detailState.value.title),// textAlign = TextAlign.Center
                    )
                    MultiChoiceItem(
                        text = stringResource(R.string.delete_file),
                        checked = deleteFile.value
                    ) { deleteFile.value = !deleteFile.value }
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
}