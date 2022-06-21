package com.junkfood.seal.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalVideoThumbnailLoader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(
    title: String = "",
    author: String = "",
    thumbnailUrl: String = "",
    videoPath: String = "",
    videoUrl: String = "",
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val imageModel = ImageRequest.Builder(context)
        .data(if (author == "playlist") videoPath else thumbnailUrl)
        .networkCachePolicy(CachePolicy.DISABLED)
        .videoFrameMillis(10000)
        .crossfade(true)
        .build()
    Box(
        modifier = Modifier
            .combinedClickable(
                enabled = true,
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                })
            .padding(12.dp)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                model = imageModel,
                contentDescription = stringResource(R.string.thumbnail),
                contentScale = ContentScale.Crop,
                imageLoader = LocalVideoThumbnailLoader.current
            )
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
            }
        }
        IconButton(
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.BottomEnd),
            onClick = onLongClick
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(id = R.string.show_more_actions)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioListItem(
    title: String = "",
    author: String = "",
    videoPath: String = "",
    thumbnailUrl: String = "",
    videoUrl: String = "",
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .combinedClickable(enabled = true, onClick = onClick, onLongClick = {
                onLongClick()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            })
            .padding(12.dp)
            .fillMaxWidth(),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .networkCachePolicy(CachePolicy.DISABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.thumbnail),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(start = 12.dp, end = 20.dp)
                    .fillMaxWidth()
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
            }
        }
        IconButton(
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.BottomEnd),
            onClick = onLongClick
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(id = R.string.show_more_actions)
            )
        }
    }
}
