package com.junkfood.seal.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.NavigationBarSpacer
import com.junkfood.seal.ui.page.download.HomePageViewModel
import com.junkfood.seal.ui.theme.SealTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageImplV2(
    downloaderState: Downloader.State,
    taskState: Downloader.DownloadTaskItem,
    viewState: HomePageViewModel.ViewState,
    errorState: Downloader.ErrorState,
    showVideoCard: Boolean = false,
    showOutput: Boolean = false,
    showDownloadProgress: Boolean = false,
    processCount: Int = 0,
    downloadCallback: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToDownloads: () -> Unit = {},
    onNavigateToTaskList: () -> Unit = {},
    cancelCallback: () -> Unit = {},
    onVideoCardClicked: () -> Unit = {},
    onUrlChanged: (String) -> Unit = {},
    isPreview: Boolean = false,
    content: @Composable () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                modifier = Modifier.padding(horizontal = 8.dp),
                navigationIcon = {
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text(text = stringResource(id = R.string.settings)) }
                        },
                    ) {
                        IconButton(onClick = { navigateToSettings() }, modifier = Modifier) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(id = R.string.settings),
                            )
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (processCount > 0)
                                Badge(modifier = Modifier.offset(x = (-16).dp, y = (16).dp)) {
                                    Text("$processCount")
                                }
                        }
                    ) {
                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider =
                                TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = { Text(text = stringResource(id = R.string.running_tasks)) },
                        ) {
                            IconButton(onClick = { onNavigateToTaskList() }, modifier = Modifier) {
                                Icon(
                                    imageVector = Icons.Outlined.Terminal,
                                    contentDescription = stringResource(id = R.string.running_tasks),
                                )
                            }
                        }
                    }
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { Text(text = stringResource(id = R.string.downloads_history)) },
                    ) {
                        IconButton(onClick = { navigateToDownloads() }, modifier = Modifier) {
                            Icon(
                                imageVector = Icons.Outlined.Subscriptions,
                                contentDescription = stringResource(id = R.string.downloads_history),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FABs(
                modifier =
                    with(receiver = Modifier) {
                        if (showDownloadProgress) this else this.imePadding()
                    },
                downloadCallback = downloadCallback,
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Title(
                onClick = {
                    cancelCallback()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )

            Column(Modifier.padding(horizontal = 24.dp).padding(top = 24.dp)) {
                content()
                NavigationBarSpacer()
                Spacer(modifier = Modifier.height(160.dp))
            }
        }
    }
}

@Composable
fun Title(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .padding(start = 12.dp, top = 24.dp)
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 3.dp)
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

@Composable
fun FABs(modifier: Modifier = Modifier, downloadCallback: () -> Unit = {}) {
    Column(modifier = modifier.padding(6.dp), horizontalAlignment = Alignment.End) {
        FloatingActionButton(
            onClick = downloadCallback,
            content = {
                Icon(
                    Icons.Outlined.FileDownload,
                    contentDescription = stringResource(R.string.download),
                )
            },
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }
}

@Composable
@Preview(name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
private fun DownloadPagePreview() {
    SealTheme {
        Column() {
            DownloadPageImplV2(
                downloaderState = Downloader.State.DownloadingVideo,
                taskState =
                    Downloader.DownloadTaskItem(
                        title = stringResource(R.string.video_title_sample_text),
                        uploader = stringResource(id = R.string.video_creator_sample_text),
                        progress = 0f,
                    ),
                viewState = HomePageViewModel.ViewState(),
                errorState = Downloader.ErrorState.None,
                processCount = 2,
                isPreview = true,
                showDownloadProgress = true,
                showVideoCard = true,
            ) {}
        }
    }
}
