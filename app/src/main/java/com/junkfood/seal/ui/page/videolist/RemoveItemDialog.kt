package com.junkfood.seal.ui.page.videolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.MultiChoiceItem
import com.junkfood.seal.ui.component.SealDialog

@Composable
fun RemoveItemDialog(
    videoListViewModel: VideoListViewModel = hiltViewModel(),
) {
    var deleteFile by remember { mutableStateOf(false) }
    val detailState = videoListViewModel.detailViewState.collectAsState()
    if (detailState.value.showDialog) {
//        deleteFile.value = false
        SealDialog(onDismissRequest = { videoListViewModel.hideDialog() },
            title = {
                Text(text = stringResource(R.string.delete_info))
            }, icon = { Icon(Icons.Outlined.Delete, null) },
            text = {
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        text = stringResource(R.string.delete_info_msg)
                            .format(detailState.value.title),// textAlign = TextAlign.Center
                    )
                    MultiChoiceItem(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = stringResource(R.string.delete_file),
                        checked = deleteFile
                    ) { deleteFile = !deleteFile }
                }

            }, confirmButton = {
                TextButton(onClick = {
                    videoListViewModel.hideDialog()
                    videoListViewModel.removeItem(deleteFile)
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