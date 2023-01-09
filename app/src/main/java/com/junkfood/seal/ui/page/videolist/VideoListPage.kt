package com.junkfood.seal.ui.page.videolist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.FilterChip
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.MediaListItem
import com.junkfood.seal.ui.component.MultiChoiceItem
import com.junkfood.seal.util.AUDIO_REGEX
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getFileSize
import kotlinx.coroutines.launch
import java.io.File

fun DownloadedVideoInfo.filterByType(
    videoFilter: Boolean = false,
    audioFilter: Boolean = true
): Boolean {
//    Log.d(TAG, "filterByType: ${this.videoPath}")
    return if (!(videoFilter || audioFilter))
        true
    else if (audioFilter)
        this.videoPath.contains(Regex(AUDIO_REGEX))
    else !this.videoPath.contains(Regex(AUDIO_REGEX))
}

fun DownloadedVideoInfo.filterByExtractor(extractor: String?): Boolean {
    return extractor.isNullOrEmpty() || (this.extractor == extractor)
}

private const val TAG = "VideoListPage"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun VideoListPage(
    videoListViewModel: VideoListViewModel = hiltViewModel(), onBackPressed: () -> Unit
) {
    val viewState = videoListViewModel.stateFlow.collectAsStateWithLifecycle().value
    val videoListFlow = videoListViewModel.videoListFlow

    val videoList = videoListFlow.collectAsState(ArrayList()).value
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val scope = rememberCoroutineScope()

    val fileSizeMap = remember(videoList.size) {
        mutableMapOf<Int, Long>().apply {
            putAll(videoList.map { Pair(it.id, it.videoPath.getFileSize()) })
        }
    }


    var isSelectEnabled by remember { mutableStateOf(false) }
    var showRemoveMultipleItemsDialog by remember { mutableStateOf(false) }

    val filterSet = videoListViewModel.filterSetFlow.collectAsState(mutableSetOf()).value
    fun DownloadedVideoInfo.filterSort(viewState: VideoListViewModel.VideoListViewState): Boolean {
        return filterByType(
            videoFilter = viewState.videoFilter,
            audioFilter = viewState.audioFilter
        ) && filterByExtractor(
            filterSet.elementAtOrNull(viewState.activeFilterIndex)
        )
    }

    @Composable
    fun FilterChips(modifier: Modifier = Modifier) {
        Row(
            modifier
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
                .selectableGroup()
        ) {
            FilterChip(
                selected = viewState.audioFilter,
                onClick = { videoListViewModel.clickAudioFilter() },
                label = stringResource(id = R.string.audio),
            )

            FilterChip(
                selected = viewState.videoFilter,
                onClick = { videoListViewModel.clickVideoFilter() },
                label = stringResource(id = R.string.video),
            )
            if (filterSet.size > 1) {
                Row {
                    Divider(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .height(24.dp)
                            .width(1f.dp)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    for (i in 0 until filterSet.size) {
                        FilterChip(
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
    ) { mutableStateOf(videoList.count { it.filterSort(viewState) }) }

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

    BackHandler(isSelectEnabled) {
        isSelectEnabled = false
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    IconToggleButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onCheckedChange = { isSelectEnabled = !isSelectEnabled },
                        checked = isSelectEnabled
                    ) {
                        Icon(
                            Icons.Outlined.Checklist,
                            contentDescription = stringResource(R.string.multiselect_mode)
                        )
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
                                            && item.filterSort(viewState)
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
        val cellCount = when (LocalWindowWidthState.current) {
            WindowWidthSizeClass.Expanded -> 2
            else -> 1
        }
        val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(cellCount) }
        LazyVerticalGrid(
            modifier = Modifier
                .padding(innerPadding), columns = GridCells.Fixed(cellCount)
        ) {
            item(span = span) {
                FilterChips(Modifier.fillMaxWidth())
            }
            for (info in videoList) {
                val fileSize =
                    fileSizeMap.getOrElse(info.id) { File(info.videoPath).length() }

                item(
                    key = info.id,
                    contentType = { info.videoPath.contains(AUDIO_REGEX) }) {
                    with(info) {
                        AnimatedVisibility(
                            visible = info.filterSort(viewState),
                            exit = shrinkVertically() + fadeOut(),
                            enter = expandVertically() + fadeIn()
                        ) {
                            MediaListItem(
                                modifier = Modifier,
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoPath = videoPath,
                                videoFileSize = fileSize / (1024f * 1024f),
                                videoUrl = videoUrl,
                                isSelectEnabled = { isSelectEnabled },
                                isSelected = { selectedItemIds.contains(id) },
                                onSelect = {
                                    if (selectedItemIds.contains(id)) selectedItemIds.remove(id)
                                    else selectedItemIds.add(id)
                                },
                                onClick = { FileUtil.openFile(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, info) }
                        }
                    }
                }
            }

        }


    }
    VideoDetailDrawer()
    if (showRemoveMultipleItemsDialog) {
        var deleteFile by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showRemoveMultipleItemsDialog = false },
            icon = { Icon(Icons.Outlined.DeleteSweep, null) },
            title = { Text(stringResource(R.string.delete_info)) }, text = {
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        text = stringResource(R.string.delete_multiple_items_msg).format(
                            selectedItemIds.size
                        )
                    )
                    MultiChoiceItem(
                        text = stringResource(R.string.delete_file) + " (%.2f M)".format(
                            selectedFileSizeSum / (1024f * 1024f)
                        ),
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




