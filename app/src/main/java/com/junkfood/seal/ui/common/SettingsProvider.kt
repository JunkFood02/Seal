package com.junkfood.seal.ui.common

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.ui.theme.ColorScheme.DEFAULT_SEED_COLOR
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.PreferenceUtil.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val LocalDarkTheme = compositionLocalOf { PreferenceUtil.DarkThemePreference() }
val LocalDynamicColor = compositionLocalOf { true }
val LocalVideoThumbnailLoader = staticCompositionLocalOf {
    ImageLoader.Builder(context).build()
}
val LocalSeedColor = compositionLocalOf { DEFAULT_SEED_COLOR }

val settingFlow: Flow<PreferenceUtil.AppSettings> =
    context.dataStore.data.map {
        PreferenceUtil.AppSettings(
            PreferenceUtil.DarkThemePreference(it[PreferenceUtil.DARK_THEME_KEY] ?: FOLLOW_SYSTEM),
            it[PreferenceUtil.DYNAMIC_COLOR_KEY] ?: true,
            it[PreferenceUtil.THEME_COLOR_KEY] ?: DEFAULT_SEED_COLOR
        )
    }

@Composable
fun SettingsProvider(content: @Composable () -> Unit) {
    val appSettingsState = settingFlow.collectAsState(PreferenceUtil.initialAppSettings()).value
    CompositionLocalProvider(
        LocalDarkTheme provides appSettingsState.darkTheme,
        LocalDynamicColor provides (appSettingsState.dynamicColor),
        LocalVideoThumbnailLoader provides ImageLoader.Builder(LocalContext.current)
            .components { add(VideoFrameDecoder.Factory()) }.build(),
        LocalSeedColor provides appSettingsState.seedColor,
        content = content
    )
}