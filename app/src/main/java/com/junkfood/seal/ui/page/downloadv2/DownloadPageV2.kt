package com.junkfood.seal.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.component.ActionButton
import com.junkfood.seal.ui.component.SelectionGroupItem
import com.junkfood.seal.ui.component.SelectionGroupRow
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
import org.koin.dsl.module

enum class Filter {
    All,
    Downloading,
    Canceled,
    Finished;

    @Composable
    @ReadOnlyComposable
    fun label(): String =
        when (this) {
            All -> stringResource(R.string.all)
            Downloading -> stringResource(R.string.status_downloading)
            Canceled -> stringResource(R.string.status_canceled)
            Finished -> stringResource(R.string.status_completed)
        }

    fun predict(entry: Pair<Task, Task.State>): Boolean {
        if (this == All) return true
        val state = entry.second.downloadState
        return when (this) {
            Downloading -> {
                when (state) {
                    is Task.DownloadState.FetchingInfo,
                    Task.DownloadState.Idle,
                    Task.DownloadState.ReadyWithInfo,
                    is Task.DownloadState.Running -> true
                    else -> false
                }
            }
            Canceled -> {
                state is Task.DownloadState.Error || state is Task.DownloadState.Canceled
            }
            Finished -> {
                state is Task.DownloadState.Completed
            }
            else -> {
                true
            }
        }
    }
}

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
    val view = LocalView.current
    DownloadPageImplV2(
        modifier = modifier,
        taskDownloadStateMap = downloader.getTaskStateMap(),
        processCount = processCount,
        downloadCallback = {
            view.slightHapticFeedback()
            dialogViewModel.postAction(Action.ShowSheet())
        },
        navigateToSettings = navigateToSettings,
        navigateToDownloads = navigateToDownloads,
        onNavigateToTaskList = onNavigateToTaskList,
    ) { task, state ->
        when (state) {
            is Task.DownloadState.Canceled -> {
                downloader.restart(task)
            }
            is Task.DownloadState.Completed -> {}
            is Task.DownloadState.Error -> {}
            is Task.DownloadState.Cancelable,
            Task.DownloadState.Idle,
            Task.DownloadState.ReadyWithInfo -> {
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
    taskDownloadStateMap: SnapshotStateMap<Task, Task.State>,
    processCount: Int = 0,
    downloadCallback: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToDownloads: () -> Unit = {},
    onNavigateToTaskList: () -> Unit = {},
    onActionPost: (Task, Task.DownloadState) -> Unit,
) {
    var activeFilter by remember { mutableStateOf(Filter.All) }
    val filteredMap = taskDownloadStateMap.filter { activeFilter.predict(it.toPair()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = { FABs(modifier = Modifier, downloadCallback = downloadCallback) },
    ) {
        val containerColor =
            MaterialTheme.colorScheme.run {
                if (LocalDarkTheme.current.isDarkTheme()) surfaceContainer
                else surfaceContainerLowest
            }
        val lazyListState = rememberLazyListState()
        val firstVisibleItem by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            LazyColumn(
                state = lazyListState,
                contentPadding =
                    PaddingValues(
                        bottom =
                            80.dp +
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding()
                    ),
            ) {
                item { Surface { Spacer(modifier = Modifier.height(24.dp)) } }
                stickyHeader {
                    Surface {
                        Column {
                            TopAppBar(
                                title = {
                                    Text(
                                        stringResource(R.string.download_queue),
                                        style =
                                            MaterialTheme.typography.titleLarge.copy(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 8.dp),
                                navigationIcon = {
                                    IconButton(onClick = {}) {
                                        Icon(
                                            Icons.Outlined.Menu,
                                            stringResource(R.string.show_more_actions),
                                            modifier = Modifier,
                                        )
                                    }
                                },
                            )
                            SelectionGroupRow(
                                modifier =
                                    Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp)
                            ) {
                                Filter.entries.forEach {
                                    SelectionGroupItem(
                                        selected = activeFilter == it,
                                        onClick = { activeFilter = it },
                                    ) {
                                        Text(it.label())
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            if (firstVisibleItem != 0) {
                                HorizontalDivider(thickness = Dp.Hairline)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier =
                            Modifier.padding(end = 24.dp, start = 16.dp)
                                .padding(top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Row(
                                modifier =
                                    Modifier.padding(vertical = 6.dp)
                                        .padding(start = 8.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "5 videos, 3 audios",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        FilledIconButton(
                            onClick = {},
                            modifier = Modifier.padding(end = 4.dp).size(32.dp),
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = containerColor
                                ),
                        ) {
                            Icon(
                                imageVector =
                                    if (true) Icons.AutoMirrored.Outlined.List
                                    else Icons.Outlined.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        FilledIconButton(
                            onClick = {},
                            modifier = Modifier.size(32.dp),
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = containerColor
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                items(
                    filteredMap.toList().sortedBy { (_, state) -> state.downloadState },
                    key = { (task, state) -> task.id },
                ) { (task, state) ->
                    with(state.viewState) {
                        VideoCardV2(
                            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 20.dp),
                            thumbnailModel = R.drawable.sample2,
                            title = stringResource(R.string.video_title_sample_text),
                            uploader = stringResource(R.string.video_creator_sample_text),
                            duration = duration,
                            fileSizeApprox = fileSizeApprox,
                            actionButton = {
                                ActionButton(
                                    modifier = Modifier,
                                    downloadState = state.downloadState,
                                ) {
                                    onActionPost(task, state.downloadState)
                                }
                            },
                            stateIndicator = {
                                StateIndicator(
                                    modifier = Modifier,
                                    downloadState = state.downloadState,
                                )
                            },
                        ) {}
                    }
                }
            }
            if (filteredMap.isEmpty()) {
                Spacer(modifier = Modifier.weight(0.3f))
                DownloadQueuePlaceholder(modifier = Modifier)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun Title(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 12.dp)) {
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
    val view = LocalView.current
    TooltipBox(
        modifier = modifier,
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.settings)) } },
    ) {
        IconButton(
            onClick = {
                view.slightHapticFeedback()
                onClick()
            },
            modifier = Modifier,
        ) {
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
    val view = LocalView.current

    TooltipBox(
        modifier = modifier,
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.running_tasks)) } },
    ) {
        IconButton(
            onClick = {
                view.slightHapticFeedback()
                onClick()
            },
            modifier = Modifier,
        ) {
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
    val view = LocalView.current
    TooltipBox(
        modifier = modifier,
        state = rememberTooltipState(),
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.downloads_history)) } },
    ) {
        IconButton(
            onClick = {
                view.slightHapticFeedback()
                onClick()
            },
            modifier = Modifier,
        ) {
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
                    val map = mutableStateMapOf<Task, Task.State>()
                    val list =
                        listOf(
                            Task.State(Task.DownloadState.Idle, null, Task.ViewState()),
                            Task.State(
                                Task.DownloadState.Canceled(Task.RestartableAction.Download),
                                null,
                                Task.ViewState(),
                            ),
                            Task.State(Task.DownloadState.Completed(null), null, Task.ViewState()),
                        )
                    map.run {
                        repeat(3) {
                            put(Task(url = "$it", preferences = PreferencesMock), list[it % 3])
                        }
                    }

                    return map
                }

                override fun cancel(task: Task) {}

                override fun restart(task: Task) {}

                override fun enqueue(task: Task) {}

                override fun enqueue(task: Task, state: Task.State) {}
            }
        }
    }

    KoinApplication(application = { modules(module) }) {
        val downloader: DownloaderV2 = koinInject()
        SealTheme {
            Column() {
                DownloadPageImplV2(
                    taskDownloadStateMap = downloader.getTaskStateMap(),
                    processCount = 2,
                    onActionPost = { task, state ->
                        when (state) {
                            is Task.DownloadState.Canceled -> {
                                downloader.restart(task)
                            }
                            is Task.DownloadState.Completed -> {}
                            is Task.DownloadState.Error -> {
                                downloader.restart(task)
                            }
                            is Task.DownloadState.Cancelable,
                            Task.DownloadState.Idle,
                            Task.DownloadState.ReadyWithInfo -> {
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
