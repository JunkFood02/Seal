package com.junkfood.seal.ui.page.settings.command

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE_INDEX
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

    var editingTemplateIndex by remember { mutableStateOf(-1) }
    var selectedTemplateIndex by remember {
        mutableStateOf(PreferenceUtil.getInt(TEMPLATE_INDEX, 0))
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
            itemsIndexed(templates) { index, commandTemplate ->
                TemplateItem(
                    label = commandTemplate.name,
                    template = commandTemplate.template,
                    selected = selectedTemplateIndex == index,
                    onClick = {
                        editingTemplateIndex = index
                        showEditDialog = true
                    }, onSelect = {
                        selectedTemplateIndex = index
                        PreferenceUtil.updateInt(TEMPLATE_INDEX, selectedTemplateIndex)
                    })
                {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    editingTemplateIndex = index
                    if (templates.size != 1)
                        showDeleteDialog = true
                }
            }
            item {
                PreferenceItemVariant(
                    title = stringResource(id = R.string.new_template),
                    icon = Icons.Outlined.Add
                ) {
                    editingTemplateIndex = -1
                    showEditDialog = true
                }
            }
        }
    }
    if (showEditDialog) {
        if (editingTemplateIndex == -1)
            CommandTemplateDialog(
                newTemplate = true,
                commandTemplate = CommandTemplate(0, "", ""),
                onDismissRequest = { showEditDialog = false })
        else
            CommandTemplateDialog(
                newTemplate = false,
                commandTemplate = templates[editingTemplateIndex],
                onDismissRequest = { showEditDialog = false })
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, null) },
            title = { Text(stringResource(R.string.remove_template)) },
            text = { Text(stringResource(R.string.remove_template_desc).format(templates[editingTemplateIndex].name)) },
            dismissButton = { DismissButton { showDeleteDialog = false } },
            confirmButton = {
                ConfirmButton {
                    if (selectedTemplateIndex >= editingTemplateIndex) {
                        selectedTemplateIndex--
                        PreferenceUtil.updateInt(TEMPLATE_INDEX, selectedTemplateIndex)
                    }
                    scope.launch { DatabaseUtil.deleteTemplate(templates[editingTemplateIndex]) }
                    showDeleteDialog = false
                }
            })
    }
}