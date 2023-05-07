package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
    onClick: () -> Unit = {},
    progress: Float = 100f,
    fileSizeApprox: Double = 1024 * 1024 * 69.0,
    duration: Int = 359,
    isPreview: Boolean = false,
    isLoading: Boolean = true
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onClick() }, shape = MaterialTheme.shapes.small
    ) {
        val loadingContent: @Composable () -> Unit = {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .shimmerEffect()
                        .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.BottomEnd)
                            .height(12.dp)
                            .width(42.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .shimmerEffect()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .shimmerEffect()
                )
            }
        }

        val loadedContent: @Composable () -> Unit = {
            Column {
                Box(Modifier.fillMaxWidth()) {
                    AsyncImageImpl(
                        modifier = Modifier
                            .padding()
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
                            .clip(MaterialTheme.shapes.small),
                        model = thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        isPreview = isPreview
                    )
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
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "Download Progress Bar"
                )
                if (progress < 0f)
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                else
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = progressAnimationValue / 100f,
                    )
            }
        }
        Crossfade(
            targetState = isLoading,
            label = "Crossfade between download states",
            animationSpec = tween(
                durationMillis = 150
            )
        ) { isLoading ->
            if (isLoading)
                loadingContent()
            else
                loadedContent()
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

@Composable
@Preview
@Preview(name = "Dark Mode Loaded", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun VideoCardPreviewLoaded() {
    SealTheme() {
        VideoCard(isPreview = true, isLoading = false)
    }
}
