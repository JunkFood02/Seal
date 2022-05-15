package com.junkfood.seal.ui.page.videolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.AudioListItem
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.VideoListItem
import com.junkfood.seal.util.FileUtil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun VideoListPage(
    navController: NavController, videoListViewModel: VideoListViewModel = hiltViewModel()
) {
    val viewState = videoListViewModel.viewState.collectAsState()
    val list = viewState.value.listFlow.collectAsState(ArrayList())
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val scope = rememberCoroutineScope()


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
                    IconButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }, scrollBehavior = scrollBehavior, contentPadding = PaddingValues()
            )
        }
    ) { innerPadding ->
        val audioFilter = remember { mutableStateOf(false) }
        val videoFilter = remember { mutableStateOf(false) }
        val ytbFilter = remember { mutableStateOf(false) }
        val bilibiliFilter = remember { mutableStateOf(false) }
        LazyColumn(
            contentPadding = innerPadding,
        ) {
            item {
                Row() {
                    AnimatedVisibility(visible = !videoFilter.value) {
                        FilterChip(
                            modifier = Modifier.padding(start = 12.dp),
                            selected = audioFilter.value,
                            onClick = { audioFilter.value = !audioFilter.value },
                            label = {
                                Text(text = "Audio")
                            }, trailingIcon = {
                                AnimatedVisibility(visible = audioFilter.value) {
                                    Icon(Icons.Outlined.Clear, "", modifier = Modifier.size(18.dp))
                                }
                            }

                        )
                    }
                    AnimatedVisibility(visible = !audioFilter.value) {
                        FilterChip(
                            modifier = Modifier.padding(start = 12.dp),
                            selected = videoFilter.value,
                            onClick = { videoFilter.value = !videoFilter.value },
                            label = {
                                Text(text = "Video")
                            }, trailingIcon = {
                                AnimatedVisibility(visible = videoFilter.value) {
                                    Icon(Icons.Outlined.Clear, "", modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }


                    FilterChip(
                        modifier = Modifier.padding(start = 12.dp),
                        selected = bilibiliFilter.value,
                        onClick = { bilibiliFilter.value = !bilibiliFilter.value },
                        label = {
                            Text(text = "Bilibili")
                        }, trailingIcon = {
                            AnimatedVisibility(visible = bilibiliFilter.value) {
                                Icon(Icons.Outlined.Clear, "", modifier = Modifier.size(18.dp))
                            }
                        }
                    )
                    FilterChip(
                        modifier = Modifier.padding(start = 12.dp),
                        selected = ytbFilter.value,
                        onClick = { ytbFilter.value = !ytbFilter.value },
                        label = {
                            Text(text = "YouTube")
                        }, trailingIcon = {
                            AnimatedVisibility(visible = ytbFilter.value) {
                                Icon(Icons.Outlined.Clear, "", modifier = Modifier.size(18.dp))
                            }
                        }
                    )
                }
            }
            items(list.value.reversed()) {
                AnimatedVisibility(visible = !audioFilter.value) {
                    with(it) {
                        if (!videoPath.contains(".mp3"))
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
            items(list.value.reversed()) {
                AnimatedVisibility(visible = !videoFilter.value) {
                    with(it) {
                        if (videoPath.contains(".mp3"))
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
    VideoDetailDrawer()
    if (viewState.value.showDialog)
        RemoveItemDialog()
}


