package com.junkfood.seal.ui.page.settings.directory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SnippetFolder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.page.settings.general.DialogCheckBoxItem

@Composable
fun DirectoryPreferenceDialog(onDismissRequest: () -> Unit = {}, onConfirm: () -> Unit = {}) {
    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton {

            }
        },
        dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        },
        title = {
            Text(
                text = stringResource(
                    id = R.string.subdirectory
                )
            )
        },
        icon = { Icon(imageVector = Icons.Outlined.SnippetFolder, contentDescription = null) },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.subdirectory_desc),
                    modifier = Modifier.padding(horizontal = 24.dp),
//                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                DialogCheckBoxItem(text = "Website", checked = true) {

                }
                DialogCheckBoxItem(
                    text = "Playlist title",
                    checked = true
                ) {

                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your downloads will be saved as:\n.../website/playlist_title/file_name",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
//                    style = MaterialTheme.typography.labelMedium,
                )
            }
        })
}

@Preview
@Composable
private fun DirectoryPreferenceDialogPreview() {
    DirectoryPreferenceDialog()
}