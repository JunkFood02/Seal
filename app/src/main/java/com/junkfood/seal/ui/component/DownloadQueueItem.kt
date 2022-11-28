package com.junkfood.seal.ui.component

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.common.LocalWindowWidthState
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
            AsyncImageImpl(
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
        }

    }

}


@Composable
fun PlaylistItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    imageModel: Any = R.drawable.sample,
    title: String = "sample title ".repeat(5),
    author: String? = "author sample ".repeat(5),
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected) { onClick() },
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                modifier = Modifier
                    .padding(start = 4.dp, end = 12.dp)
                    .align(Alignment.CenterVertically), checked = selected, onCheckedChange = null
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .padding(end = 4.dp)
                    .weight(if (LocalWindowWidthState.current == WindowWidthSizeClass.Compact) 2f else 1f)
            ) {
                AsyncImageImpl(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(3f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                author?.let {
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
}
