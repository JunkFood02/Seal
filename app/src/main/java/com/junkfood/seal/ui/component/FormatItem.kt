package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.connectWithBlank
import com.junkfood.seal.util.connectWithDelimiter
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText


@Composable
fun FormatVideoPreview(
    modifier: Modifier = Modifier,
    title: String,
    author: String,
    thumbnailUrl: String,
    duration: Int,
    showButton: Boolean = true,
    isSplittingVideo: Boolean,
    isClippingVideo: Boolean,
    isClippingAvailable: Boolean = false,
    isSplitByChapterAvailable: Boolean = false,
    onTitleClick: () -> Unit = {},
    onImageClicked: () -> Unit = {},
    onClippingToggled: () -> Unit = {},
    onSplittingToggled: () -> Unit = {},
) {
    val imageWeight = when (LocalWindowWidthState.current) {
        WindowWidthSizeClass.Expanded -> 0.25f
        WindowWidthSizeClass.Medium -> 0.30f
        else -> 0.45f
    }
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Top, unbounded = false),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(imageWeight)
            ) {
                MediaImage(
                    modifier = Modifier.clickable(
                        onClick = onImageClicked,
                        onClickLabel = stringResource(
                            id = R.string.share
                        ),
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    imageModel = thumbnailUrl, isAudio = false, contentDescription = stringResource(
                        id = R.string.thumbnail
                    )
                )
                Surface(
                    modifier = Modifier
                        .padding(2.dp)
                        .align(Alignment.BottomEnd),
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    val durationText = duration.toDurationText()
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = durationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f - imageWeight)
                    .fillMaxWidth()
                    .clickable(
                        onClick = onTitleClick,
                        onClickLabel = stringResource(id = R.string.rename),
                        indication = null, interactionSource = remember {
                            MutableInteractionSource()
                        }
                    ), verticalArrangement = Arrangement.Top
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (author != "playlist" && author != "null") Text(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 3.dp),
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showButton) {
            var expanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                IconToggleButton(
                    modifier = Modifier.size(36.dp),
                    onCheckedChange = {
                        if (isClippingVideo) {
                            onClippingToggled()
                        } else if (isSplittingVideo) {
                            onSplittingToggled()
                        } else if (isClippingAvailable && isSplitByChapterAvailable) {
                            expanded = true
                        } else if (isSplitByChapterAvailable) {
                            onSplittingToggled()
                        } else if (isClippingAvailable) {
                            onClippingToggled()
                        }
                    },
                    checked = isClippingVideo || isSplittingVideo
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.ContentCut,
                        contentDescription = stringResource(id = R.string.clip_video)
                    )
                }

                DropdownMenu(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.ContentCut, null) },
                        text = { Text(text = stringResource(id = R.string.clip_video)) },
                        onClick = {
                            onClippingToggled()
                            expanded = false
                        })
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.VerticalSplit, null) },
                        text = { Text(text = stringResource(id = R.string.split_video)) },
                        onClick = {
                            onSplittingToggled()
                            expanded = false
                        })
                }
            }
        }


    }
}

@Composable
@Preview
fun VideoInfoPreview() {
    SealTheme {
        Surface {
            Column {
                FormatVideoPreview(
                    title = stringResource(id = R.string.video_title_sample_text),
                    author = stringResource(
                        id = R.string.video_creator_sample_text
                    ),
                    thumbnailUrl = "",
                    duration = 7890,
                    isSplittingVideo = false,
                    isClippingVideo = false,
                    isSplitByChapterAvailable = true,
                    isClippingAvailable = true
                )
            }
        }
    }
}

@Composable
fun FormatItem(
    formatInfo: Format, selected: Boolean = false,
    outlineColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    with(formatInfo) {
        FormatItem(
            formatDesc = format.toString(),
            resolution = resolution.toString(),
            codec = connectWithBlank(
                vcodec.toString().substringBefore("."),
                acodec.toString().substringBefore(".")
            ).run { if (isNotBlank()) "($this)" else this },
            ext = ext.toString(),
            bitRate = tbr?.toFloat() ?: 0f,
            fileSize = fileSize ?: fileSizeApprox ?: .0,
            outlineColor = outlineColor,
            containerColor = containerColor,
            selected = selected,
            onLongClick = onLongClick,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FormatItem(
    modifier: Modifier = Modifier,
    formatDesc: String = "247 - 1280x720 (720p)",
    resolution: String = "1920x1080",
    codec: String = "h264 aac",
    ext: String = "mp4",
    bitRate: Float = 745.67f,
    fileSize: Double = 1024 * 1024 * 69.0,
    selected: Boolean = false,
    outlineColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val animatedOutlineColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.run {
            if (selected) outlineColor else outlineVariant
        }, animationSpec = tween(100)
    )
    val animatedTitleColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.run {
            if (selected) outlineColor else onSurface
        }, animationSpec = tween(100)
    )
    val animatedContainerColor by animateColorAsState(
        if (selected) containerColor else MaterialTheme.colorScheme.surface,
        animationSpec = tween(100)
    )

    Column(modifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .selectable(selected = selected) { onClick() }
        .combinedClickable(
            onClick = { onClick() },
            onLongClick = onLongClick,
            onLongClickLabel = stringResource(R.string.copy_link)
        )
        .border(
            width = 1.dp, color = animatedOutlineColor, shape = MaterialTheme.shapes.medium
        )
        .background(animatedContainerColor)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Text(
                text = formatDesc,
                style = MaterialTheme.typography.titleSmall,
                minLines = 2,
                maxLines = 2,
                color = animatedTitleColor, overflow = TextOverflow.Clip
            )

            val bitRateText =
                if (bitRate < 1024f) "%.1f Kbps".format(bitRate) else "%.2f Mbps".format(bitRate / 1024f)
            val fileSizeText = fileSize.toFileSizeText()
            val codecText = "$ext $codec".uppercase()
            Text(
                text = connectWithDelimiter(fileSizeText, bitRateText, delimiter = " "),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1
            )

            Text(
                text = codecText,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onSurface, maxLines = 1
            )
        }
    }

}

@Composable
@Preview(
    name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(name = "Light")
fun PreviewFormat() {
    MaterialTheme {
        var selected by remember { mutableStateOf(-1) }
        Surface {
            Column() {
//                FormatSubtitle(text = stringResource(R.string.video_only))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormatPreviewContent(selected) { selected = it }
                }
            }
        }
    }
}

fun LazyGridScope.FormatPreviewContent(selected: Int = 0, onClick: (Int) -> Unit = {}) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        FormatSubtitle(text = "Suggested")
    }
    item(span = { GridItemSpan(maxLineSpan) }) {
        FormatItem(selected = selected == 1) { onClick(1) }
    }
    item(span = { GridItemSpan(maxLineSpan) }) {
        FormatSubtitle(text = stringResource(R.string.video_only))
    }
    for (i in 0..4) {
        item {
            FormatItem(selected = selected == i) { onClick(i) }
        }
    }
    item(span = { GridItemSpan(maxLineSpan) }) {
        FormatSubtitle(
            text = stringResource(R.string.video),
            color = MaterialTheme.colorScheme.secondary
        )
    }
    for (i in 0..5) {
        item {
            FormatItem(
                selected = selected == i,
                outlineColor = MaterialTheme.colorScheme.secondary,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) { onClick(i) }
        }
    }
    item(span = { GridItemSpan(maxLineSpan) }) {
        FormatSubtitle(
            text = stringResource(R.string.audio),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
    for (i in 0..5) {
        item {
            FormatItem(
                selected = selected == i,
                outlineColor = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) { onClick(i) }
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
            .padding(top = 12.dp, bottom = 4.dp)
            .padding(horizontal = 12.dp),
        color = color,
        style = MaterialTheme.typography.titleSmall
    )
}


fun String.toEmpty() = if (equals("none") || equals("null")) "" else this