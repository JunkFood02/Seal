package com.junkfood.seal.player

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSessionService.CommandButton
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Media Playback Service
 *
 * Foreground service that handles audio/video playback using ExoPlayer.
 * Integrates with Media3 Session for external controls (notifications, Android Auto, etc.)
 *
 * Features:
 *  - Background playback
 *  - Media session integration
 *  - Notification controls
 *  - Persistent queue management
 *  - Seamless transition from download to playback
 */
class PlayerService : MediaSessionService() {

    companion object {
        private const val TAG = "PlayerService"
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREV = "action_prev"
        const val ACTION_STOP = "action_stop"
        const val EXTRA_MEDIA_URI = "extra_media_uri"
        const val EXTRA_MEDIA_TITLE = "extra_media_title"
        const val EXTRA_MEDIA_ARTIST = "extra_media_artist"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSession
    private var player: ExoPlayer? = null

    // Playback queue
    private val playbackQueue = mutableListOf<MediaItem>()
    private var currentIndex = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PlayerService created")

        // Initialize ExoPlayer
        initializePlayer()

        // Setup MediaSession
        setupMediaSession()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setUseLazyPreparation(true)
            .build()
            .apply {
                // Configure for low-latency playback
                setAudioAttributes(
                    androidx.media3.common.AudioAttributes.DEFAULT,
                    true
                )
                // Handle playback state
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                if (playWhenReady) {
                                    // Start foreground for active playback
                                    startForeground(
                                        NotificationId.PLAYBACK_NOTIFICATION,
                                        buildNotification()
                                    )
                                }
                            }
                            Player.STATE_ENDED -> {
                                // Move to next track or stop
                                playNext()
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        // Update notification
                        updateNotification()
                    }
                })
            }
    }

    private fun setupMediaSession() {
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(
                // PendingIntent for launching app from notification
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    /**
     * Handle command from MediaController
     */
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        extras: android.os.Bundle
    ): SessionResult {
        return when (customCommand.commandCode) {
            // Add custom command handlers if needed
            else -> SessionResult(SessionResult.RESULT_ERROR_UNKNOWN)
        }
    }

    /**
     * Play a media file from URI
     */
    fun playMedia(uri: String, title: String? = null, artist: String? = null) {
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .apply {
                        title?.let { setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(artist ?: "Unknown")
                                .build()
                        ) }
                    }
                    .build()

                playbackQueue.add(mediaItem)
                currentIndex = playbackQueue.size - 1

                player?.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }
            }
        }
    }

    /**
     * Queue multiple items for playback
     */
    fun queueMedia(items: List<Pair<String, String?>>) {
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                items.forEach { (uri, title) ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(title ?: "Unknown")
                                .build()
                        )
                        .build()
                    playbackQueue.add(mediaItem)
                }

                // If not currently playing, start first item
                if (player?.playbackState != Player.STATE_READY) {
                    currentIndex = 0
                    player?.setMediaItem(playbackQueue[0])
                    player?.prepare()
                }
            }
        }
    }

    fun playPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun playNext() {
        if (currentIndex < playbackQueue.size - 1) {
            currentIndex++
            player?.apply {
                setMediaItem(playbackQueue[currentIndex])
                prepare()
                play()
            }
        } else {
            // End of queue - stop service
            stopSelf()
        }
    }

    fun playPrevious() {
        if (player?.currentPosition ?: 0 > 3000) {
            // If > 3 seconds into track, restart it
            player?.seekTo(0)
        } else if (currentIndex > 0) {
            currentIndex--
            player?.apply {
                setMediaItem(playbackQueue[currentIndex])
                prepare()
                play()
            }
        }
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> playMedia(
                intent.getStringExtra(EXTRA_MEDIA_URI) ?: return,
                intent.getStringExtra(EXTRA_MEDIA_TITLE),
                intent.getStringExtra(EXTRA_MEDIA_ARTIST)
            )
            ACTION_PAUSE -> playPause()
            ACTION_NEXT -> playNext()
            ACTION_PREV -> playPrevious()
            ACTION_STOP -> stopSelf()
        }
    }

    private fun buildNotification(): android.app.Notification {
        // Build media-style notification for playback controls
        return androidx.media3.session.MediaSessionService.buildMediaSessionNotification(
            this,
            mediaSession.sessionToken,
            R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground,
            R.drawable.ic_pause,
            R.drawable.ic_play
        )
    }

    private fun updateNotification() {
        // Update notification with current state
        // Media3 handles this automatically via MediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PlayerService destroyed")

        player?.release()
        player = null

        mediaSession.run {
            release()
        }

        serviceJob.cancel()
    }

    override fun onBind(intent: Intent): IBinder {
        return PlayerBinder()
    }

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
        fun getPlayer(): ExoPlayer? = player
        fun getMediaSession(): MediaSession = mediaSession
        fun getQueue(): List<MediaItem> = playbackQueue.toList()
        fun getCurrentIndex(): Int = currentIndex
    }
}

object NotificationId {
    const val PLAYBACK_NOTIFICATION = 1001
}
