package com.junkfood.seal.ui.page.videolist

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.FilterChip
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.MediaListItem
import com.junkfood.seal.util.FileUtil

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    val filterSet = videoListViewModel.filterSetFlow.collectAsState(mutableSetOf()).value
    fun DownloadedVideoInfo.filterSort(viewState: VideoListViewModel.VideoListViewState): Boolean {
        return filterByType(
            videoFilter = viewState.videoFilter,
            audioFilter = viewState.audioFilter
        ) && filterByExtractor(
            filterSet.elementAtOrNull(viewState.activeFilterIndex)
        )
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
                }, scrollBehavior = scrollBehavior, contentPadding = PaddingValues()
            )

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
                            onClick = { FileUtil.openFileInURI(videoPath) }
                        ) { videoListViewModel.showDrawer(scope, it) }
                    }
                }
            }
        }

    }
    VideoDetailDrawer()

}




