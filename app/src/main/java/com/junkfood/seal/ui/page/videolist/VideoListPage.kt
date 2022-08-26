package com.junkfood.seal.ui.page.videolist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(
    videoListViewModel: VideoListViewModel = hiltViewModel(), onBackPressed: () -> Unit
) {
    val viewState = videoListViewModel.stateFlow.collectAsState().value
    val videoListFlow = videoListViewModel.videoListFlow

    val videoList = videoListFlow.collectAsState(ArrayList())
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val scope = rememberCoroutineScope()

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

    val selectedItemIds =
        remember(videoList.value, isSelectEnabled, viewState) { mutableStateListOf<Int>() }
    val selectedVideos = remember(selectedItemIds.size) {
        mutableStateOf(
            videoList.value.count { info ->
                selectedItemIds.contains(info.id) && info.filterByType(
                    videoFilter = true,
                    audioFilter = false
                )
            })
    }
    val selectedAudioFiles = remember(selectedItemIds.size) {
        mutableStateOf(
            videoList.value.count { info ->
                selectedItemIds.contains(info.id) && info.filterByType(
                    videoFilter = false,
                    audioFilter = true
                )
            })
    }
    val visibleItemCount = remember(
        videoList.value, viewState
    ) { mutableStateOf(videoList.value.count { it.filterSort(viewState) }) }

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
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.downloads_history)
                    )
                },
                navigationIcon = {
                    BackButton(Modifier.padding(horizontal = 8.dp)) {
                        onBackPressed()
                    }
                }, actions = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = { isSelectEnabled = !isSelectEnabled }) {
                        Icon(Icons.Outlined.Checklist, null)
                    }
                }, scrollBehavior = scrollBehavior, contentPadding = PaddingValues()
            )
        }, bottomBar = {
            AnimatedVisibility(
                isSelectEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BottomAppBar(
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Checkbox(
                        checked = selectedItemIds.size == visibleItemCount.value && selectedItemIds.isNotEmpty(),
                        onCheckedChange = {
                            if (selectedItemIds.size == visibleItemCount.value) {
                                selectedItemIds.clear()
                            } else {
                                for (item in videoList.value) {
                                    if (!selectedItemIds.contains(item.id)
                                        && item.filterSort(viewState)
                                    ) {
                                        selectedItemIds.add(item.id)
                                    }
                                }
                            }
                        })
                    Text(
                        stringResource(R.string.multiselect_item_count).format(
                            selectedVideos.value,
                            selectedAudioFiles.value
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { showRemoveMultipleItemsDialog = true },
                        enabled = selectedItemIds.isNotEmpty()
                    ) {
                        Icon(Icons.Outlined.DeleteSweep, null)
                    }
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(innerPadding),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
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
            items(
                videoList.value.reversed().sortedBy { it.filterByType() },
                key = { info -> info.id }) {
                with(it) {
                    AnimatedVisibility(
                        visible = it.filterSort(viewState),
                        exit = shrinkVertically() + fadeOut(),
                        enter = expandVertically() + fadeIn()
                    ) {
                        MediaListItem(
                            modifier = Modifier,
                            title = videoTitle,
                            author = videoAuthor,
                            thumbnailUrl = thumbnailUrl,
                            videoPath = videoPath,
                            videoUrl = videoUrl,
                            isSelectEnabled = isSelectEnabled,
                            isSelected = selectedItemIds.contains(id),
                            onSelect = {
                                if (selectedItemIds.contains(id)) selectedItemIds.remove(id)
                                else selectedItemIds.add(id)
                            },
                            onClick = { FileUtil.openFileInURI(videoPath) }
                        ) { videoListViewModel.showDrawer(scope, it) }
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
                        text = stringResource(R.string.delete_file),
                        checked = deleteFile
                    ) { deleteFile = !deleteFile }
                }
            }, confirmButton = {
                ConfirmButton {
                    scope.launch {
                        selectedItemIds.forEach { id ->
                            if (deleteFile) {
                                val info = DatabaseUtil.getInfoById(id)
                                File(info.videoPath).delete()
                            }
                            DatabaseUtil.deleteInfoById(id)
                        }
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




