package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState

private const val AUDIO_REGEX = "(\\.mp3)|(\\.aac)|(\\.opus)|(\\.m4a)"


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun MediaListItem(
    modifier: Modifier = Modifier,
    title: String = "",
    author: String = "",
    thumbnailUrl: String = "",
    videoPath: String = "",
    videoUrl: String = "",
    videoFileSize: Float = 0f,
    isFileAvailable: Boolean = true,
    isSelectEnabled: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val isAudio = videoPath.contains(Regex(AUDIO_REGEX))
    val imageWeight = when (LocalWindowWidthState.current) {
        WindowWidthSizeClass.Expanded -> {
            if (isAudio) 0.30f else 0.55f
        }
        WindowWidthSizeClass.Medium -> {
            if (isAudio) 0.20f else 0.30f
        }
        else -> {
            if (isAudio) 0.25f else 0.45f
        }
    }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val imageModel = ImageRequest.Builder(context)
        .data(thumbnailUrl)
        .crossfade(true)
        .build()
    Box(
        modifier = with(modifier) {
            if (!isSelectEnabled)
                combinedClickable(
                    enabled = true,
                    onClick = { onClick() },
                    onClickLabel = stringResource(R.string.open_file),
                    onLongClick = {
                        onLongClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onLongClickLabel = stringResource(R.string.show_more_actions)
                )
            else
                selectable(selected = isSelected, onClick = onSelect)
        }
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterVertically),
                visible = isSelectEnabled,
            ) {
                Checkbox(
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp),
                    checked = isSelected,
                    onCheckedChange = null
                )
            }
            MediaImage(
                modifier = Modifier.weight(imageWeight),
                imageModel = imageModel,
                isAudio = isAudio
            )
            Column(
                modifier = Modifier
                    .weight(1f - imageWeight)
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (author != "playlist" && author != "null")
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = if (isFileAvailable) "%.2f M".format(videoFileSize) else stringResource(
                        R.string.unavailable
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = with(MaterialTheme.colorScheme) { if (isFileAvailable) onSurfaceVariant else error },
                    maxLines = 1,
                )


            }
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomEnd), visible = !isSelectEnabled,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(100))
        ) {
            IconButton(
                modifier = Modifier
                    .padding(12.dp)
                    .size(18.dp)
                    .clearAndSetSemantics { },
                onClick = onLongClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(id = R.string.show_more_actions)
                )
            }
        }
    }
}

@Composable
fun MediaImage(modifier: Modifier = Modifier, imageModel: Any, isAudio: Boolean = false) {
    AsyncImage(
        modifier = modifier
            .aspectRatio(if (!isAudio) 16f / 9f else 1f, matchHeightConstraintsFirst = true)
            .clip(MaterialTheme.shapes.extraSmall),
        model = imageModel,
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}
