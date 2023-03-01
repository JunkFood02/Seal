package com.junkfood.seal.ui.page.command

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.SVGImage
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.CustomCommandTaskItem
import com.junkfood.seal.ui.component.TaskStatus
import com.junkfood.seal.ui.svg.TaskSVG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListPage(onBackPressed: () -> Unit, onNavigateToDetail: (Int) -> Unit) {
    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
            }, actions = {
            }, scrollBehavior = scrollBehavior
            )
        }) { paddings ->
        val clipboardManager = LocalClipboardManager.current
        LazyColumn(
            modifier = Modifier.padding(paddings), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Downloader.mutableTaskList.values.toList()) {
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
                        }, onCopyLog = {
                            onCopyLog(clipboardManager)
                        }, onShowLog = {
                            onNavigateToDetail(hashCode())
                        }
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
                        text = "No custom command tasks",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
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
}

private fun Downloader.CustomCommandTask.State.toStatus(): TaskStatus = when (this) {
    Downloader.CustomCommandTask.State.Canceled -> TaskStatus.CANCELED
    Downloader.CustomCommandTask.State.Completed -> TaskStatus.FINISHED
    is Downloader.CustomCommandTask.State.Error -> TaskStatus.ERROR
    is Downloader.CustomCommandTask.State.Running -> TaskStatus.RUNNING
}