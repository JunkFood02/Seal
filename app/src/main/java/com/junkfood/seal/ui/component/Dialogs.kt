package com.junkfood.seal.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R

@Composable
fun DeleteDialog(dismissCallback: () -> Unit, confirmCallback: () -> Unit) {
    AlertDialog(onDismissRequest = dismissCallback, icon = {
        Icon(
            Icons.Outlined.Delete,
            contentDescription = "delete"
        )
    }, title = {
        Text(text = stringResource(R.string.delete_info))
    }, text = { Text(text = stringResource(R.string.delete_info_msg)) }, confirmButton = {
        TextButton(onClick = confirmCallback) {
            Text(text = stringResource(R.string.confirm))
        }
    }, dismissButton = {
        TextButton(onClick = dismissCallback) {
            Text(text = stringResource(R.string.dismiss))
        }
    })
}