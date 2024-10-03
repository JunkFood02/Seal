package com.junkfood.seal.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.junkfood.seal.ui.page.NavigationDrawer
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.svg.DynamicColorImageVectors
import com.junkfood.seal.ui.svg.drawablevectors.download
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.makeToast
import kotlinx.coroutines.launch
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
    onMenuOpen: (() -> Unit)? = null,
    dialogViewModel: DownloadDialogViewModel,
    downloader: DownloaderV2 = koinInject(),
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    DownloadPageImplV2(
        modifier = modifier,
        taskDownloadStateMap = downloader.getTaskStateMap(),
        downloadCallback = {
            view.slightHapticFeedback()
            dialogViewModel.postAction(Action.ShowSheet())
        },
        onMenuOpen = onMenuOpen,
    ) { task, state ->
        when (state) {
            is Task.DownloadState.Canceled,
            is Task.DownloadState.Error -> {
                downloader.restart(task)
            }
            is Task.DownloadState.Completed -> {
                state.filePath?.let {
                    FileUtil.openFile(it) { context.makeToast(R.string.file_unavailable) }
                }
            }
            Task.DownloadState.Idle,
            Task.DownloadState.ReadyWithInfo,
            is Task.DownloadState.FetchingInfo,
            is Task.DownloadState.Running -> {
                downloader.cancel(task)
            }
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

        is DownloadDialogViewModel.SelectionState.PlaylistSelection -> {
            PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )
        }

        DownloadDialogViewModel.SelectionState.Idle -> {}
    }
}

@Composable
fun DownloadPageImplV2(
    modifier: Modifier = Modifier,
    taskDownloadStateMap: SnapshotStateMap<Task, Task.State>,
    downloadCallback: () -> Unit = {},
    onMenuOpen: (() -> Unit)? = null,
    onActionPost: (Task, Task.DownloadState) -> Unit,
) {
    var activeFilter by remember { mutableStateOf(Filter.All) }
    val filteredMap = taskDownloadStateMap.filter { activeFilter.predict(it.toPair()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = { FABs(modifier = Modifier, downloadCallback = downloadCallback) },
    ) { windowInsetsPadding ->
        val lazyListState = rememberLazyListState()
        val firstVisibleItem by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

        Column(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                LazyColumn(state = lazyListState, contentPadding = windowInsetsPadding) {
                    item { Surface { Spacer(modifier = Modifier.height(24.dp)) } }
                    stickyHeader {
                        Surface {
                            Column {
                                Header(onMenuOpen = onMenuOpen)
                                SelectionGroupRow(
                                    modifier =
                                        Modifier.horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 20.dp)
                                ) {
                                    Filter.entries.forEach { filter ->
                                        SelectionGroupItem(
                                            selected = activeFilter == filter,
                                            onClick = {
                                                if (activeFilter == filter) {
                                                    scope.launch {
                                                        lazyListState.animateScrollToItem(0)
                                                    }
                                                } else {
                                                    activeFilter = filter
                                                }
                                            },
                                        ) {
                                            Text(filter.label())
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

                    if (filteredMap.isNotEmpty()) {
                        item {
                            SubHeader(
                                videoCount = filteredMap.size,
                                onToggleView = { context.makeToast("Not implemented yet!") },
                                onShowMenu = { context.makeToast("Not implemented yet!") },
                            )
                        }
                    }

                    items(
                        filteredMap.toList().sortedBy { (_, state) -> state.downloadState },
                        key = { (task, state) -> task.id },
                    ) { (task, state) ->
                        with(state.viewState) {
                            VideoCardV2(
                                modifier =
                                    Modifier.padding(horizontal = 24.dp).padding(bottom = 20.dp),
                                viewState = this,
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
                            ) {
                                context.makeToast("Not implemented yet!")
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
        if (filteredMap.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                DownloadQueuePlaceholder(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(modifier: Modifier = Modifier, onMenuOpen: (() -> Unit)? = null) {
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
        modifier = modifier.padding(horizontal = 8.dp),
        navigationIcon = {
            onMenuOpen?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = stringResource(R.string.show_navigation_drawer),
                        modifier = Modifier,
                    )
                }
            }
        },
        windowInsets = WindowInsets(0.dp),
    )
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

@Composable
fun SubHeader(
    modifier: Modifier = Modifier,
    containerColor: Color =
        MaterialTheme.colorScheme.run {
            if (LocalDarkTheme.current.isDarkTheme()) surfaceContainer else surfaceContainerLowest
        },
    videoCount: Int = 0,
    audioCount: Int = 0,
    isGridView: Boolean = true,
    onToggleView: () -> Unit,
    onShowMenu: () -> Unit,
) {
    Row(
        modifier =
            modifier.padding(end = 24.dp, start = 24.dp).padding(top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(R.plurals.video_count, videoCount).format(videoCount),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        FilledIconButton(
            onClick = onToggleView,
            modifier = Modifier.clearAndSetSemantics {}.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor),
        ) {
            Icon(
                imageVector =
                    if (isGridView) Icons.AutoMirrored.Outlined.List else Icons.Outlined.GridView,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(Modifier.width(4.dp))

        FilledIconButton(
            onClick = onShowMenu,
            modifier = Modifier.clearAndSetSemantics {}.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor),
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DrawerPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    NavigationDrawer(drawerState = drawerState, onNavigateToRoute = {}, onDismissRequest = {}) {}
}

@Composable
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
                        repeat(9) {
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
                    onMenuOpen = {},
                )
            }
        }
    }
}
