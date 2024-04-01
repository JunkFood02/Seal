package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.video_title_sample_text),
    author: String = stringResource(R.string.video_creator_sample_text),
    thumbnailUrl: Any = "",
    showCancelButton: Boolean = false,
    onCancel: () -> Unit = {},
    onClick: () -> Unit = {},
    progress: Float = 90f,
    fileSizeApprox: Double = 1024 * 1024 * 69.0,
    duration: Int = 359,
    isPreview: Boolean = false
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick, shape = MaterialTheme.shapes.small
    ) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                Crossfade(targetState = thumbnailUrl, label = "") {
                    AsyncImageImpl(
                        modifier = Modifier
                            .padding()
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
                            .clip(MaterialTheme.shapes.small),
                        model = it,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        isPreview = isPreview
                    )
                }

                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.BottomEnd),
                    color = Color.Black.copy(alpha = 0.68f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    val fileSizeText = fileSizeApprox.toFileSizeText()
                    val durationText = duration.toDurationText()
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "$fileSizeText · $durationText",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    AnimatedVisibility(
                        visible = showCancelButton,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FilledTonalIconButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .size(56.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                    alpha = 0.68f
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = stringResource(id = R.string.cancel),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val progressAnimationValue by animateFloatAsState(
                targetValue = progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
            )
            if (progress < 0f)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            else
                LinearProgressIndicator(
                    progress = { progressAnimationValue / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    drawStopIndicator = null
                )
        }
    }
}

@Composable
@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun VideoCardPreview() {
    SealTheme() {
        VideoCard(isPreview = true)
    }
}
