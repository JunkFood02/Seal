package com.junkfood.seal.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.PreviewThemeLight

@Composable
fun FormatListItem(
    id: String = "248",
    note: String = "1080p",
    resolution: String = "1920x1080",
    vcodec: String = "h264",
    acodec: String = "aac",
    ext: String = "mp4",
    bitRate: Float = 745.67f,
    fileSize: Long = 1024 * 1024 * 69,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {

}


@Composable
fun FormatItem(
    modifier: Modifier = Modifier,
    id: String = "248",
    formatDesc: String = "247 - 1280x720 (720p)",
    note: String = "1080p",
    resolution: String = "1920x1080",
    vcodec: String = "h264",
    acodec: String = "aac",
    ext: String = "mp4",
    bitRate: Float = 745.67f,
    fileSize: Long = 1024 * 1024 * 69,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val outlineColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.run {
            if (selected) primary else outlineVariant
        })

    val elevation by animateDpAsState(if (selected) 15.dp else 0.dp)
    val animateDp by animateDpAsState(if (selected) 2.dp else 1.dp)

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected) { onClick() }
            .border(
                width = 1.dp,
                color = outlineColor,
                shape = MaterialTheme.shapes.medium
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(elevation))
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Text(
                text = formatDesc,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                color = MaterialTheme.colorScheme.run { if (selected) primary else onSurface })

            val bitRateText = "%.1f Kbps".format(bitRate)
            val fileSizeText = "%.2f M".format(fileSize.toFloat() / 1024 / 1024)
            val codecText = "$ext ($vcodec $acodec)"
            Text(
                text = connectWithDots(fileSizeText, bitRateText),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = codecText,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

}

@Composable
@Preview
fun PreviewFormat() {
    PreviewThemeLight {
        var selected by remember { mutableStateOf(-1) }
        Surface {
            Column() {
                FormatSubtitle(text = stringResource(R.string.video_only))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0..4) {
                        item {
                            FormatItem(selected = selected == i) { selected = i }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormatSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 12.dp),
        color = color,
        style = MaterialTheme.typography.titleMedium
    )
}


fun connectWithDots(vararg strings: String): String {
    val builder = StringBuilder(strings.first())
    for (s in strings.asList().subList(1, strings.size)) {
        builder.append(" · $s")
    }
    return builder.toString()
}