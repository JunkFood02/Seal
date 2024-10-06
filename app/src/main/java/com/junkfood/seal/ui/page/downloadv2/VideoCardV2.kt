package com.junkfood.seal.ui.page.downloadv2

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
import com.junkfood.seal.ui.common.LocalFixedColorRoles
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText

private val IconButtonSize = 64.dp
private val IconSize = 36.dp
private val ActionButtonContainerColor: Color
    @Composable get() = LocalFixedColorRoles.current.onSecondaryFixed.copy(alpha = 0.68f)
private val ActionButtonContentColor: Color
    @Composable get() = LocalFixedColorRoles.current.secondaryFixed
private val LabelContainerColor: Color = Color.Black.copy(alpha = 0.68f)

@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    viewState: Task.ViewState,
    stateIndicator: @Composable (BoxScope.() -> Unit)? = null,
    actionButton: @Composable (BoxScope.() -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    with(viewState) {
        VideoCardV2(
            modifier = modifier,
            thumbnailModel = thumbnailUrl,
            title = title,
            uploader = uploader,
            duration = duration,
            fileSizeApprox = fileSizeApprox,
            stateIndicator = stateIndicator,
            actionButton = actionButton,
            onButtonClick = onButtonClick,
        )
    }
}

@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    thumbnailModel: Any? = null,
    title: String = "",
    uploader: String = "",
    duration: Int = 0,
    fileSizeApprox: Double = .0,
    stateIndicator: @Composable (BoxScope.() -> Unit)? = null,
    actionButton: @Composable (BoxScope.() -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    val containerColor =
        MaterialTheme.colorScheme.run {
            if (LocalDarkTheme.current.isDarkTheme()) surfaceContainer else surfaceContainerLowest
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                CardImage(modifier = Modifier, thumbnailModel = thumbnailModel)
                Box(Modifier.align(Alignment.TopStart)) { stateIndicator?.invoke(this) }
                Box(Modifier.align(Alignment.Center)) { actionButton?.invoke(this) }
                VideoInfoLabel(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    duration = duration,
                    fileSizeApprox = fileSizeApprox,
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleText(modifier = Modifier.weight(1f), title = title, uploader = uploader)
                IconButton(onButtonClick, modifier = Modifier.align(Alignment.CenterVertically)) {
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
            thumbnailModel = R.drawable.sample3,
            title = stringResource(R.string.video_title_sample_text),
            uploader = stringResource(R.string.video_creator_sample_text),
            actionButton = { ActionButton(modifier = Modifier, downloadState = downloadState) {} },
            stateIndicator = { StateIndicator(modifier = Modifier, downloadState = downloadState) },
        ) {}
    }
}

@Composable
private fun CardImage(modifier: Modifier = Modifier, thumbnailModel: Any? = null) {
    if (thumbnailModel != null) {
        AsyncImageImpl(
            modifier =
                modifier
                    .padding()
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            model = thumbnailModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(
            modifier =
                modifier
                    .padding()
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {}
    }
}

@Composable
private fun TitleText(modifier: Modifier = Modifier, title: String, uploader: String) {
    Column(
        modifier = modifier.padding(start = 12.dp).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 3.dp),
            text = uploader,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VideoInfoLabel(modifier: Modifier = Modifier, duration: Int, fileSizeApprox: Double) {
    Surface(
        modifier = modifier.padding(4.dp),
        color = LabelContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        val fileSizeText = fileSizeApprox.toFileSizeText()
        val durationText = duration.toDurationText()
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = "$fileSizeText  $durationText",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}

@Composable
fun StateIndicator(modifier: Modifier = Modifier, downloadState: Task.DownloadState) {
    Surface(
        modifier = modifier.padding(8.dp),
        color = LabelContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
        val text =
            when (downloadState) {
                is Task.DownloadState.Canceled -> R.string.status_canceled
                is Task.DownloadState.Completed -> R.string.status_downloaded
                is Task.DownloadState.Error -> R.string.status_error
                is Task.DownloadState.FetchingInfo -> R.string.status_fetching_video_info
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
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    downloadState: Task.DownloadState,
    onActionPost: (UiAction) -> Unit,
) =
    when (downloadState) {
        is Task.DownloadState.Error -> {
            RestartButton(modifier = modifier) { onActionPost(UiAction.Resume) }
        }
        is Task.DownloadState.Canceled -> {
            ResumeButton(modifier = modifier, downloadState.progress) {
                onActionPost(UiAction.Resume)
            }
        }
        is Task.DownloadState.Completed -> {
            PlayVideoButton(modifier = modifier) { onActionPost(UiAction.OpenFile(downloadState.filePath)) }
        }
        is Task.DownloadState.FetchingInfo,
        Task.DownloadState.ReadyWithInfo,
        Task.DownloadState.Idle -> {
            ProgressButton(modifier = modifier, progress = -1f) { onActionPost(UiAction.Cancel) }
        }
        is Task.DownloadState.Running -> {
            ProgressButton(modifier = modifier, progress = downloadState.progress) {
                onActionPost(UiAction.Cancel)
            }
        }
    }

@Composable
private fun ResumeButton(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    onClick: () -> Unit,
) {
    val background = ActionButtonContainerColor

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
                color = ActionButtonContentColor,
                trackColor = Color.Transparent,
                gapSize = 0.dp,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Download,
            contentDescription = stringResource(R.string.restart),
            modifier = Modifier.size(IconSize).align(Alignment.Center),
            tint = ActionButtonContentColor,
        )
    }
}

@Composable
fun RestartButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = ActionButtonContainerColor

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
            tint = ActionButtonContentColor,
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
                containerColor = ActionButtonContainerColor,
                contentColor = ActionButtonContentColor,
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
    val background = ActionButtonContainerColor

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
                color = ActionButtonContentColor,
                trackColor = Color.Transparent,
            )
        } else {
            CircularProgressIndicator(
                { animatedProgress },
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ActionButtonContentColor,
                gapSize = 0.dp,
                trackColor = Color.Transparent,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Pause,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(IconSize),
            tint = ActionButtonContentColor,
        )
    }
}
