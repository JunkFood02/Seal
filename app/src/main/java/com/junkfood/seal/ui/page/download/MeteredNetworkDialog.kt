package com.junkfood.seal.ui.page.download


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.junkfood.seal.R

@Composable
@Preview
fun MeteredNetworkDialog(
    onDismissRequest: () -> Unit = {},
    onDownloadConfirm: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Outlined.SignalCellularConnectedNoInternet4Bar,
                contentDescription = null
            )
        },
        text = {
            Text(text = stringResource(id = R.string.download_disabled_with_cellular))
        },
        title = { Text(text = stringResource(id = R.string.download_with_cellular)) },
        confirmButton = {
            Button(onClick = onDownloadConfirm) {
                Text(text = stringResource(id = R.string.allow_once))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}