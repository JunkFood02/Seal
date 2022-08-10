package com.junkfood.seal.ui.page.settings.download

import android.util.Log
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.CreditItem
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.util.DatabaseUtil

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
    var showEditDialog by remember { mutableStateOf(false) }
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
                CreditItem(title = commandTemplate.name, license = commandTemplate.template) {
                    Log.d(TAG, "TemplateListPage: $currentTemplateIndex")
                    currentTemplateIndex = index
                    Log.d(TAG, "TemplateListPage: $currentTemplateIndex")
                    showEditDialog = true
                }
            }
        }
    }
    if (showEditDialog) {
        Log.d(TAG, "TemplateListPage: $currentTemplateIndex")
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
}