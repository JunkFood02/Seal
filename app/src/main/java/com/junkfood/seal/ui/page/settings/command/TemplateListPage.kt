package com.junkfood.seal.ui.page.settings.command

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.database.backup.BackupUtil
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HelpDialog
import com.junkfood.seal.ui.component.PreferenceItemVariant
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.ui.component.TemplateItem
import com.junkfood.seal.ui.page.settings.about.YtdlpRepository
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.TEMPLATE_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TemplateListPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListPage(onNavigateBack: () -> Unit, onNavigateToEditPage: (Int) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val templates by PreferenceUtil.templateListStateFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showHelpDialog by remember { mutableStateOf(false) }

    var isMultiSelectEnabled by remember { mutableStateOf(false) }


    val selectedTemplates = remember {
        mutableStateListOf<CommandTemplate>()
    }
    LaunchedEffect(isMultiSelectEnabled) {
        if (!isMultiSelectEnabled) {
            delay(200)
            selectedTemplates.clear()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShortcutsDialog by remember { mutableStateOf(false) }
    var isCustomCommandEnabled by remember {
        mutableStateOf(CUSTOM_COMMAND.getBoolean())
    }

    var selectedTemplateId by TEMPLATE_ID.intState
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(isMultiSelectEnabled) {
        isMultiSelectEnabled = false
    }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier, hostState = snackbarHostState
            )
        },
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    modifier = Modifier,
                    text = if (isMultiSelectEnabled) stringResource(id = R.string.custom_command_template)
                    else stringResource(id = R.string.custom_command),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }, navigationIcon = {
                BackButton {
                    onNavigateBack()
                }
            }, actions = {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = {
                    view.slightHapticFeedback()
                    showHelpDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = stringResource(
                            id = R.string.how_does_it_work
                        )
                    )
                }
                if (!isMultiSelectEnabled) {
                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {

                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                Icons.Outlined.MoreVert, contentDescription = stringResource(
                                    R.string.show_more_actions
                                )
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(leadingIcon = {
                                Icon(
                                    Icons.Outlined.ContentPasteGo, null
                                )
                            }, text = {
                                Text(stringResource(R.string.export_to_clipboard))
                            }, onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.template_exported)
                                            .format(templates.size)
                                    )
                                }
                                scope.launch {
                                    clipboardManager.setText(
                                        AnnotatedString(BackupUtil.exportTemplatesToJson())
                                    )
                                    expanded = false
                                }
                            })
                            DropdownMenuItem(leadingIcon = {
                                Icon(
                                    Icons.Outlined.AssignmentReturn, null
                                )
                            }, text = {
                                Text(stringResource(R.string.import_from_clipboard))
                            }, onClick = {
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
                }
            }, scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            val checkBoxState = remember(isMultiSelectEnabled, selectedTemplates.size) {
                when (selectedTemplates.size) {
                    templates.size -> ToggleableState.On
                    in 1 until templates.size -> ToggleableState.Indeterminate
                    else -> ToggleableState.Off
                }
            }
            AnimatedVisibility(
                isMultiSelectEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {

                BottomAppBar {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TriStateCheckbox(state = checkBoxState, onClick = {
                            view.slightHapticFeedback()
                            when (checkBoxState) {
                                ToggleableState.On -> selectedTemplates.clear()
                                else -> selectedTemplates.run {
                                    clear()
                                    addAll(templates)
                                }
                            }
                        }, modifier = Modifier.padding(start = 12.dp))

                        Text(
                            text = stringResource(
                                id = R.string.selected_item_count, selectedTemplates.size
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f),
                        )

                        IconButton(
                            onClick = {
                                view.slightHapticFeedback()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.template_exported)
                                            .format(selectedTemplates.size)
                                    )
                                }
                                scope.launch {
                                    clipboardManager.setText(
                                        AnnotatedString(
                                            BackupUtil.exportTemplatesToJson(
                                                templates = selectedTemplates,
                                                shortcuts = emptyList()
                                            )
                                        )
                                    )
                                }
                            }, enabled = selectedTemplates.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPasteGo,
                                contentDescription = stringResource(
                                    id = R.string.export_to_clipboard
                                )
                            )
                        }
                        IconButton(
                            onClick = {
                                view.slightHapticFeedback()
                                showDeleteDialog = true
                            },
                            enabled = selectedTemplates.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = stringResource(id = R.string.remove)
                            )
                        }
                    }

                }

            }
        }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                PreferenceSwitchWithContainer(title = stringResource(R.string.use_custom_command),
                    icon = null,
                    isChecked = isCustomCommandEnabled,
                    onClick = {
                        isCustomCommandEnabled = !isCustomCommandEnabled
                        PreferenceUtil.updateValue(
                            CUSTOM_COMMAND, isCustomCommandEnabled
                        )
                    })
            }
            items(templates) { commandTemplate ->
                TemplateItem(label = commandTemplate.name,
                    template = commandTemplate.template,
                    selected = selectedTemplateId == commandTemplate.id,
                    isMultiSelectEnabled = isMultiSelectEnabled,
                    onCheckedChange = {
                        if (selectedTemplates.contains(commandTemplate)) selectedTemplates.remove(
                            commandTemplate
                        )
                        else selectedTemplates.add(commandTemplate)
                    },
                    checked = selectedTemplates.contains(commandTemplate),
                    onClick = {
                        onNavigateToEditPage(commandTemplate.id)
                    },
                    onSelect = {
                        selectedTemplateId = commandTemplate.id
                        PreferenceUtil.encodeInt(TEMPLATE_ID, selectedTemplateId)
                    }, onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        isMultiSelectEnabled = true
                        selectedTemplates.add(commandTemplate)
                    })
            }
            if (!isMultiSelectEnabled) {
                item {
                    PreferenceItemVariant(
                        title = stringResource(id = R.string.new_template),
                        icon = Icons.Outlined.Add
                    ) {
                        onNavigateToEditPage(-1)
                    }
                }
                item {
                    PreferenceItemVariant(
                        title = stringResource(id = R.string.edit_shortcuts),
                        icon = Icons.Outlined.BookmarkAdd,
                    ) {
                        showShortcutsDialog = true
                    }

                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, null) },
            title = { Text(stringResource(R.string.remove_template)) },
            text = {
                Text(
                    stringResource(
                        R.string.remove_multiple_templates_msg, pluralStringResource(
                            id = R.plurals.item_count,
                            count = selectedTemplates.size,
                            selectedTemplates.size
                        )
                    )
                )
            },
            dismissButton = { DismissButton { showDeleteDialog = false } },
            confirmButton = {
                ConfirmButton {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            DatabaseUtil.deleteTemplates(selectedTemplates)
                        }
                        isMultiSelectEnabled = false
                    }
                    showDeleteDialog = false
                }
            })
    }


    if (showShortcutsDialog) {
        OptionChipsDialog {
            showShortcutsDialog = false
        }
    }
    val uriHandler = LocalUriHandler.current
    if (showHelpDialog) {
        HelpDialog(
            text = stringResource(id = R.string.custom_command_usage_msg),
            onDismissRequest = { showHelpDialog = false }, dismissButton = null
        ) {
            TextButton(onClick = {
                showHelpDialog = false
                uriHandler.openUri(YtdlpRepository)
            }) {
                Text(text = stringResource(id = R.string.learn_more))
            }
        }
    }

    LaunchedEffect(templates.size) {
        if (templates.isNotEmpty() && templates.find { it.id == selectedTemplateId } == null) {
            selectedTemplateId = templates.first().id
            PreferenceUtil.encodeInt(TEMPLATE_ID, selectedTemplateId)
        }
    }
}