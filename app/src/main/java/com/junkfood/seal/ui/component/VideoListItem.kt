package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalVideoThumbnailLoader
import com.junkfood.seal.ui.page.videolist.AUDIO_REGEX


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaListItem(
    modifier: Modifier = Modifier,
    title: String = "",
    author: String = "",
    thumbnailUrl: String = "",
    videoPath: String = "",
    videoUrl: String = "",
    isSelectEnabled: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val isAudio = videoPath.contains(Regex(AUDIO_REGEX))
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val imageModel = ImageRequest.Builder(context)
        .data(thumbnailUrl)
        .crossfade(true)
        .build()
    Box(
        modifier = modifier
            .combinedClickable(
                enabled = true,
                onClick = { if (!isSelectEnabled) onClick() else onSelect() },
                onLongClick = {
                    if (!isSelectEnabled) {
                        onLongClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                })
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
            MediaImage(imageModel = imageModel, isAudio = isAudio)
            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
                    .size(18.dp),
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
fun MediaImage(imageModel: Any, isAudio: Boolean = false) {
    AsyncImage(
        modifier = Modifier
            .fillMaxWidth(if (!isAudio) 0.45f else 0.25f)
            .aspectRatio(if (!isAudio) 16f / 9f else 1f, matchHeightConstraintsFirst = false)
            .clip(MaterialTheme.shapes.extraSmall),
        model = imageModel,
        contentDescription = stringResource(R.string.thumbnail),
        contentScale = ContentScale.Crop,
        imageLoader = LocalVideoThumbnailLoader.current
    )
}
