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
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.util.FileUtil

data class Filter(
    val name: String,
    val regex: String,
    val valueState: MutableState<Boolean>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(
    navController: NavController, videoListViewModel: VideoListViewModel = hiltViewModel()
) {
    val viewState = videoListViewModel.viewState.collectAsState()
    val videoList = viewState.value.videoListFlow.collectAsState(ArrayList())
    val audioList = viewState.value.audioListFlow.collectAsState(ArrayList())
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val scope = rememberCoroutineScope()
    val audioFilter = remember { mutableStateOf(false) }
    val videoFilter = remember { mutableStateOf(false) }


    val filterList1 = listOf(
        Filter("Bilibili", "(b23\\.tv)|(bilibili)", remember { mutableStateOf(false) }),
        Filter("YouTube", "youtu", remember { mutableStateOf(false) }),
        Filter("NicoNico", "nico", remember { mutableStateOf(false) })
    )


    fun websiteFilter(url: String, filter: Filter): Boolean {
        return (!filter.valueState.value or url.contains(Regex(filter.regex)))
    }


    fun urlFilterInList(url: String): Boolean {
        var res = true
        for (filter in filterList1) {
            res = res.and(websiteFilter(url, filter))
        }
        return res
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
                        navController.popBackStack()
                    }
                }, scrollBehavior = scrollBehavior, contentPadding = PaddingValues()
            )

        }
    ) { innerPadding ->

        Column(
            Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.padding(
                    bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                ),
            ) {
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp)
                    ) {
                        FilterChipWithIcon(
                            select = audioFilter.value,
                            onClick = {
                                audioFilter.value = !audioFilter.value
                                if (videoFilter.value) videoFilter.value = false
                            },
                            label = stringResource(id = R.string.audio)
                        )

                        FilterChipWithIcon(
                            select = videoFilter.value,
                            onClick = {
                                videoFilter.value = !videoFilter.value
                                if (audioFilter.value) audioFilter.value = false
                            },
                            label = stringResource(id = R.string.video)
                        )

                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .height(24.dp)
                                .width(1.5f.dp)
                                .align(Alignment.CenterVertically),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        for (filter in filterList1) {
                            with(filter) {
                                FilterChipWithIcon(
                                    select = valueState.value,
                                    onClick = {
                                        filterList1.forEach {
                                            if (it != this) it.valueState.value = false
                                        }
                                        valueState.value = !valueState.value
                                    },
                                    label = name
                                )
                            }
                        }
                    }
                }
                items(videoList.value.reversed()) {
                    AnimatedVisibility(
                        visible = !audioFilter.value and urlFilterInList(it.videoUrl)
                    )
                    {
                        with(it) {
                            VideoListItem(
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoUrl = videoUrl,
                                onClick = { FileUtil.openFile(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, this@with) }
                        }
                    }
                }
                items(audioList.value.reversed()) {
                    AnimatedVisibility(
                        visible = !videoFilter.value and urlFilterInList(it.videoUrl)
                    ) {
                        with(it) {
                            AudioListItem(
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoUrl = videoUrl,
                                onClick = { FileUtil.openFile(videoPath) }
                            ) { videoListViewModel.showDrawer(scope, this@with) }
                        }
                    }
                }
            }
        }
    }
    VideoDetailDrawer()
    if (viewState.value.showDialog)
        RemoveItemDialog()
}




