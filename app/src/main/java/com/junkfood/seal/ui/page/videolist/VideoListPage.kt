package com.junkfood.seal.ui.page.videolist

import VideoStreamSVG
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.SVGImage
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.CheckBoxItem
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.MediaListItem
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SealSearchBar
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.util.AUDIO_REGEX
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getFileSize
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.toFileSizeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

fun DownloadedVideoInfo.filterByType(
    videoFilter: Boolean = false,
    audioFilter: Boolean = true
): Boolean {
    return if (!(videoFilter || audioFilter))
        true
    else if (audioFilter)
        this.videoPath.contains(Regex(AUDIO_REGEX))
    else !this.videoPath.contains(Regex(AUDIO_REGEX))
}

fun DownloadedVideoInfo.filterSort(
    viewState: VideoListViewModel.VideoListViewState,
    filterSet: Set<String>
): Boolean {
    return filterByType(
        videoFilter = viewState.videoFilter,
        audioFilter = viewState.audioFilter
    ) && filterByExtractor(
        filterSet.elementAtOrNull(viewState.activeFilterIndex)
    )
}

fun DownloadedVideoInfo.filterByExtractor(extractor: String?): Boolean {
    return extractor.isNullOrEmpty() || (this.extractor == extractor)
}

private const val TAG = "VideoListPage"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun VideoListPage(
    videoListViewModel: VideoListViewModel = hiltViewModel(), onBackPressed: () -> Unit
) {
    val viewState by videoListViewModel.stateFlow.collectAsStateWithLifecycle()
    val fullVideoList by videoListViewModel.videoListFlow.collectAsStateWithLifecycle(emptyList())
    val searchedVideoList by videoListViewModel.searchedVideoListFlow.collectAsStateWithLifecycle(
        emptyList()
    )

    val videoList = if (viewState.isSearching) searchedVideoList else fullVideoList
    val filterSet by videoListViewModel.filterSetFlow.collectAsState(mutableSetOf())

    val scrollBehavior =
        if (fullVideoList.isNotEmpty()) TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true }
        ) else TopAppBarDefaults.pinnedScrollBehavior()

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val softKeyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    val fileSizeMap = remember(fullVideoList.size) {
        mutableMapOf<Int, Long>().apply {
            putAll(videoList.map { Pair(it.id, it.videoPath.getFileSize()) })
        }
    }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    var currentVideoInfoId by rememberSaveable { mutableIntStateOf(0) }

    val currentVideoInfo by remember(currentVideoInfoId) {
        derivedStateOf {
            videoList.find { it.id == currentVideoInfoId } ?: DownloadedVideoInfo()
        }
    }

    var isSelectEnabled by remember { mutableStateOf(false) }
    var showRemoveMultipleItemsDialog by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val lazyGridState = rememberLazyGridState()

    @Composable
    fun FilterChips(modifier: Modifier = Modifier) {
        Row(
            modifier
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                VideoFilterChip(
                    selected = viewState.audioFilter,
                    onClick = { videoListViewModel.clickAudioFilter() },
                    label = stringResource(id = R.string.audio),
                )

                VideoFilterChip(
                    selected = viewState.videoFilter,
                    onClick = { videoListViewModel.clickVideoFilter() },
                    label = stringResource(id = R.string.video),
                )
                if (filterSet.size > 1) {
                    VerticalDivider(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .height(24.dp)
                            .width(1f.dp)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    for (i in 0 until filterSet.size) {
                        VideoFilterChip(
                            selected = viewState.activeFilterIndex == i,
                            onClick = { videoListViewModel.clickExtractorFilter(i) },
                            label = filterSet.elementAt(i)
                        )
                    }
                }
            }
        }
    }

    val selectedItemIds =
        remember(videoList, isSelectEnabled, viewState) { mutableStateListOf<Int>() }
    val selectedVideos = remember(selectedItemIds.size) {
        mutableStateOf(
            videoList.count { info ->
                selectedItemIds.contains(info.id) && info.filterByType(
                    videoFilter = true,
                    audioFilter = false
                )
            })
    }
    val selectedAudioFiles = remember(selectedItemIds.size) {
        mutableStateOf(
            videoList.count { info ->
                selectedItemIds.contains(info.id) && info.filterByType(
                    videoFilter = false,
                    audioFilter = true
                )
            })
    }

    val selectedFileSizeSum by remember(selectedItemIds.size) {
        derivedStateOf {
            selectedItemIds.fold(0L) { acc: Long, id: Int ->
                acc + fileSizeMap.getOrElse(id) { 0L }
            }
        }
    }

    val visibleItemCount = remember(
        videoList, viewState
    ) { mutableStateOf(videoList.count { it.filterSort(viewState, filterSet) }) }

    val checkBoxState by remember(selectedItemIds, visibleItemCount) {
        derivedStateOf {
            if (selectedItemIds.isEmpty())
                ToggleableState.Off
            else if (selectedItemIds.size == visibleItemCount.value && selectedItemIds.isNotEmpty())
                ToggleableState.On
            else
                ToggleableState.Indeterminate
        }
    }

    var showRemoveDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    BackHandler(isSelectEnabled || viewState.isSearching) {
        if (isSelectEnabled) {
            isSelectEnabled = false
        } else {
            videoListViewModel.toggleSearch(false)
        }
    }


    LaunchedEffect(sheetState.targetValue, isSelectEnabled) {
        if (showBottomSheet || isSelectEnabled) {
            softKeyboardController?.hide()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.downloads_history)
                    )
                },
                navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, actions = {
                    if (fullVideoList.isNotEmpty()) {
                        IconToggleButton(
                            modifier = Modifier,
                            onCheckedChange = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                videoListViewModel.toggleSearch(it)
                                if (it) {
                                    scope.launch {
                                        delay(50)
                                        lazyGridState.animateScrollToItem(0)
                                    }
                                }
                            },
                            checked = viewState.isSearching
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                        var expanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopEnd)
                        ) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = stringResource(
                                        id = R.string.show_more_actions
                                    )
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.export_backup)) },
                                    onClick = { showExportDialog = true })
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.AssignmentReturn,
                                            contentDescription = null
                                        )
                                    },
                                    text = { Text(text = stringResource(id = R.string.import_backup)) },
                                    onClick = { showImportDialog = true })
                            }
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, bottomBar = {
            AnimatedVisibility(
                isSelectEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BottomAppBar(
                    modifier = Modifier
                ) {
                    val selectAllText = stringResource(R.string.select_all)
                    TriStateCheckbox(
                        modifier = Modifier.semantics {
                            this.contentDescription = selectAllText
                        },
                        state = checkBoxState,
                        onClick = {
                            when (checkBoxState) {
                                ToggleableState.On -> selectedItemIds.clear()
                                else -> {
                                    for (item in videoList) {
                                        if (!selectedItemIds.contains(item.id)
                                            && item.filterSort(viewState, filterSet)
                                        ) {
                                            selectedItemIds.add(item.id)
                                        }
                                    }
                                }
                            }
                        },
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.multiselect_item_count).format(
                            selectedVideos.value,
                            selectedAudioFiles.value
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(
                        onClick = { showRemoveMultipleItemsDialog = true },
                        enabled = selectedItemIds.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(id = R.string.remove)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (fullVideoList.isEmpty())
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SVGImage(
                        SVGString = VideoStreamSVG,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 72.dp, vertical = 20.dp)
                    )
                    Text(
                        text = stringResource(R.string.no_downloaded_media),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        val cellCount = when (LocalWindowWidthState.current) {
            WindowWidthSizeClass.Expanded -> 2
            else -> 1
        }
        val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(cellCount) }
        LazyVerticalGrid(
            modifier = Modifier.padding(innerPadding),
            columns = GridCells.Fixed(cellCount),
            state = lazyGridState
        ) {
            if (fullVideoList.isNotEmpty()) {
                item(span = span) {
                    Column {
                        AnimatedVisibility(visible = viewState.isSearching) {
                            SealSearchBar(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(vertical = 8.dp),
                                text = viewState.searchText,
                                placeholderText = stringResource(R.string.search_in_downloads),
                                onValueChange = videoListViewModel::updateSearchText
                            )
                        }
                        FilterChips(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }

            }
            for (info in videoList) {
                val fileSize =
                    fileSizeMap.getOrElse(info.id) { File(info.videoPath).length() }

                item(
                    key = info.id,
                    contentType = { info.videoPath.contains(AUDIO_REGEX) }) {
                    with(info) {
                        AnimatedVisibility(
                            visible = info.filterSort(viewState, filterSet),
                            exit = shrinkVertically() + fadeOut(),
                            enter = expandVertically() + fadeIn()
                        ) {
                            MediaListItem(
                                modifier = Modifier,
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoPath = videoPath,
                                videoFileSize = fileSize,
                                videoUrl = videoUrl,
                                isSelectEnabled = { isSelectEnabled },
                                isSelected = { selectedItemIds.contains(id) },
                                onSelect = {
                                    if (selectedItemIds.contains(id)) selectedItemIds.remove(id)
                                    else selectedItemIds.add(id)
                                },
                                onClick = {
                                    FileUtil.openFile(path = videoPath) {
                                        ToastUtil.makeToastSuspend(App.context.getString(R.string.file_unavailable))
                                    }
                                }, onLongClick = {
                                    currentVideoInfoId = info.id
                                    scope.launch {
                                        showBottomSheet = true
                                        delay(50)
                                        sheetState.show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

        }


    }


    if (showBottomSheet) {
        VideoDetailDrawer(
            sheetState = sheetState,
            info = currentVideoInfo,
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showBottomSheet = false
                }
            },
            onDelete = { showRemoveDialog = true })
    }

    var deleteFile by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        RemoveItemDialog(
            info = currentVideoInfo,
            deleteFile = deleteFile,
            onDeleteFileToggled = { deleteFile = it },
            onRemoveConfirm = {
                scope.launch(Dispatchers.IO) {
                    DatabaseUtil.deleteInfoListByIdList(
                        listOf(currentVideoInfoId),
                        deleteFile = deleteFile
                    )
                }
            }, onDismissRequest = {
                showRemoveDialog = false
            })
    }

    if (showRemoveMultipleItemsDialog) {
        SealDialog(
            onDismissRequest = { showRemoveMultipleItemsDialog = false },
            icon = { Icon(Icons.Outlined.DeleteSweep, null) },
            title = { Text(stringResource(R.string.delete_info)) }, text = {
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        text = stringResource(R.string.delete_multiple_items_msg).format(
                            selectedItemIds.size
                        )
                    )
                    CheckBoxItem(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = stringResource(R.string.delete_file) + " (${selectedFileSizeSum.toFileSizeText()})",
                        checked = deleteFile
                    ) { deleteFile = !deleteFile }
                }
            }, confirmButton = {
                ConfirmButton {
                    scope.launch {
                        DatabaseUtil.deleteInfoListByIdList(selectedItemIds, deleteFile)
                    }
                    showRemoveMultipleItemsDialog = false
                    isSelectEnabled = false
                }
            }, dismissButton = {
                DismissButton {
                    showRemoveMultipleItemsDialog = false
                }
            }
        )
    }
}




