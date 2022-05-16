package com.junkfood.seal.ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

var LocalDarkTheme = compositionLocalOf { true }
val settingFlow: Flow<Boolean> =
    context.dataStore.data.map { it[PreferenceUtil.DARK_THEME_KEY] ?: true }

@Composable
fun SettingsProvider(content: @Composable () -> Unit) {

    CompositionLocalProvider(
        LocalDarkTheme provides settingFlow.collectAsState(true).value,
        content = content
    )
}