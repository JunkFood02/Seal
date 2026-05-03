package com.junkfood.seal.player.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SkipNext
import androidx.compose.material.icons.automirrored.filled.SkipPrevious
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.junkfood.seal.player.PlayerViewModel
import com.junkfood.seal.player.PlaybackState
import kotlinx.coroutines.flow.collectLatest

/**
 * Main Media Player Screen
 *
 * Full-screen or embedded video/audio player with:
 *  - Video surface using Media3 PlayerSurface
 *  - Custom playback controls (play/pause, seek, skip)
 *  - Progress slider with buffering indicator
 *  - Volume and speed controls
 *  - Queue management
 *  - Full-screen toggle
 *  - Picture-in-picture support (future)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayerScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialMediaUri: String? = null,
    initialTitle: String? = null,
    initialArtist: String? = null
) {
    val context = LocalContext.current
    val player = viewModel.player
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()

    var isFullScreen by remember { mutableFloatStateOf(0f) }

    // Load initial media if provided
    LaunchedEffect(initialMediaUri) {
        initialMediaUri?.let { uri ->
            viewModel.playMedia(uri, initialTitle, initialArtist)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Surface (if video content)
        player?.let { exoPlayer ->
            VideoPlayerSurface(
                player = exoPlayer,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(if (isFullScreen > 0.5f) 16f / 9f else 1f)
            )
        }

        // Top app bar (for close/fullscreen)
        AnimatedVisibility(
            visible = isFullScreen > 0.5f,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            TopAppBar(
                title = { Text(currentMedia?.title ?: "Player") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: PIP */  }) {
                        Icon(Icons.Default.Fullscreen, "Fullscreen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            )
        }

        // Bottom controls overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Spacer for top bar
            if (isFullScreen > 0.5f) Spacer(Modifier.height(56.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Track info
                currentMedia?.let { media ->
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    media.artist?.let { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Playback controls
            PlaybackControls(
                playbackState = playbackState,
                position = position,
                duration = duration,
                volume = volume,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSkipNext = { viewModel.playNext() },
                onSkipPrev = { viewModel.playPrevious() },
                onVolumeChange = { viewModel.setVolume(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }

    // Handle lifecycle
    DisposableEffect(Unit) {
        onDispose {
            // Pause playback when leaving screen (optional)
            // viewModel.pause()
        }
    }
}

/**
 * Video player surface using Media3's PlayerSurface
 */
@Composable
private fun VideoPlayerSurface(
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {
    // For now we use AndroidView - PlayerSurface is still experimental
    // Future: switch to androidx.media3.ui.compose.PlayerSurface when stable
    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = false  // Custom controls
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        update = { view ->
            view.player = player
        }
    )
}

/**
 * Playback controls row
 */
@Composable
private fun PlaybackControls(
    playbackState: PlaybackState,
    position: Long,
    duration: Long,
    volume: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Progress bar
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Slider(
                value = position.toFloat(),
                valueRange = 0f..duration.coerceAtLeast(0L).toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(position),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Previous
            IconButton(onClick = onSkipPrev) {
                Icon(
                    Icons.AutoMirrored.Filled.SkipPrevious,
                    "Previous",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            // Play/Pause
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = when (playbackState) {
                        PlaybackState.PLAYING -> Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = if (playbackState == PlaybackState.PLAYING) "Pause" else "Play",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            // Next
            IconButton(onClick = onSkipNext) {
                Icon(
                    Icons.AutoMirrored.Filled.SkipNext,
                    "Next",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        }

        // Volume control (optional)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    volume == 0f -> Icons.Default.VolumeOff
                                            volume < 0.5f -> Icons.Default.VolumeDown
                                            else -> Icons.Default.VolumeUp
                                        },
                contentDescription = "Volume",
                modifier = Modifier.size(24.dp),
                tint = Color.White.copy(alpha = 0.7f)
                                    )

            Slider(
                value = volume,
                valueRange = 0f..1f,
                onValueChange = { onSeekVolume(it) },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

/**
 * Format time in MM:SS or HH:MM:SS
 */
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
