package com.junkfood.seal.ui.page.settings.command

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.component.ButtonChip
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.ui.component.PasteButton
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CommandTemplateDialog(
    commandTemplate: CommandTemplate = CommandTemplate(0, "", ""),
    newTemplate: Boolean = commandTemplate.id == 0,
    onDismissRequest: () -> Unit = {},
    confirmationCallback: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var templateText by remember { mutableStateOf(commandTemplate.template) }
    var templateName by remember { mutableStateOf(commandTemplate.name) }
    var isError by remember { mutableStateOf(false) }
    AlertDialog(
        icon = { Icon(if (newTemplate) Icons.Outlined.Add else Icons.Outlined.EditNote, null) },
        title = {
            Text(
                stringResource(if (newTemplate) R.string.new_template else R.string.edit)
            )
        },
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
        confirmButton = {
            ConfirmButton {
                if (templateName.isBlank() || templateName.isEmpty()) {
                    isError = true
                } else {
                    scope.launch {
                        val id = if (newTemplate) {
                            DatabaseUtil.insertTemplate(
                                CommandTemplate(0, templateName, templateText)
                            ).toInt()

                        } else {
                            DatabaseUtil.updateTemplate(
                                commandTemplate.copy(
                                    name = templateName, template = templateText
                                )
                            )
                            commandTemplate.id
                        }
                        confirmationCallback(id)
                        onDismissRequest()
                    }
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
                Text(
                    text = stringResource(R.string.edit_template_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    value = templateName,
                    onValueChange = {
                        templateName = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.template_label)) },
                    maxLines = 1,
                    isError = isError
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    value = templateText,
                    onValueChange = { templateText = it },
                    trailingIcon = {
                        if (templateText.isEmpty())
                            PasteButton { templateText = it }
                        else ClearButton { templateText = "" }
                    },
                    label = { Text(stringResource(R.string.custom_command_template)) },
                    maxLines = 12,
                    minLines = 3
                )
                LinkButton()
            }
        })
}