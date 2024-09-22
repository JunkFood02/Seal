package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.download.Task
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.theme.FixedAccentColors
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText

@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    viewState: Task.ViewState,
    downloadState: Task.DownloadState,
    stateIndicator: @Composable (BoxScope.() -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
    ) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                if (viewState.thumbnailUrl != null) {
                    AsyncImageImpl(
                        modifier =
                            Modifier.padding()
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                        model = viewState.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier =
                            Modifier.padding()
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {}
                }

                stateIndicator?.invoke(this)

                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
                    val text =
                        when (downloadState) {
                            is Task.DownloadState.Canceled -> R.string.status_canceled
                            is Task.DownloadState.Completed -> R.string.status_downloaded
                            is Task.DownloadState.Error -> R.string.status_error
                            is Task.DownloadState.FetchingInfo ->
                                R.string.status_fetching_video_info
                            Task.DownloadState.Idle -> R.string.status_enqueued
                            Task.DownloadState.ReadyWithInfo -> R.string.status_enqueued
                            is Task.DownloadState.Running -> R.string.status_downloading
                        }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (downloadState is Task.DownloadState.Error) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme.run {
                                        if (isDarkTheme) error else errorContainer
                                    },
                                modifier = Modifier.padding(start = 4.dp).size(12.dp),
                            )
                        }
                        Text(
                            stringResource(id = text),
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }

                Surface(
                    modifier = Modifier.padding(4.dp).align(Alignment.BottomEnd),
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    val fileSizeText = viewState.fileSizeApprox.toFileSizeText()
                    val durationText = viewState.duration.toDurationText()
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "$fileSizeText  $durationText",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(start = 12.dp).padding(vertical = 12.dp).weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = viewState.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (viewState.uploader.isNotEmpty()) {
                        Text(
                            modifier = Modifier.padding(top = 3.dp),
                            text = viewState.uploader,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(
                    onButtonClick,
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.show_more_actions),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private val IconButtonSize = 64.dp
private val IconSize = 36.dp
private val ContainerColor: Color
    @Composable get() = FixedAccentColors.onSecondaryFixed.copy(alpha = 0.68f)
private val ContentColor: Color
    @Composable get() = FixedAccentColors.secondaryFixed

@Composable
@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun VideoCardV2Preview() {
    SealTheme() {
        val downloadState =
            Task.DownloadState.Error(
                throwable = Throwable(),
                action = Task.RestartableAction.Download,
            )
        VideoCardV2(
            viewState = Task.ViewState(),
            downloadState = downloadState,
            stateIndicator = {
                StateIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    downloadState = downloadState,
                ) {}
            },
        ) {}
    }
}

@Composable
fun StateIndicator(
    modifier: Modifier = Modifier,
    downloadState: Task.DownloadState,
    onClick: () -> Unit,
) {
    when (downloadState) {
        is Task.DownloadState.Error -> {
            RestartButton(modifier, onClick)
        }
        is Task.DownloadState.Canceled -> {
            ResumeButton(modifier, downloadState.progress, onClick)
        }
        is Task.DownloadState.Completed -> {
            PlayVideoButton(modifier, onClick)
        }
        is Task.DownloadState.FetchingInfo,
        Task.DownloadState.ReadyWithInfo,
        Task.DownloadState.Idle -> {
            ProgressButton(modifier = modifier, progress = -1f, onClick = onClick)
        }
        is Task.DownloadState.Running -> {
            ProgressButton(
                modifier = modifier,
                progress = downloadState.progress,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ResumeButton(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    onClick: () -> Unit,
) {
    val background = ContainerColor

    Box(
        modifier =
            modifier
                .size(IconButtonSize)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        if (progress != null) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ContentColor,
                trackColor = Color.Transparent,
                gapSize = 0.dp,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Download,
            contentDescription = stringResource(R.string.restart),
            modifier = Modifier.size(IconSize).align(Alignment.Center),
            tint = ContentColor,
        )
    }
}

@Composable
fun RestartButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = ContainerColor

    Box(
        modifier =
            modifier
                .size(IconButtonSize)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        Icon(
            imageVector = Icons.Rounded.RestartAlt,
            contentDescription = stringResource(R.string.restart),
            modifier = Modifier.size(IconSize).align(Alignment.Center),
            tint = ContentColor,
        )
    }
}

@Composable
private fun PlayVideoButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(IconButtonSize),
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = ContainerColor,
                contentColor = ContentColor,
            ),
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = stringResource(R.string.open_file),
            modifier = Modifier.size(IconSize),
        )
    }
}

@Composable
private fun ProgressButton(modifier: Modifier = Modifier, progress: Float, onClick: () -> Unit) {
    val animatedProgress by
        animateFloatAsState(
            progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progress",
        )
    val background = ContainerColor

    Box(
        modifier =
            modifier
                .size(IconButtonSize)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        if (progress < 0) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ContentColor,
                trackColor = Color.Transparent,
            )
        } else {
            CircularProgressIndicator(
                { animatedProgress },
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ContentColor,
                gapSize = 0.dp,
                trackColor = Color.Transparent,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Pause,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(IconSize),
            tint = ContentColor,
        )
    }
}
