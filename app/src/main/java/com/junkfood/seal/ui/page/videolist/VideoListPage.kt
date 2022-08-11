package com.junkfood.seal.ui.page.videolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.util.FileUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(
    videoListViewModel: VideoListViewModel = hiltViewModel(), onBackPressed: () -> Unit
) {
    val viewState = videoListViewModel.viewState.collectAsState()
    val videoList = viewState.value.videoListFlow.collectAsState(ArrayList())
    val audioList = viewState.value.audioListFlow.collectAsState(ArrayList())
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val scope = rememberCoroutineScope()
    val audioFilter = remember { mutableStateOf(false) }
    val videoFilter = remember { mutableStateOf(false) }

    val filterSet = remember(videoList.value, audioList.value) {
        with(mutableSetOf<String>()) {
            videoList.value.forEach { add(it.extractor) }
            audioList.value.forEach { add(it.extractor) }
            this
        }
    }

    var activeFilter by remember(filterSet) { mutableStateOf(-1) }

    fun filterByExtractor(extractor: String): Boolean {
        return if (activeFilter == -1) true else filterSet.elementAt(activeFilter) == extractor
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

        Column(
            Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
            ) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        FilterChip(
                            selected = audioFilter.value,
                            onClick = {
                                audioFilter.value = !audioFilter.value
                                if (videoFilter.value) videoFilter.value = false
                            },
                            label = stringResource(id = R.string.audio),
                        )

                        FilterChip(
                            selected = videoFilter.value,
                            onClick = {
                                videoFilter.value = !videoFilter.value
                                if (audioFilter.value) audioFilter.value = false
                            },
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
                                        selected = activeFilter == i,
                                        onClick = {
                                            activeFilter =
                                                if (activeFilter == i) -1
                                                else i
                                        },
                                        label = filterSet.elementAt(i)
                                    )

                                }
                            }
                        }
                    }
                }
                items(videoList.value.reversed()) {
                    AnimatedVisibility(visible = !audioFilter.value && filterByExtractor(it.extractor)) {
                        with(it) {
                            VideoListItem(
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoPath = videoPath,
                                videoUrl = videoUrl,
                                onClick = { FileUtil.openFileInURI(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, this@with) }

                        }
                    }

                }
                items(audioList.value.reversed()) {
                    AnimatedVisibility(
                        visible = !videoFilter.value && filterByExtractor(it.extractor)
                    ) {
                        with(it) {
                            AudioListItem(
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoPath = videoPath,
                                videoUrl = videoUrl,
                                onClick = { FileUtil.openFileInURI(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, this@with) }
                        }

                    }
                }
            }
        }
    }
    VideoDetailDrawer()
}




