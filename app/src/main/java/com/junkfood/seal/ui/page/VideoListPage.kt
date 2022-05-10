package com.junkfood.seal.ui.page

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.junkfood.seal.ui.component.VideoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(navController: NavController) {

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Downloads",
                        //fontSize = MaterialTheme.typography.displaySmall.fontSize
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = {
            LazyColumn() {
                item {
                    Column() {
                        for (i in 1..10) {
                            VideoListItem(
                                title = "少女レイ／初音ミク ▶みきとP ｜Shoujorei／Hatsune Miku▶mikitoP",
                                author = "みきとP / mikitoP Official Channel",
                                thumbnailUrl = "https://img.youtube.com/vi/JW3N-HvU0MA/maxresdefault.jpg",
                                videoUrl = "null"
                            ) {}

                        }
                    }
                }
            }
        })

}