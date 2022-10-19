package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RenameDialog(
    commandTemplate: CommandTemplate = CommandTemplate(0, "", ""),
    newTemplate: Boolean = false,
    onDismissRequest: () -> Unit = {},
    confirmationCallback: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var reName by remember { mutableStateOf(commandTemplate.name) }
    var isError by remember { mutableStateOf(false) }
    AlertDialog(
        icon = { Icon(if (newTemplate) Icons.Outlined.Add else Icons.Outlined.EditNote, null) },
        title = {
            Text(
                stringResource(R.string.rename)
            )
        },
        onDismissRequest = {},
        confirmButton = {
            ConfirmButton {
                if (reName.isBlank() || reName.isEmpty()) {
                    isError = true
                } else {
                    scope.launch {
                    }
                    confirmationCallback()
                    onDismissRequest()
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 16.dp),
                    value = reName,
                    onValueChange = {
                        reName = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.new_name)) },
                    maxLines = 1,
                    isError = isError
                )
            }
        })
}