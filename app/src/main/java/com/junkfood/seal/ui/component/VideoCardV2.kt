package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText

@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.video_title_sample_text),
    author: String = stringResource(R.string.video_creator_sample_text),
    viewState: Task.ViewState,
    stateIndicator: @Composable (() -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
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
                        color = MaterialTheme.colorScheme.surfaceDim,
                    ) {}
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
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Column(
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                stateIndicator?.invoke()
                IconButton(onButtonClick) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.show_more_actions),
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
        VideoCardV2(
            viewState = Task.ViewState(),
            stateIndicator = { ProgressButton(progress = 0.2f) {} },
        ) {}
    }
}

@Composable
fun StateIndicator(modifier: Modifier = Modifier, state: Task.State, onClick: () -> Unit) {
    when (state) {
        is Task.State.Canceled -> {
            RestartButton(modifier, onClick)
        }
        is Task.State.Completed -> {
            OpenFileButton(modifier, onClick)
        }
        is Task.State.Error -> {
            ErrorButton(modifier, onClick)
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
private fun RestartButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Outlined.RestartAlt, stringResource(R.string.restart))
    }
}

@Composable
private fun OpenFileButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            Icons.Rounded.CheckCircle,
            stringResource(R.string.open_file),
            tint =
                greenTonalPalettes.accent1(if (LocalDarkTheme.current.isDarkTheme()) 80.0 else 40.0),
        )
    }
}

@Composable
private fun ErrorButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            Icons.Rounded.Error,
            stringResource(R.string.copy_error_report),
            tint = MaterialTheme.colorScheme.error,
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

    Box(
        modifier =
            modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        if (progress < 0) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp).align(Alignment.Center),
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeWidth = 3.dp,
            )
        } else {
            CircularProgressIndicator(
                { animatedProgress },
                modifier = Modifier.size(28.dp).align(Alignment.Center),
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeWidth = 3.dp,
                gapSize = 0.dp
            )
        }
        Icon(
            imageVector = Icons.Rounded.Stop,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
