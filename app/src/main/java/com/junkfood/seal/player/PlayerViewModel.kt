package com.junkfood.seal.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.junkfood.seal.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Player ViewModel for managing playback state in Compose UI
 *
 * State management for ExoPlayer integration with Jetpack Compose.
 * Provides reactive state updates and playback controls.
 */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    // Player instance (singleton per app)
    private var _player: ExoPlayer? = null
    val player: ExoPlayer?
        get() = _player

    // Playback state
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // Current media info
    private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
    val currentMedia: StateFlow<MediaInfo?> = _currentMedia.asStateFlow()

    // Playback position (for progress bar)
    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    // Playback duration
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // Buffer percentage
    private val _bufferedPercentage = MutableStateFlow(0)
    val bufferedPercentage: StateFlow<Int> = _bufferedPercentage.asStateFlow()

    // Volume (0.0 - 1.0)
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Playback speed (0.5x - 2.0x)
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    init {
        initializePlayer()
        observePlayerState()
    }

    /**
     * Initialize ExoPlayer instance
     */
    private fun initializePlayer() {
        if (_player != null) return

        val context = getApplication<Application>().applicationContext
        _player = ExoPlayer.Builder(context)
            .setUseLazyPreparation(true)
            .build()
            .apply {
                // Set initial volume
                volume = 1.0f
                // Set playback parameters
                playbackParameters = androidx.media3.common.PlaybackParameters(1.0f)
            }

        _playbackState.value = PlaybackState.READY
    }

    /**
     * Observe player state and update flows
     */
    private fun observePlayerState() {
        viewModelScope.launch {
            _player?.let { player ->
                // Position updates
                player.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playbackState.value = if (isPlaying) {
                            PlaybackState.PLAYING
                        } else {
                            when (player.playbackState) {
                                Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                                Player.STATE_ENDED -> PlaybackState.ENDED
                                else -> PlaybackState.PAUSED
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_IDLE -> _playbackState.value = PlaybackState.IDLE
                            Player.STATE_BUFFERING -> _playbackState.value = PlaybackState.BUFFERING
                            Player.STATE_READY -> {
                                _duration.value = player.duration.coerceAtLeast(0L)
                                _playbackState.value = if (player.playWhenReady) {
                                    PlaybackState.PLAYING
                                } else {
                                    PlaybackState.PAUSED
                                }
                            }
                            Player.STATE_ENDED -> {
                                _playbackState.value = PlaybackState.ENDED
                                // Auto-play next if available
                                playNext()
                            }
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.let {
                            _currentMedia.value = MediaInfo.fromMediaItem(it)
                        }
                    }

                    override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                        _playbackSpeed.value = playbackParameters.speed
                    }
                })
            }
        }

        // Periodically update position
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000) // Update every second
                _player?.currentPosition?.let { pos ->
                    _position.value = pos.coerceAtLeast(0L)
                }
            }
        }
    }

    /**
     * Play a single media file
     */
    fun playMedia(uri: String, title: String? = null, artist: String? = null) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(title ?: "Unknown")
                        .setArtist(artist ?: "Unknown")
                        .build()
                )
                .build()

            _player?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        }
    }

    /**
     * Queue media for playback
     */
    fun queueMedia(items: List<MediaItem>) {
        viewModelScope.launch {
            _player?.apply {
                addMediaItems(items)
                prepare()
            }
        }
    }

    /**
     * Clear playback queue
     */
    fun clearQueue() {
        _player?.apply {
            stop()
            clearMediaItems()
            _playbackState.value = PlaybackState.IDLE
        }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        _player?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = PlaybackState.PAUSED
            } else {
                player.play()
                _playbackState.value = PlaybackState.PLAYING
            }
        }
    }

    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        _player?.seekTo(positionMs)
    }

    /**
     * Skip forward/backward
     */
    fun skip(direction: SkipDirection) {
        val delta = when (direction) {
            SkipDirection.FORWARD -> 10000L  // 10 seconds
            SkipDirection.BACKWARD -> -10000L
        }
        _player?.currentPosition?.let { current ->
            seekTo((current + delta).coerceAtLeast(0L))
        }
    }

    /**
     * Set volume
     */
    fun setVolume(volume: Float) {
        _player?.volume = volume.coerceIn(0.0f, 1.0f)
        _volume.value = volume
    }

    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        _player?.playbackParameters = androidx.media3.common.PlaybackParameters(speed)
        _playbackSpeed.value = speed
    }

    /**
     * Play next item
     */
    fun playNext() {
        val nextIndex = _player?.nextWindowIndex ?: -1
        if (nextIndex != -1) {
            _player?.seekToNext()
        }
    }

    /**
     * Play previous item
     */
    fun playPrevious() {
        val prevIndex = _player?.previousWindowIndex ?: -1
        if (prevIndex != -1) {
            _player?.seekToPrevious()
        } else {
            // Restart current track
            seekTo(0)
        }
    }

    /**
     * Release player resources
     */
    override fun onCleared() {
        super.onCleared()
        _player?.release()
        _player = null
    }
}

/**
 * Playback state enumeration
 */
enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}

/**
 * Skip direction
 */
enum class SkipDirection {
    FORWARD,
    BACKWARD
}

/**
 * Media information data class
 */
data class MediaInfo(
    val title: String,
    val artist: String?,
    val duration: Long,
    val uri: String
) {
    companion object {
        fun fromMediaItem(mediaItem: MediaItem): MediaInfo {
            return MediaInfo(
                title = mediaItem.mediaMetadata.title ?: "Unknown",
                artist = mediaItem.mediaMetadata.artist,
                duration = mediaItem.mediaMetadata.durationMs ?: 0L,
                uri = mediaItem.mediaId ?: ""
            )
        }
    }
}
