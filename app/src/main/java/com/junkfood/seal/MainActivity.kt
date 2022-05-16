package com.junkfood.seal

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.ui.page.HomeEntry
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.PreferenceUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.update

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val LocalDarkTheme = compositionLocalOf<Boolean> { DarkThemePreference.default }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveStatusBar()
        setContent {
            val appearanceViewModel: AppearanceViewModel = hiltViewModel()
            appearanceViewModel.viewState.update {
                it.copy(
                    darkTheme = isSystemInDarkTheme(),
                    dynamicColor = (Build.VERSION.SDK_INT >= 31) and PreferenceUtil.getValue(
                        PreferenceUtil.DYNAMIC_COLORS, true
                    )
                )
            }
            CompositionLocalProvider(
                LocalDynamicColor
            )
            val state=appearanceViewModel.viewState.collectAsState()
            SealTheme(darkTheme =state.value.darkTheme,state.value.dynamicColor) {
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View, windowInsets: WindowInsetsCompat ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, if (insets.bottom > 50) insets.bottom else 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}





