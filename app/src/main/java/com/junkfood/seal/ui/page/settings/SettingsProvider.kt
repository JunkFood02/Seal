package com.junkfood.seal.ui.page.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.PreferenceUtil.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val LocalDarkTheme = compositionLocalOf { PreferenceUtil.DarkThemePreference() }
val LocalDynamicColor = compositionLocalOf { true }
val settingFlow: Flow<PreferenceUtil.AppSettings> =
    context.dataStore.data.map {
        PreferenceUtil.AppSettings(
            PreferenceUtil.DarkThemePreference(it[PreferenceUtil.DARK_THEME_KEY] ?: FOLLOW_SYSTEM),
            it[PreferenceUtil.DYNAMIC_COLOR_KEY] ?: true
        )
    }

@Composable
fun SettingsProvider(content: @Composable () -> Unit) {

    val appSettingsState = settingFlow.collectAsState(PreferenceUtil.initialAppSettings()).value
    CompositionLocalProvider(
        LocalDarkTheme provides appSettingsState.darkTheme,
        LocalDynamicColor provides (appSettingsState.dynamicColor),
        content = content
    )
}