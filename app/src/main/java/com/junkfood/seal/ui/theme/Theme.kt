package com.junkfood.seal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.color.MaterialColors
import com.junkfood.seal.ui.theme.ColorScheme.darkColorSchemeFromColor
import com.junkfood.seal.ui.theme.ColorScheme.lightColorSchemeFromColor
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.ON


fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
fun Color.harmonizeWithPrimary(): Color {
    return Color(
        MaterialColors.harmonize(
            this.toArgb(),
            MaterialTheme.colorScheme.primary.toArgb()
        )
    )
}

@Composable
fun SealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Int,
    dynamicColorEnable: Boolean,
    dynamicColor: Int,
    content: @Composable () -> Unit
) {
    rememberSystemUiController().run {
        setStatusBarColor(Color.Transparent, !darkTheme)
        setSystemBarsColor(Color.Transparent, !darkTheme)
        setNavigationBarColor(Color.Transparent, !darkTheme)
    }
    val colorScheme: androidx.compose.material3.ColorScheme
    when {
        dynamicColor == ON && dynamicColorEnable -> {
            val context = LocalContext.current
            colorScheme =
                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
        }
        darkTheme -> {
            colorScheme = darkColorSchemeFromColor(seedColor)
        }
        else -> {
            colorScheme = lightColorSchemeFromColor(seedColor)
        }
    }
//    val colorScheme =
//        when {
//            darkTheme -> {
//                if(dynamicColorEnable && dynamicColor == ON) {
//                    darkColorSchemeFromColor(seedColor)
//                }
//                else {
//
//                }
//            }
//            else -> lightColorSchemeFromColor(seedColor)
//        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}