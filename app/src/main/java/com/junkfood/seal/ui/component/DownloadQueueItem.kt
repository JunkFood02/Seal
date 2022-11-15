package com.junkfood.seal.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.PreviewThemeLight


@Composable
@Preview
fun DownloadQueue(modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(MaterialTheme.shapes.medium)) {
        DownloadQueueItem()
        DownloadQueueItem()
        DownloadQueueItem()
    }


}

@Composable
@Preview
fun DownloadQueueItem(
    modifier: Modifier = Modifier,
    imageModel: Any = R.drawable.sample,
    title: String = "sample title ".repeat(5),
    author: String = "author sample ".repeat(5),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
//        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { }) {
            AsyncImage(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.4f)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .aspectRatio(16f / 10f, matchHeightConstraintsFirst = true),
                model = imageModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(end = 8.dp)
                    .weight(1f)
                    .fillMaxHeight(), verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
@Preview
fun PlaylistPreview() {
    var selected by remember { mutableStateOf(false) }
    Column() {
        PreviewThemeLight {
            PlaylistItem(selected = selected) { selected = !selected }
            PlaylistItem(selected = selected) { selected = !selected }
            PlaylistItem(selected = selected) { selected = !selected }
        }

    }

}

@Composable
fun PlaylistItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    imageModel: Any = R.drawable.sample,
    title: String = "sample title ".repeat(5),
    author: String = "author sample ".repeat(5),
    onClick: () -> Unit = {},
) {

    val sizeState = animateDpAsState(targetValue = if (selected) 20.dp else 0.dp)
    Surface(modifier = modifier.fillMaxWidth(), color = with(MaterialTheme.colorScheme) {
        if (selected) surfaceColorAtElevation(15.dp) else surface
    }) {
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .selectable(selected) { onClick() }) {
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .padding(end = 4.dp)
                    .weight(0.4f)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(sizeState.value)
                            .padding(2.dp),
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

            }
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
//                    .padding(end = 0.dp)
                    .weight(1f)
                    .fillMaxHeight(), verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}