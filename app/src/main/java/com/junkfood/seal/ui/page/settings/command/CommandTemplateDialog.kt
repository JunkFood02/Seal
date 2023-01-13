package com.junkfood.seal.ui.page.settings.command

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import com.google.accompanist.flowlayout.FlowRow
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.database.OptionShortcut
import com.junkfood.seal.ui.component.AddButton
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.ui.component.PasteFromClipBoardButton
import com.junkfood.seal.ui.component.ShortcutChip
import com.junkfood.seal.ui.component.SealTextField
import com.junkfood.seal.util.DatabaseUtil
import com.kyant.monet.a3
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
                            PasteFromClipBoardButton { templateText = it }
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

@Composable
fun OptionChipsDialog(onDismissRequest: () -> Unit) {
    val scope = rememberCoroutineScope()
    val shortcuts by DatabaseUtil.getShortcuts().collectAsState(emptyList())
    var text by remember { mutableStateOf("") }
    val addShortCuts = {
        scope.launch {
            if (shortcuts.find { it.option == text } == null)
                DatabaseUtil.insertShortcut(OptionShortcut(option = text))
            text = ""
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.edit_option_chips)) },
        icon = { Icon(Icons.Outlined.Edit, null) }, text = {
            Column {
                Column(
                    modifier = Modifier
                        .requiredHeight(400.dp)
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(modifier = Modifier.width(400.dp)) {
                        shortcuts.forEach { item ->
                            ShortcutChip(
                                text = item.option,
                                onRemove = {
                                    scope.launch {
                                        DatabaseUtil.deleteShortcut(item)
                                    }
                                })
                        }
                    }
                }

                SealTextField(
                    modifier = Modifier.padding(top = 12.dp),
                    value = text,
                    onValueChange = { text = it },
                    trailingIcon = {
                        AddButton(onClick = { addShortCuts() }, enabled = text.isNotEmpty())
                    },
                    keyboardActions = KeyboardActions(onDone = { addShortCuts() }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    maxLines = 2,

                )
            }

        }, confirmButton = {
            ConfirmButton { onDismissRequest() }
        })
}