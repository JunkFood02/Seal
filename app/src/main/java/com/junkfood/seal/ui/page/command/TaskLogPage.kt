package com.junkfood.seal.ui.page.command

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.App
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ButtonChip
import com.junkfood.seal.util.DownloadUtil
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "TaskLogPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskLogPage(onBackPressed: () -> Unit, taskHashCode: Int) {
    Log.d(TAG, "TaskLogPage: $taskHashCode")
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val task = Downloader.mutableTaskList.values.find { it.hashCode() == taskHashCode } ?: return
    val clipboardManager = LocalClipboardManager.current
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.logs),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
            }, navigationIcon = {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
            }, actions = {
            }, scrollBehavior = scrollBehavior
            )
        }, bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Center
            ) {
                Divider(modifier = Modifier.fillMaxWidth())
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    task.run {
                        ButtonChip(
                            icon = Icons.Outlined.ContentCopy,
                            label = stringResource(id = R.string.copy_log)
                        ) {
                            onCopyLog(clipboardManager)
                        }
                        if (state is Downloader.CustomCommandTask.State.Error)
                            ButtonChip(
                                icon = Icons.Outlined.ErrorOutline,
                                label = stringResource(id = R.string.copy_error_report),
                                iconColor = MaterialTheme.colorScheme.error,
                            ) {
                                onCopyError(clipboardManager)
                            }
                        if (state is Downloader.CustomCommandTask.State.Running)
                            ButtonChip(
                                icon = Icons.Outlined.Cancel,
                                label = stringResource(id = R.string.cancel),
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                onCancel()
                            }
                        if (state is Downloader.CustomCommandTask.State.Canceled)
                            ButtonChip(
                                icon = Icons.Outlined.RestartAlt,
                                label = stringResource(id = R.string.restart),
                            ) {
                                onRestart()
                            }
                    }
                }
            }
        }) { paddings ->
        val scrollState = rememberScrollState()
        LaunchedEffect(key1 = scrollState.maxValue) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        Column(
            modifier = Modifier
                .padding(paddings)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            SelectionContainer() {
                Text(
                    text = task.output,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}