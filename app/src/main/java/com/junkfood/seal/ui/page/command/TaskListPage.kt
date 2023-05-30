package com.junkfood.seal.ui.page.command

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.common.SVGImage
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.BottomDrawer
import com.junkfood.seal.ui.component.CustomCommandTaskItem
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.DrawerPreview
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.OutlinedButtonChip
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.TaskStatus
import com.junkfood.seal.ui.svg.TaskSVG
import com.junkfood.seal.util.TEMPLATE_EXAMPLE
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TaskListPage(onBackPressed: () -> Unit, onNavigateToDetail: (Int) -> Unit) {
    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true
    )

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
            }) {
                Icon(Icons.Outlined.Add, stringResource(id = R.string.new_task))
            }
        }) { paddings ->
        val clipboardManager = LocalClipboardManager.current
        LazyColumn(
            modifier = Modifier.padding(paddings),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                Downloader.mutableTaskList.values.toList().sortedBy { it.state.toStatus() },
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
                        }, modifier = Modifier.animateItemPlacement()
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
    BottomDrawer(drawerState = sheetState, sheetContent = {
        var showDialog by remember { mutableStateOf(false) }
        Column(
            Modifier.fillMaxWidth()
        ) {
            TaskCreatorDialogContent()
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
        if (showDialog) {
            TemplatePickerDialog() { showDialog = false }
        }
    }) {}
//            item {
//                CustomCommandTaskItem(status = TaskStatus.RUNNING)
//            }
//            item {
//                CustomCommandTaskItem(status = TaskStatus.FINISHED)
//            }
//            item {
//                CustomCommandTaskItem(status = TaskStatus.ERROR)
//            }
//            item {
//                CustomCommandTaskItem(status = TaskStatus.CANCELED)
//            }


}


private fun Downloader.CustomCommandTask.State.toStatus(): TaskStatus = when (this) {
    Downloader.CustomCommandTask.State.Canceled -> TaskStatus.CANCELED
    Downloader.CustomCommandTask.State.Completed -> TaskStatus.FINISHED
    is Downloader.CustomCommandTask.State.Error -> TaskStatus.ERROR
    is Downloader.CustomCommandTask.State.Running -> TaskStatus.RUNNING
}

@Composable
fun ColumnScope.TaskCreatorDialogContent() {
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
    var value by remember { mutableStateOf("https://www.example.com") }
    OutlinedTextField(value = value, onValueChange = {
        value = it
    }, label = {
        Text(text = stringResource(id = R.string.video_url))
    }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 3)


    LazyRow(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OutlinedButtonChip(icon = Icons.Outlined.Code, label = "Template 1") {

            }
        }
        item {
            OutlinedButtonChip(
                icon = Icons.Outlined.NewLabel,
                label = stringResource(id = R.string.new_template)
            ) {

            }
        }
        item {
            OutlinedButtonChip(
                icon = Icons.Outlined.Edit,
                label = stringResource(id = R.string.edit_template, "Template 1")
            ) {

            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
                TaskCreatorDialogContent()

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
@Preview
fun TemplatePickerDialog(onDismissRequest: () -> Unit = {}) {
    SealDialog(onDismissRequest = onDismissRequest, confirmButton = {
        DismissButton(onClick = onDismissRequest)
    }, title = {
        Text(text = stringResource(id = R.string.template_selection))
    }, icon = { Icon(imageVector = Icons.Outlined.Code, contentDescription = null) },
        text = {
            Box() {
                HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
                LazyColumn {
                    repeat(20) {
                        item {
                            TemplateSingleChoiceItem(
                                text = "Template $it",
                                supportingText = TEMPLATE_EXAMPLE,
                                selected = false
                            ) {

                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
            }


        }
    )

}

@Composable
private fun TemplateSingleChoiceItem(
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
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}