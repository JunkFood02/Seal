package com.junkfood.seal.ui.page.videolist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DeleteDialog
import com.junkfood.seal.ui.component.VideoListItem
import com.junkfood.seal.util.DatabaseUtil
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
    val openDialog = remember { mutableStateOf(-1) }
    if (openDialog.value != -1) {
        DeleteDialog({ openDialog.value = -1 }) {
            DatabaseUtil.deleteInfoById(openDialog.value)
        }
    }

    BackHandler() {
        if (!videoListViewModel.hideDrawer(scope))
            navController.popBackStack()
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.downloads_history),
                        //fontSize = MaterialTheme.typography.displaySmall.fontSize
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
            ) {
                item {
                    for (item in list.value) {
                        VideoListItem(
                            title = item.videoTitle,
                            author = item.videoAuthor,
                            thumbnailUrl = item.thumbnailUrl,
                            videoUrl = item.videoUrl,
                            isAudio = item.videoPath.contains(".mp3"),
                            onClick = { FileUtil.openFile(item.videoPath) }
                        ) {
                            videoListViewModel.showDrawer(scope, item)
                        }
                    }
                }

            }
        })
    VideoDetailDrawer()

}