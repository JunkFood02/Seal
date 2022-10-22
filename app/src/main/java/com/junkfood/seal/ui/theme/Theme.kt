package com.junkfood.seal.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.junkfood.seal.ui.theme.ColorScheme.colorSchemeFromColor

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
    isHighContrastModeEnabled: Boolean = false,
    seedColor: Int,
    isDynamicColorEnabled: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        DynamicColors.isDynamicColorAvailable() && isDynamicColorEnabled -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        else -> colorSchemeFromColor(seedColor, darkTheme)
    }.run {
        if (isHighContrastModeEnabled && darkTheme) copy(
            surface = Color.Black,
            background = Color.Black,
        )
        else this
    }
    val view = LocalView.current
    if(!view.isInEditMode){
        val currentWindow = (view.context as? Activity)?.window
        SideEffect {
            (view.context as Activity).window.statusBarColor = android.graphics.Color.TRANSPARENT
            (view.context as Activity).window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(currentWindow!!, view).isAppearanceLightStatusBars =
                darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}