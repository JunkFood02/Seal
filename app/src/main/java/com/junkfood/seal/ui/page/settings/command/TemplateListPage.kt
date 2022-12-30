package com.junkfood.seal.ui.page.settings.command

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItemVariant
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.ui.component.TemplateItem
import com.junkfood.seal.ui.page.settings.general.CommandTemplateDialog
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE_ID
import kotlinx.coroutines.launch

private const val TAG = "TemplateListPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListPage(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val templates = DatabaseUtil.getTemplateFlow().collectAsState(ArrayList()).value
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var isCustomCommandEnabled by remember {
        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND))
    }

    var editingTemplateId by remember { mutableStateOf(-1) }
    var selectedTemplateId by remember {
        mutableStateOf(PreferenceUtil.getInt(TEMPLATE_ID, 0))
    }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier,
                hostState = snackbarHostState
            )
        },
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.custom_command),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }, navigationIcon = {
                BackButton(modifier = Modifier.padding(start = 8.dp)) {
                    onBackPressed()
                }
            }, actions = {
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(
                                R.string.show_more_actions
                            )
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Outlined.ContentPasteGo, null) },
                            text = {
                                Text(stringResource(R.string.export_to_clipboard))
                            },
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.template_exported)
                                            .format(templates.size)
                                    )
                                }
                                scope.launch {
                                    clipboardManager.setText(
                                        AnnotatedString(DatabaseUtil.exportTemplatesToJson())
                                    )
                                    expanded = false
                                }
                            })
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Outlined.AssignmentReturn, null) },
                            text = {
                                Text(stringResource(R.string.import_from_clipboard))
                            },
                            onClick = {
                                scope.launch {
                                    expanded = false
                                    clipboardManager.getText()?.text?.let {
                                        if (it.isNotEmpty()) {
                                            val res = DatabaseUtil.importTemplatesFromJson(it)
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.template_imported)
                                                    .format(res)
                                            )
                                        }
                                    }
                                }
                            })
                    }
                }
            }, scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                PreferenceSwitchWithContainer(
                    title = stringResource(R.string.use_custom_command),
                    icon = null,
                    isChecked = isCustomCommandEnabled,
                    onClick = {
                        isCustomCommandEnabled = !isCustomCommandEnabled
                        PreferenceUtil.updateValue(
                            PreferenceUtil.CUSTOM_COMMAND,
                            isCustomCommandEnabled
                        )
                    })
            }
            items(templates) { commandTemplate ->
                TemplateItem(
                    label = commandTemplate.name,
                    template = commandTemplate.template,
                    selected = selectedTemplateId == commandTemplate.id,
                    onClick = {
                        editingTemplateId = commandTemplate.id
                        showEditDialog = true
                    }, onSelect = {
                        selectedTemplateId = commandTemplate.id
                        PreferenceUtil.updateInt(TEMPLATE_ID, selectedTemplateId)
                    })
                {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    editingTemplateId = commandTemplate.id
                    if (templates.size != 1)
                        showDeleteDialog = true
                }
            }
            item {
                PreferenceItemVariant(
                    title = stringResource(id = R.string.new_template),
                    icon = Icons.Outlined.Add
                ) {
                    editingTemplateId = -1
                    showEditDialog = true
                }
            }
        }
    }
    if (showEditDialog) {
        if (editingTemplateId == -1)
            CommandTemplateDialog(
                commandTemplate = CommandTemplate(0, "", ""),
                onDismissRequest = { showEditDialog = false })
        else
            CommandTemplateDialog(
                commandTemplate = templates.find { it.id == editingTemplateId } ?: CommandTemplate(
                    id = 0,
                    name = "",
                    template = ""
                ),
                onDismissRequest = { showEditDialog = false })
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, null) },
            title = { Text(stringResource(R.string.remove_template)) },
            text = { Text(stringResource(R.string.remove_template_desc).format(templates.find { it.id == editingTemplateId }?.name)) },
            dismissButton = { DismissButton { showDeleteDialog = false } },
            confirmButton = {
                ConfirmButton {
                    scope.launch {
                        DatabaseUtil.deleteTemplateById(editingTemplateId)
                    }
                    showDeleteDialog = false
                }
            })
    }
    LaunchedEffect(templates.size) {
        if (templates.isNotEmpty() && templates.find { it.id == selectedTemplateId } == null) {
            selectedTemplateId = templates.first().id
            PreferenceUtil.updateInt(TEMPLATE_ID, selectedTemplateId)
        }
    }
}