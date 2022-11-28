package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.theme.PreviewThemeLight

enum class DownloadTaskItemStatus(
    val statusLabelId: Int, val primaryButtonIcon: ImageVector, val primaryOperationDescId: Int
) {
    ENQUEUED(
        R.string.status_enqueued,
        Icons.Rounded.Cancel,
        R.string.cancel
    ),
    COMPLETED(
        R.string.status_completed,
        Icons.Rounded.PlayArrow,
        R.string.open_file
    ),
    DOWNLOADING(
        R.string.status_downloading,
        Icons.Rounded.Stop,
        R.string.cancel
    ),
    CANCELED(
        R.string.status_canceled,
        Icons.Outlined.RestartAlt,
        R.string.restart
    ),
    FETCHING_INFO(
        R.string.status_fetching_video_info,
        Icons.Rounded.Stop,
        R.string.cancel
    ),
    ERROR(R.string.status_error, Icons.Outlined.RestartAlt, R.string.restart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadTaskItem(
    modifier: Modifier = Modifier,
    imageModel: Any = R.drawable.sample,
    title: String = "sample title ".repeat(5),
    author: String = "author sample ".repeat(5),
    status: DownloadTaskItemStatus = DownloadTaskItemStatus.ENQUEUED,
    expanded: Boolean = false,
    progress: Float = -1f,
    progressText: String = "This is a sample of very long progress text. ".repeat(3),
    errorText: String = "This is a sample of very long error text. ".repeat(3)
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        onClick = { isExpanded = !isExpanded },
        shape = MaterialTheme.shapes.small
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                AsyncImageImpl(
                    modifier = Modifier
                        .padding(12.dp)
                        .weight(1f)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .aspectRatio(16f / 10f, matchHeightConstraintsFirst = true),
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                /*                Image(
                                    painter = painterResource(id = R.drawable.sample),
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .aspectRatio(16f / 10f, matchHeightConstraintsFirst = true),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )*/
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .padding(end = 12.dp)
                        .weight(1f)
                        .fillMaxHeight(), verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
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
                    Spacer(modifier = Modifier.weight(1f, true))
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = status.statusLabelId),
                            style = MaterialTheme.typography.labelMedium,
                            color = with(MaterialTheme.colorScheme)
                            { if (status != DownloadTaskItemStatus.ERROR) primary else error }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        val animatedDegree =
                            animateFloatAsState(targetValue = if (isExpanded) 0f else -180f)
                        FilledTonalIconButton(modifier = Modifier
                            .padding()
                            .align(Alignment.Bottom)
                            .size(24.dp),
                            onClick = { isExpanded = !isExpanded }) {
                            Icon(
                                Icons.Outlined.ExpandLess,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.rotate(animatedDegree.value)
                            )
                        }
                    }

                }
            }


        }
        AnimatedVisibility(visible = isExpanded) {
            Divider(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(1.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(horizontal = 12.dp)
                    .padding(top = 4.dp, bottom = 4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(4f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (status != DownloadTaskItemStatus.ERROR) progressText else errorText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = with(MaterialTheme.colorScheme)
                        { if (status != DownloadTaskItemStatus.ERROR) onSurfaceVariant else error },
                    )
                }
                val containerColor = MaterialTheme.colorScheme.secondaryContainer
//                    val contentColor = contentColorFor(backgroundColor = containerColor)
                val contentColor =
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(2f)
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    Row(modifier = Modifier, horizontalArrangement = Arrangement.End) {
                        FilledIconButton(
                            onClick = { }, colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = containerColor, contentColor = contentColor
                            )
                        ) {
                            Icon(
                                status.primaryButtonIcon,
                                stringResource(id = status.primaryOperationDescId),
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { isMenuExpanded = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = containerColor, contentColor = contentColor
                            )
                        ) {
                            Icon(
                                Icons.Outlined.MoreHoriz,
                                stringResource(R.string.show_more_actions)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.copy_link)) },
                            onClick = { },
                            leadingIcon = { Icon(Icons.Outlined.Link, null) })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.copy_error_report)) },
                            onClick = { },
                            leadingIcon = { Icon(Icons.Outlined.BugReport, null) })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.remove)) },
                            onClick = { },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null) })
                    }
                }
            }
        }
        if (progress < 0) LinearProgressIndicator(
            Modifier
                .fillMaxWidth()
                .padding()
        )
        else LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(), progress = progress
        )

    }
}


@Composable
@Preview
fun DownloadTaskItemPreview(modifier: Modifier = Modifier) {
    Column(modifier) {
        PreviewThemeLight {
            val hapticFeedback = LocalHapticFeedback.current
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.download_task_count).format(5),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {}
                        .padding(start = 8.dp)
                        .padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.recently_added),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .size(18.dp),
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
            Column {
                DownloadTaskItem(expanded = false, status = DownloadTaskItemStatus.ENQUEUED)
                DownloadTaskItem(expanded = false, status = DownloadTaskItemStatus.FETCHING_INFO)
                DownloadTaskItem(
                    expanded = false,
                    progress = 1f,
                    status = DownloadTaskItemStatus.COMPLETED
                )
                DownloadTaskItem(
                    expanded = false,
                    progress = 0f,
                    status = DownloadTaskItemStatus.CANCELED
                )
                DownloadTaskItem(
                    expanded = true,
                    progress = 0f,
                    status = DownloadTaskItemStatus.ERROR
                )
            }
        }
    }
}