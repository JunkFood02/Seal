package com.junkfood.seal.ui.common

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.ui.theme.ColorScheme.DEFAULT_SEED_COLOR
import com.junkfood.seal.util.PreferenceUtil

val LocalDarkTheme = compositionLocalOf { PreferenceUtil.DarkThemePreference() }
val LocalVideoThumbnailLoader = staticCompositionLocalOf {
    ImageLoader.Builder(context).build()
}
val LocalSeedColor = compositionLocalOf { DEFAULT_SEED_COLOR }
val LocalWindowWidthState = staticCompositionLocalOf { WindowWidthSizeClass.Compact }
val settingFlow = PreferenceUtil.AppSettingsStateFlow
val LocalDynamicColorSwitch = compositionLocalOf { PreferenceUtil.DynamicPreference() }

@Composable
fun SettingsProvider(windowWidthSizeClass: WindowWidthSizeClass, content: @Composable () -> Unit) {
    val appSettingsState = settingFlow.collectAsState().value
    CompositionLocalProvider(
        LocalDarkTheme provides appSettingsState.darkTheme,
        LocalVideoThumbnailLoader provides ImageLoader.Builder(LocalContext.current)
            .build(),
        LocalSeedColor provides appSettingsState.seedColor,
        LocalWindowWidthState provides windowWidthSizeClass,
        LocalDynamicColorSwitch provides appSettingsState.dynamicPreference,
        content = content
    )
}