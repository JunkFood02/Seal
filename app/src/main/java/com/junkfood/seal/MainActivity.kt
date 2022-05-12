package com.junkfood.seal

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.ui.viewmodel.DownloadViewModel
import com.junkfood.seal.ui.viewmodel.VideoListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val videoListViewModel: VideoListViewModel by viewModels()
    private val downloadViewModel: DownloadViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setImmersiveStatusBar()
        setContent {
            SealTheme {
                HomeEntry()
            }
        }

    }

    /*override fun attachBaseContext(newBase: Context?) {

        super.attachBaseContext(
            newBase?.applyNewLocale(
                Locale(
                    PreferenceManager.getDefaultSharedPreferences(context)
                        .getString("language", "en")!!
                )
            )
        )
    }

    private fun Context.applyNewLocale(locale: Locale): Context {
        val config = this.resources.configuration
        val sysLocale =
            config.locales.get(0)
        if (sysLocale.language != locale.language) {
            Locale.setDefault(locale)
            config.setLocale(locale)
            resources.configuration.updateFrom(config)
        }
        return this
    }*/

    private fun setImmersiveStatusBar() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View, windowInsets: WindowInsetsCompat ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, insets.top, 0, if (insets.bottom > 50) insets.bottom else 0)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}





