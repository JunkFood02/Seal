package com.junkfood.seal.player.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import com.junkfood.seal.App
import com.junkfood.seal.player.PlayerViewModel
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.theme.SealTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Standalone Player Activity
 *
 * Launched when user wants to play a downloaded video or audio file.
 * Can be embedded as a fragment/composable in the main app.
 */
class PlayerActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SettingsProvider(calculateWindowSizeClass(this).widthSizeClass) {
                SealTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                ) {
                    // Get media from intent
                    val uri = intent?.getStringExtra("media_uri")
                    val title = intent?.getStringExtra("media_title")
                    val artist = intent?.getStringExtra("media_artist")

                    MediaPlayerScreen(
                        viewModel = playerViewModel,
                        onNavigateBack = { finish() },
                        initialMediaUri = uri,
                        initialTitle = title,
                        initialArtist = artist
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Launch player with a specific file
         */
        fun launch(
            activity: Activity,
            filePath: String,
            title: String? = null,
            artist: String? = null
        ) {
            val intent = Intent(activity, PlayerActivity::class.java).apply {
                putExtra("media_uri", filePath)
                putExtra("media_title", title)
                putExtra("media_artist", artist)
            }
            activity.startActivity(intent)
        }

        /**
         * Launch player directly from a file
         */
        fun launch(context: android.content.Context, file: java.io.File) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra("media_uri", file.absolutePath)
                putExtra("media_title", file.nameWithoutExtension)
            }
            context.startActivity(intent)
        }
    }
}
