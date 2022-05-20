package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandTemplateDialog(
    onDismissRequest: () -> Unit,
    confirmationCallback: () -> Unit,
    template: String,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.edit_template_desc))
                OutlinedTextField(
                    modifier = Modifier.padding(vertical = 12.dp),
                    value = template,
                    onValueChange = onValueChange,
                    label = { Text(stringResource(R.string.custom_command_template)) })

                TextButton(
                    onClick = onClick,
                ) {
                    Row() {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.yt_dlp_docs)
                        )
                    }

                }
            }
        })
}