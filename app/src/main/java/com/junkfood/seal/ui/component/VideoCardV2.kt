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
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.rounded.Download
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
import com.junkfood.seal.ui.theme.FixedAccentColors
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText

@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    viewState: Task.ViewState,
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
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Column(
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = viewState.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = viewState.uploader,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onButtonClick, modifier = Modifier.align(Alignment.Top)) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.show_more_actions),
                    )
                }
            }
        }
    }
}

private val IconButtonSize = 64.dp
private val IconSize = 36.dp
private val ContainerColor: Color
    @Composable get() = FixedAccentColors.onSecondaryFixed.copy(alpha = 0.6f)
private val ContentColor: Color
    @Composable get() = FixedAccentColors.secondaryFixed

@Composable
@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun VideoCardV2Preview() {
    SealTheme() {
        VideoCardV2(
            viewState = Task.ViewState(),
            stateIndicator = {
                PlayVideoButton(modifier = Modifier.align(Alignment.Center)) {}
            },
        ) {}
    }
}

@Composable
fun StateIndicator(modifier: Modifier = Modifier, state: Task.State, onClick: () -> Unit) {
    when (state) {
        is Task.State.Error -> {
            RestartButton(modifier, onClick)
        }
        is Task.State.Canceled -> {
            ResumeButton(modifier, state.progress, onClick)
        }
        is Task.State.Completed -> {
            PlayVideoButton(modifier, onClick)
        }
        is Task.State.FetchingInfo,
        Task.State.ReadyWithInfo,
        Task.State.Idle -> {
            ProgressButton(modifier = modifier, progress = -1f, onClick = onClick)
        }
        is Task.State.Running -> {
            ProgressButton(modifier = modifier, progress = state.progress, onClick = onClick)
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
            )
        } else {
            CircularProgressIndicator(
                { animatedProgress },
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ContentColor,
                gapSize = 0.dp,
            )
        }
        Icon(
            imageVector = Icons.Outlined.Pause,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(IconSize),
            tint = ContentColor,
        )
    }
}
