package com.junkfood.seal.ui.page

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.database.DownloadedVideoInfo
import com.junkfood.seal.ui.component.DeleteDialog
import com.junkfood.seal.ui.component.VideoListItem
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.FileUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(navController: NavController) {
    val list: State<List<DownloadedVideoInfo>> =
        DatabaseUtil.getInfo().collectAsState(ArrayList())
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val openDialog = remember { mutableStateOf(-1) }

    if (openDialog.value != -1) {
        DeleteDialog({ openDialog.value = -1 }) {
            DatabaseUtil.deleteInfoById(openDialog.value)
            openDialog.value = -1
        }
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
                    for (item in list.value)
                        VideoListItem(
                            title = item.videoTitle,
                            author = item.videoAuthor,
                            thumbnailUrl = item.thumbnailUrl,
                            videoUrl = item.videoUrl,
                            onClick = { FileUtil.openFile(item.videoPath) }
                        ) {
                            openDialog.value = item.id
                        }
                }

            }

        })

}