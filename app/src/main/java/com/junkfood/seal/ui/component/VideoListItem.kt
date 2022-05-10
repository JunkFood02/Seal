package com.junkfood.seal.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListItem(
    title: String,
    author: String,
    thumbnailUrl: String,
    videoUrl: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(16f / 9f, matchHeightConstraintsFirst = false),
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .scale(Scale.FILL)
                .crossfade(true)
                .build(),
            contentDescription = null
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                CircularProgressIndicator(modifier = Modifier.requiredSize(32.dp))
            } else {
                SubcomposeAsyncImageContent()
            }

        }

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth(), verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}