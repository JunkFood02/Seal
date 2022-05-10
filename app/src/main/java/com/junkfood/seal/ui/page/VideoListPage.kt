package com.junkfood.seal.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.ui.component.VideoListItem

@Composable
fun VideoListPage(navController: NavController) {

    Column() {
        LargeTopAppBar(
            title = {
                Text(
                    text = "Downloads",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize
                )
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            }, modifier = Modifier.padding(9f.dp)
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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