package com.junkfood.seal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val LightColorScheme = lightColorScheme(
    primary = Color(lightScheme.primary),
    onPrimary = Color(lightScheme.onPrimary),
    primaryContainer = Color(lightScheme.primaryContainer),
    onPrimaryContainer = Color(lightScheme.onPrimaryContainer),
    secondary = Color(lightScheme.secondary),
    onSecondary = Color(lightScheme.onSecondary),
    secondaryContainer = Color(lightScheme.secondaryContainer),
    onSecondaryContainer = Color(lightScheme.onSecondaryContainer),
    tertiary = Color(lightScheme.tertiary),
    onTertiary = Color(lightScheme.onTertiary),
    tertiaryContainer = Color(lightScheme.tertiaryContainer),
    onTertiaryContainer = Color(lightScheme.onTertiaryContainer),
    error = Color(lightScheme.error),
    errorContainer = Color(lightScheme.errorContainer),
    onError = Color(lightScheme.onError),
    onErrorContainer = Color(lightScheme.onErrorContainer),
    background = Color(lightScheme.background),
    onBackground = Color(lightScheme.onBackground),
    surface = Color(lightScheme.surface),
    onSurface = Color(lightScheme.onSurface),
    surfaceVariant = Color(lightScheme.surfaceVariant),
    onSurfaceVariant = Color(lightScheme.onSurfaceVariant),
    outline = Color(lightScheme.outline),
    inverseOnSurface = Color(lightScheme.inverseOnSurface),
    inverseSurface = Color(lightScheme.inverseSurface),
    inversePrimary = Color(lightScheme.inversePrimary),
)
private val DarkColorScheme = darkColorScheme(
    primary = Color(darkScheme.primary),
    onPrimary = Color(darkScheme.onPrimary),
    primaryContainer = Color(darkScheme.primaryContainer),
    onPrimaryContainer = Color(darkScheme.onPrimaryContainer),
    secondary = Color(darkScheme.secondary),
    onSecondary = Color(darkScheme.onSecondary),
    secondaryContainer = Color(darkScheme.secondaryContainer),
    onSecondaryContainer = Color(darkScheme.onSecondaryContainer),
    tertiary = Color(darkScheme.tertiary),
    onTertiary = Color(darkScheme.onTertiary),
    tertiaryContainer = Color(darkScheme.tertiaryContainer),
    onTertiaryContainer = Color(darkScheme.onTertiaryContainer),
    error = Color(darkScheme.error),
    errorContainer = Color(darkScheme.errorContainer),
    onError = Color(darkScheme.onError),
    onErrorContainer = Color(darkScheme.onErrorContainer),
    background = Color(darkScheme.background),
    onBackground = Color(darkScheme.onBackground),
    surface = Color(darkScheme.surface),
    onSurface = Color(darkScheme.onSurface),
    surfaceVariant = Color(darkScheme.surfaceVariant),
    onSurfaceVariant = Color(darkScheme.onSurfaceVariant),
    outline = Color(darkScheme.outline),
    inverseOnSurface = Color(darkScheme.inverseOnSurface),
    inverseSurface = Color(darkScheme.inverseSurface),
    inversePrimary = Color(darkScheme.inversePrimary),
)

fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
fun SealTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= 31,
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
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}