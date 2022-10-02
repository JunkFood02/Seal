package com.junkfood.seal.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.PRIVATE_MODE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(
    modifier: Modifier = Modifier,
    title: String = "Video title sample text",
    author: String = "Video creator sample text",
    thumbnailUrl: Any = R.drawable.sample,
    onClick: () -> Unit = {},
    progress: Float = 100f,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onClick() }, shape = MaterialTheme.shapes.small
    ) {
        Column {
            AsyncImage(
                modifier = Modifier
                    .padding()
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                model = if (PreferenceUtil.getValue(PRIVATE_MODE)) null else
                    ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null, contentScale = ContentScale.FillWidth
            )
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
                targetValue = progress / 100f,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            if (progress < 0f)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            else
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = progressAnimationValue,
                )
        }
    }
}