package com.junkfood.seal.ui.page.settings.download

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.TemplateItem
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.launch

private const val TAG = "TemplateListPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListPage(onBackPressed: () -> Unit) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec,
        rememberTopAppBarState(),
        canScroll = { true })
    val templates = DatabaseUtil.getTemplates().collectAsState(ArrayList()).value
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var currentTemplateIndex by remember { mutableStateOf(-1) }
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.custom_command_template),
                )
            }, navigationIcon = {
                BackButton(modifier = Modifier.padding(start = 8.dp)) {
                    onBackPressed()
                }
            }, scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                onClick = {
                    currentTemplateIndex = -1
                    showEditDialog = true
                }) {
                Icon(Icons.Outlined.Add, stringResource(R.string.new_template))
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            itemsIndexed(templates) { index, commandTemplate ->
                TemplateItem(
                    label = commandTemplate.name,
                    template = commandTemplate.template,
                    onClick = {
                        currentTemplateIndex = index
                        showEditDialog = true
                    })
                {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentTemplateIndex = index
                    showDeleteDialog = true
                }
            }
        }
    }
    if (showEditDialog) {
        if (currentTemplateIndex == -1)
            CommandTemplateDialog(
                newTemplate = true,
                commandTemplate = CommandTemplate(0, "", ""),
                onDismissRequest = { showEditDialog = false })
        else
            CommandTemplateDialog(
                newTemplate = false,
                commandTemplate = templates[currentTemplateIndex],
                onDismissRequest = { showEditDialog = false })
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Outlined.Delete, null) },
            title = { Text(stringResource(R.string.remove_template)) },
            text = { Text(stringResource(R.string.remove_template_desc).format(templates[currentTemplateIndex].name)) },
            dismissButton = { DismissButton { showDeleteDialog = false } },
            confirmButton = {
                ConfirmButton {
                    scope.launch { DatabaseUtil.deleteTemplate(templates[currentTemplateIndex]) }
                    showDeleteDialog = false
                }
            })
    }
}