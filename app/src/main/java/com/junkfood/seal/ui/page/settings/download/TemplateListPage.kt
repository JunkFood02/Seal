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
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE_INDEX
import kotlinx.coroutines.launch

private const val TAG = "TemplateListPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListPage(onBackPressed: () -> Unit) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec,
        rememberTopAppBarState(),
        canScroll = { true })
    val templates = DatabaseUtil.getTemplateFlow().collectAsState(ArrayList()).value
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var editingTemplateIndex by remember { mutableStateOf(-1) }
    var selectedTemplateIndex by remember {
        mutableStateOf(PreferenceUtil.getInt(TEMPLATE_INDEX, 0))
    }
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
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
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