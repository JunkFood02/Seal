package com.junkfood.seal.ui.common

import android.os.Build
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.junkfood.seal.ui.theme.DEFAULT_SEED_COLOR
import com.junkfood.seal.util.DarkThemePreference
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.palettesMap
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.PaletteStyle
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes

val LocalDarkTheme = compositionLocalOf { DarkThemePreference() }
val LocalSeedColor = compositionLocalOf { DEFAULT_SEED_COLOR }
val LocalWindowWidthState = staticCompositionLocalOf { WindowWidthSizeClass.Compact }
val LocalDynamicColorSwitch = compositionLocalOf { false }
val LocalPaletteStyleIndex = compositionLocalOf { 0 }

@Composable
fun SettingsProvider(windowWidthSizeClass: WindowWidthSizeClass, content: @Composable () -> Unit) {
    val appSettingsState = PreferenceUtil.AppSettingsStateFlow.collectAsState().value
    CompositionLocalProvider(
        LocalDarkTheme provides appSettingsState.darkTheme,
        LocalSeedColor provides appSettingsState.seedColor,
        LocalPaletteStyleIndex provides appSettingsState.paletteStyleIndex,
        LocalTonalPalettes provides if (appSettingsState.isDynamicColorEnabled && Build.VERSION.SDK_INT >= 31) dynamicLightColorScheme(
            LocalContext.current
        ).toTonalPalettes()
        else Color(appSettingsState.seedColor).toTonalPalettes(
            palettesMap.getOrElse(appSettingsState.paletteStyleIndex) { PaletteStyle.TonalSpot }
        ),
        LocalWindowWidthState provides windowWidthSizeClass,
        LocalDynamicColorSwitch provides appSettingsState.isDynamicColorEnabled,
        content = content
    )
}