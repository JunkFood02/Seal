package com.junkfood.seal.ui.page.command

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.App
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.CustomCommandTaskItem
import com.junkfood.seal.ui.component.TaskItemPreview
import com.junkfood.seal.ui.component.TaskStatus
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.TextUtil
import com.yausername.youtubedl_android.YoutubeDL
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
        val clipboardManager = LocalClipboardManager.current
        LazyColumn(
            modifier = Modifier.padding(paddings), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Downloader.mutableTaskList.values.toList()) {
                val id = it.toKey()

                CustomCommandTaskItem(
                    status = it.state.toStatus(),
                    progress = if (it.state is Downloader.CustomCommandTask.State.Running) it.state.progress / 100f else 0f,
                    progressText = it.currentLine,
                    url = it.url,
                    templateName = it.template.name,
                    onCancel = {
                        YoutubeDL.destroyProcessById(id)
                        Downloader.onProcessCanceled(id)
                    },
                    onCopyError = {
                        clipboardManager.setText(AnnotatedString(it.currentLine))
                        TextUtil.makeToast(R.string.error_copied)
                    },
                    onRestart = {
                        App.applicationScope.launch {
                            DownloadUtil.executeCommandInBackground(
                                it.url,
                                it.template
                            )
                        }
                    }, onCopyLog = {
                        clipboardManager.setText(AnnotatedString(it.output))
                    }, onShowLog = {
                        TextUtil.makeToast("Not Yet Implement!")
                    }
                )
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
}

private fun Downloader.CustomCommandTask.State.toStatus(): TaskStatus = when (this) {
    Downloader.CustomCommandTask.State.Canceled -> TaskStatus.CANCELED
    Downloader.CustomCommandTask.State.Completed -> TaskStatus.FINISHED
    is Downloader.CustomCommandTask.State.Error -> TaskStatus.ERROR
    is Downloader.CustomCommandTask.State.Running -> TaskStatus.RUNNING
}