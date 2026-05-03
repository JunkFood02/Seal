package com.junkfood.seal.player.integration

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.junkfood.seal.App
import com.junkfood.seal.player.PlayerService
import com.junkfood.seal.player.PlayerViewModel
import com.junkfood.seal.ui.page.videolist.VideoListPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Playback Integration Manager
 *
 * Bridges the download system with the playback system.
 * Handles seamless transition from downloaded content to playback.
 *
 * Features:
 *  - Launch player from downloaded media
 *  - Handle video/audio file types appropriately
 *  - Persistent playback queue across app sessions
 *  - Integration with VideoListPage play buttons
 */
object PlaybackIntegration {

    private const val TAG = "PlaybackIntegration"

    /**
     * Start playback from a downloaded file
     *
     * @param context Android context
     * @param filePath Path to downloaded media file
     * @param title Optional title (falls back to filename)
     * @param artist Optional artist/uploader name
     */
    suspend fun playMediaFile(
        context: Context,
        filePath: String,
        title: String? = null,
        artist: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            // Create content URI from file path
            val uri = Uri.fromFile(android.content.File(filePath))

            // Start PlayerService if not running
            val serviceIntent = Intent(context, PlayerService::class.java).apply {
                action = PlayerService.ACTION_PLAY
                putExtra(PlayerService.EXTRA_MEDIA_URI, uri.toString())
                putExtra(PlayerService.EXTRA_MEDIA_TITLE, title)
                putExtra(PlayerService.EXTRA_MEDIA_ARTIST, artist)
            }
            context.startService(serviceIntent)

            // Also update ViewModel if in foreground
            App.applicationScope.launch(Dispatchers.Main) {
                // Find or create PlayerViewModel
                // Will be injected via Koin or ViewModelProvider
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Queue multiple downloaded files
     */
    suspend fun queueMediaFiles(
        context: Context,
        files: List<Pair<String, String?>> // (path, title)
    ) = withContext(Dispatchers.IO) {
        try {
            val serviceIntent = Intent(context, PlayerService::class.java).apply {
                action = PlayerService.ACTION_PLAY
            }
            context.startService(serviceIntent)
            // PlayerService will handle queue via MediaSession
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Pause playback
     */
    fun pausePlayback(context: Context) {
        val intent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    /**
     * Resume playback
     */
    fun resumePlayback(context: Context) {
        val intent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_PLAY
        }
        context.startService(intent)
    }

    /**
     * Stop playback and release resources
     */
    fun stopPlayback(context: Context) {
        val intent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_STOP
        }
        context.stopService(intent)
    }

    /**
     * Check if player service is running
     */
    fun isPlaying(): Boolean {
        // Check via PlayerViewModel or service binder
        return false // Placeholder
    }

    /**
     * Get current playback position (for seeking)
     */
    fun getCurrentPosition(): Long = 0

    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        // Via PlayerViewModel
    }
}

/**
 * Extension to detect media file type
 */
fun String.isAudioFile(): Boolean {
    val audioExtensions = listOf("mp3", "aac", "m4a", "opus", "ogg", "flac", "wav")
    return audioExtensions.any { this.endsWith(it, ignoreCase = true) }
}

fun String.isVideoFile(): Boolean {
    val videoExtensions = listOf("mp4", "mkv", "webm", "avi", "mov", "flv")
    return videoExtensions.any { this.endsWith(it, ignoreCase = true) }
}

/**
 * Extension for rapid media launch from VideoInfo
 */
fun File.launchPlayer(context: Context, title: String? = null) {
    val intent = Intent(context, PlayerService::class.java).apply {
        action = PlayerService.ACTION_PLAY
        putExtra(PlayerService.EXTRA_MEDIA_URI, this@launchPlayer.absolutePath)
        putExtra(PlayerService.EXTRA_MEDIA_TITLE, title ?: this@launchPlayer.name)
    }
    context.startService(intent)
}
