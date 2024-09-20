package com.junkfood.seal.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.ui.component.NavigationBarSpacer
import com.junkfood.seal.ui.component.StateIndicator
import com.junkfood.seal.ui.component.VideoCardV2
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.svg.DynamicColorImageVectors
import com.junkfood.seal.ui.svg.drawablevectors.download
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.DownloadUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageV2(
    modifier: Modifier = Modifier,
    dialogViewModel: DownloadDialogViewModel = koinViewModel(),
    downloader: DownloaderV2 = koinInject(),
    processCount: Int = 0,
    navigateToSettings: () -> Unit,
    navigateToDownloads: () -> Unit,
    onNavigateToTaskList: () -> Unit,
) {

    DownloadPageImplV2(
        modifier = modifier,
        taskStateMap = downloader.getTaskStateMap(),
        processCount = processCount,
        downloadCallback = { dialogViewModel.postAction(Action.ShowSheet()) },
        navigateToSettings = navigateToSettings,
        navigateToDownloads = navigateToDownloads,
        onNavigateToTaskList = onNavigateToTaskList,
    ) { task, state ->
        when (state) {
            is Task.State.Canceled -> {
                downloader.restart(task)
            }
            is Task.State.Completed -> {}
            is Task.State.Error -> {}
            is Task.State.Cancelable,
            Task.State.Idle,
            Task.State.ReadyWithInfo -> {
                downloader.cancel(task)
            }
            else -> {}
        }
    }

    var preferences by remember {
        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
    }
    val sheetValue by dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle()
    val state by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()

    val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sheetValue) {
        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            launch { sheetState.hide() }.invokeOnCompletion { showDialog = false }
        }
    }

    if (showDialog) {

        DownloadDialog(
            state = state,
            sheetState = sheetState,
            config = Config(),
            preferences = preferences,
            onPreferencesUpdate = { preferences = it },
            onActionPost = { dialogViewModel.postAction(it) },
        )
    }
    when (selectionState) {
        is DownloadDialogViewModel.SelectionState.FormatSelection ->
            FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageImplV2(
    modifier: Modifier = Modifier,
    taskStateMap: SnapshotStateMap<Task, Task.State>,
    processCount: Int = 0,
    downloadCallback: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToDownloads: () -> Unit = {},
    onNavigateToTaskList: () -> Unit = {},
    onActionPost: (Task, Task.State) -> Unit,
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                modifier = Modifier.padding(horizontal = 8.dp),
                navigationIcon = {
                    SettingsIconButton(modifier = Modifier, onClick = navigateToSettings)
                },
                actions = {
                    TaskListIconButton(
                        modifier = Modifier,
                        processCount = processCount,
                        onClick = onNavigateToTaskList,
                    )
                    DownloadHistoryIconButton(modifier = Modifier, onClick = navigateToDownloads)
                },
            )
        },
        floatingActionButton = { FABs(modifier = Modifier, downloadCallback = downloadCallback) },
    ) {
        Column(modifier = Modifier.padding(it).fillMaxSize()) {
            Title()

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(taskStateMap.toList()) { (task, state) ->
                        VideoCardV2(
                            modifier = Modifier,
                            viewState = task.viewState,
                            stateIndicator = {
                                StateIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    state = state,
                                ) {
                                    onActionPost(task, state)
                                }
                            },
                        ) {}
                    }
                }
                if (taskStateMap.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    DownloadQueuePlaceholder(modifier = Modifier)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            NavigationBarSpacer()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    TooltipBox(
        modifier = modifier,
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.settings)) } },
    ) {
        IconButton(onClick = onClick, modifier = Modifier) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(id = R.string.settings),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListIconButton(
    modifier: Modifier = Modifier,
    processCount: Int,
    onClick: () -> Unit,
) {
    TooltipBox(
        modifier = modifier,
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { Text(text = stringResource(id = R.string.running_tasks)) },
    ) {
        IconButton(onClick = onClick, modifier = Modifier) {
            BadgedBox(
                badge = {
                    if (processCount > 0) {
                        Badge(modifier = Modifier) { Text("$processCount") }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Terminal,
                    contentDescription = stringResource(id = R.string.running_tasks),
                )
            }
        }
    }
}

@Composable
@Preview
private fun DownloadQueuePlaceholder(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = rememberVectorPainter(image = DynamicColorImageVectors.download()),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.5f),
        )
        Text(
            text = stringResource(R.string.you_ll_find_your_downloads_here),
            modifier = Modifier.padding(top = 36.dp).padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.download_hint),
            modifier = Modifier.padding(top = 4.dp).padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadHistoryIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    TooltipBox(
        modifier = modifier,
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { Text(text = stringResource(id = R.string.downloads_history)) },
    ) {
        IconButton(onClick = onClick, modifier = Modifier) {
            Icon(
                imageVector = Icons.Outlined.Subscriptions,
                contentDescription = stringResource(id = R.string.downloads_history),
            )
        }
    }
}

@Composable
@Preview(name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
private fun DownloadPagePreview() {
    val module = module {
        single<DownloaderV2> {
            object : DownloaderV2 {
                override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
                    return mutableStateMapOf(
                        Task("", PreferencesMock) to
                            Task.State.Error(
                                Throwable(""),
                                action = Task.RestartableAction.Download,
                            )
                    )
                }

                override fun enqueue(task: Task) {}

                override fun enqueue(taskList: List<Task>) {}

                override fun cancel(task: Task) {}

                override fun restart(task: Task) {}
            }
        }
        viewModel { DownloadDialogViewModel(downloader = get()) }
    }

    KoinApplication(application = { modules(module) }) {
        val downloader: DownloaderV2 = koinInject()
        SealTheme {
            Column() {
                DownloadPageImplV2(
                    taskStateMap = downloader.getTaskStateMap(),
                    processCount = 2,
                    onActionPost = { task, state ->
                        when (state) {
                            is Task.State.Canceled -> {
                                downloader.restart(task)
                            }
                            is Task.State.Completed -> {}
                            is Task.State.Error -> {
                                downloader.restart(task)
                            }
                            is Task.State.Cancelable,
                            Task.State.Idle,
                            Task.State.ReadyWithInfo -> {
                                downloader.cancel(task)
                            }
                            else -> {}
                        }
                    },
                )
            }
        }
    }
}
