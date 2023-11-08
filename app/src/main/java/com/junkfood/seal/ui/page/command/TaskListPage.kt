package com.junkfood.seal.ui.page.command

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.common.SVGImage
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.CustomCommandTaskItem
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.DrawerPreview
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.OutlinedButtonChip
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.TaskStatus
import com.junkfood.seal.ui.page.settings.command.CommandTemplateDialog
import com.junkfood.seal.ui.svg.TaskSVG
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.TEMPLATE_ID
import com.junkfood.seal.util.matchUrlFromString
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TaskListPage(onBackPressed: () -> Unit, onNavigateToDetail: (Int) -> Unit) {
    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.running_tasks),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
            }, navigationIcon = {
                BackButton { onBackPressed() }
            }, actions = {}, scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    sheetState.show()
                }
            }, modifier = Modifier.padding(vertical = 18.dp, horizontal = 6.dp)) {
                Icon(Icons.Outlined.Add, stringResource(id = R.string.new_task))
            }
        }) { paddings ->
        val clipboardManager = LocalClipboardManager.current
        LazyColumn(
            modifier = Modifier.padding(paddings),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Downloader.mutableTaskList.values.toList().sortedBy { it.state.toStatus() },
                key = { it.toKey() }) {
                it.run {
                    CustomCommandTaskItem(
                        status = state.toStatus(),
                        progress = if (state is Downloader.CustomCommandTask.State.Running) state.progress / 100f else 0f,
                        progressText = currentLine,
                        url = url,
                        templateName = template.name,
                        onCancel = { onCancel() },
                        onCopyError = {
                            onCopyError(clipboardManager)
                        },
                        onRestart = {
                            onRestart()
                        },
                        onCopyLog = {
                            onCopyLog(clipboardManager)
                        },
                        onShowLog = {
                            onNavigateToDetail(hashCode())
                        },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
        if (Downloader.mutableTaskList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SVGImage(
                        SVGString = TaskSVG,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 72.dp, vertical = 20.dp)
                    )
                    Text(
                        text = stringResource(R.string.no_custom_command_tasks),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
    }
    BackHandler(sheetState.targetValue == SheetValue.Expanded) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }
    }
    if (showBottomSheet) SealModalBottomSheet(sheetState = sheetState,
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }
        },
        content = {
            val clipboardManager = LocalClipboardManager.current

            var showTemplateSelectionDialog by remember { mutableStateOf(false) }
            var showTemplateCreatorDialog by remember { mutableStateOf(false) }
            var showTemplateEditorDialog by remember { mutableStateOf(false) }

            val template by remember(
                showTemplateCreatorDialog, showTemplateSelectionDialog, showTemplateEditorDialog
            ) {
                mutableStateOf(PreferenceUtil.getTemplate())
            }

            var url by remember { mutableStateOf("") }

            LaunchedEffect(sheetState.targetValue) {
                if (sheetState.targetValue == SheetValue.Expanded) url =
                    matchUrlFromString(clipboardManager.getText()?.text.toString(), true)

            }

            Column(
                Modifier.fillMaxWidth()
            ) {
                TaskCreatorDialogContent(url = url,
                    onValueChange = {
                        url = it
                    },
                    template = template,
                    onTemplateSelectionClicked = { showTemplateSelectionDialog = true },
                    onNewTemplateClicked = { showTemplateCreatorDialog = true },
                    onEditClicked = { showTemplateEditorDialog = true })
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    item {
                        OutlinedButtonWithIcon(
                            modifier = Modifier.padding(horizontal = 12.dp), onClick = {
                                scope.launch { sheetState.hide() }
                            }, icon = Icons.Outlined.Cancel, text = stringResource(R.string.cancel)
                        )
                    }
                    item {
                        FilledButtonWithIcon(
                            onClick = {
                                Downloader.executeCommandWithUrl(url)
                                scope.launch { sheetState.hide() }
                            },
                            icon = Icons.Outlined.DownloadDone,
                            text = stringResource(R.string.start)
                        )
                    }
                }
            }
            if (showTemplateSelectionDialog) {
                TemplatePickerDialog() { showTemplateSelectionDialog = false }
            }
            if (showTemplateCreatorDialog) {
                CommandTemplateDialog(onDismissRequest = { showTemplateCreatorDialog = false },
                    confirmationCallback = {
                        scope.launch {
                            TEMPLATE_ID.updateInt(it)
                        }
                    })
            }
            if (showTemplateEditorDialog) {
                CommandTemplateDialog(commandTemplate = template,
                    onDismissRequest = { showTemplateEditorDialog = false })
            }
        })

}


private fun Downloader.CustomCommandTask.State.toStatus(): TaskStatus = when (this) {
    Downloader.CustomCommandTask.State.Canceled -> TaskStatus.CANCELED
    Downloader.CustomCommandTask.State.Completed -> TaskStatus.FINISHED
    is Downloader.CustomCommandTask.State.Error -> TaskStatus.ERROR
    is Downloader.CustomCommandTask.State.Running -> TaskStatus.RUNNING
}

@Composable
fun ColumnScope.TaskCreatorDialogContent(
    url: String,
    onValueChange: (String) -> Unit = {},
    template: CommandTemplate,
    onTemplateSelectionClicked: () -> Unit = {},
    onNewTemplateClicked: () -> Unit = {},
    onEditClicked: () -> Unit = {},
) {
    Icon(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        imageVector = Icons.Outlined.Add,
        contentDescription = null
    )
    Text(
        text = stringResource(id = R.string.new_task),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 16.dp),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
    Text(
        text = stringResource(R.string.custom_command_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(bottom = 16.dp)
    )
    OutlinedTextField(value = url, onValueChange = onValueChange, label = {
        Text(text = stringResource(id = R.string.video_url))
    }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 3)


    LazyRow(
        modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedButtonChip(
                icon = Icons.Outlined.Code,
                label = template.name,
                onClick = onTemplateSelectionClicked
            )
        }
        item {
            OutlinedButtonChip(
                icon = Icons.Outlined.NewLabel,
                label = stringResource(id = R.string.new_template),
                onClick = onNewTemplateClicked
            )
        }
        item {
            OutlinedButtonChip(
                icon = Icons.Outlined.Edit,
                label = stringResource(id = R.string.edit_template, template.name),
                onClick = onEditClicked
            )
        }
    }
}

@Composable
@Preview
fun TaskCreatorDialog() {
    val templateList = listOf(
        CommandTemplate(0, "Template Sample", ""),
    )
    MaterialTheme {
        DrawerPreview {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                TaskCreatorDialogContent(
                    url = "https://www.example.com",
                    template = CommandTemplate(0, "Template Sample", ""),

                    )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    item {
                        OutlinedButtonWithIcon(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            onClick = {},
                            icon = Icons.Outlined.Cancel,
                            text = stringResource(R.string.cancel)
                        )
                    }
                    item {
                        FilledButtonWithIcon(
                            onClick = {},
                            icon = Icons.Outlined.DownloadDone,
                            text = stringResource(R.string.start)
                        )
                    }
                }
            }

        }
    }
}


@Composable
fun TemplatePickerDialog(onDismissRequest: () -> Unit = {}) {
    val templateList by PreferenceUtil.templateStateFlow.collectAsStateWithLifecycle()
    var selectedId by TEMPLATE_ID.intState
    val scrollState =
        rememberLazyListState(initialFirstVisibleItemIndex = templateList.indexOfFirst { it.id == selectedId }
            .run {
                if (this == -1) 0 else this
            })

    SealDialog(onDismissRequest = onDismissRequest, confirmButton = {
        DismissButton(onClick = onDismissRequest)
    }, title = {
        Text(text = stringResource(id = R.string.template_selection))
    }, icon = { Icon(imageVector = Icons.Outlined.Code, contentDescription = null) }, text = {
        Box(modifier = Modifier.heightIn(max = 450.dp)) {
            HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
            LazyColumn(state = scrollState) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(templateList) {
                    TemplateSingleChoiceItem(
                        text = it.name, supportingText = it.template, selected = it.id == selectedId
                    ) {
                        selectedId = it.id
                        TEMPLATE_ID.updateInt(it.id)
                        onDismissRequest()
                    }
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
            HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
        }
    })
}

@Composable
fun TemplateSingleChoiceItem(
    modifier: Modifier = Modifier,
    text: String,
    supportingText: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = true,
                onClick = onClick,
            )
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            modifier = Modifier
                .padding(end = 8.dp)
                .clearAndSetSemantics { },
            selected = selected,
            onClick = onClick
        )
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = supportingText.replace("\n", " "),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}