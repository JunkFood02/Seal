package com.junkfood.seal.ui.page.command

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.CustomCommandTaskItem
import com.junkfood.seal.ui.component.TaskItemPreview
import com.junkfood.seal.ui.component.TaskStatus
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.launch

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
        LazyColumn(
            modifier = Modifier.padding(paddings), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Downloader.mutableTaskList.values.toList()) {
                CustomCommandTaskItem(
                    status = it.state.toStatus(),
                    progress = if (it.state is Downloader.CustomCommandTask.State.Running) it.state.progress / 100f else 0f,
                    progressText = it.currentLine,
                    url = it.url,
                    templateName = it.template.name,
                    )
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.RUNNING)
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.FINISHED)
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.ERROR)
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.CANCELED)
            }
        }

    }
}

fun Downloader.CustomCommandTask.State.toStatus(): TaskStatus = when (this) {
    Downloader.CustomCommandTask.State.Canceled -> TaskStatus.CANCELED
    Downloader.CustomCommandTask.State.Completed -> TaskStatus.FINISHED
    is Downloader.CustomCommandTask.State.Error -> TaskStatus.ERROR
    is Downloader.CustomCommandTask.State.Running -> TaskStatus.RUNNING
}