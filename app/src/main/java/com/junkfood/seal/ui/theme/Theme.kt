package com.junkfood.seal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.junkfood.seal.ui.theme.ColorScheme.darkColorSchemeFromColor
import com.junkfood.seal.ui.theme.ColorScheme.lightColorSchemeFromColor


fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
fun SealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= 31,
    seedColor: Int,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val versionCheck = (Build.VERSION.SDK_INT >= 31)

    rememberSystemUiController().run {
        setStatusBarColor(Color.Transparent, !darkTheme)
        setSystemBarsColor(Color.Transparent, !darkTheme)
        setNavigationBarColor(Color.Transparent, !darkTheme)
    }

    val colorScheme =
        when {
            (dynamicColor && versionCheck) && darkTheme -> dynamicDarkColorScheme(context)
            (versionCheck && dynamicColor) && !darkTheme -> dynamicLightColorScheme(context)
            darkTheme -> darkColorSchemeFromColor(seedColor)
            else -> lightColorSchemeFromColor(seedColor)
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}