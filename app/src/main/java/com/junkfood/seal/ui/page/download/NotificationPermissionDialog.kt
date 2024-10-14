package com.junkfood.seal.ui.page.download

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
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
fun NotificationPermissionDialog(
    onDismissRequest: () -> Unit = {},
    onPermissionGranted: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(imageVector = Icons.Outlined.NotificationsActive, contentDescription = null)
        },
        text = { Text(text = stringResource(id = R.string.enable_notifications_desc)) },
        title = { Text(text = stringResource(id = R.string.enable_notifications)) },
        confirmButton = {
            Button(onClick = onPermissionGranted) {
                Text(text = stringResource(id = R.string.okay))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.disable))
            }
        },
    )
}
