package com.junkfood.seal.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.util.UpdateUtil

@Composable
fun UpdateDialog(
    onDismissRequest: () -> Unit,
    title: String,
    onConfirmUpdate: () -> Unit,
    releaseNote: String,
    downloadStatus: UpdateUtil.DownloadStatus,
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(title)

        },
        icon = { Icon(Icons.Outlined.NewReleases, null) }, confirmButton = {
            TextButton(onClick = { if (downloadStatus !is UpdateUtil.DownloadStatus.Progress) onConfirmUpdate() }) {
                when (downloadStatus) {
                    is UpdateUtil.DownloadStatus.Progress -> Text("${downloadStatus.percent} %")
                    else -> Text(stringResource(R.string.update))
                }
            }
        }, dismissButton = {
            DismissButton { onDismissRequest() }
        }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(releaseNote)
            }
        })
}