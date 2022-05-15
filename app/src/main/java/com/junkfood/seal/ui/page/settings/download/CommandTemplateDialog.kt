package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R

@Composable
fun CommandTemplateDialog(
    onDismissRequest: () -> Unit,
    confirmationCallback: () -> Unit,
    onValueChange: (String) -> Unit,
    template: String
) {
    AlertDialog(
        title = { Text(stringResource(R.string.edit_custom_command_template)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = confirmationCallback) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        }, text = {
            Column() {
                TextField(
                    value = template,
                    onValueChange = onValueChange,
                    label = { Text(stringResource(R.string.custom_command_template)) })
            }
        })
}