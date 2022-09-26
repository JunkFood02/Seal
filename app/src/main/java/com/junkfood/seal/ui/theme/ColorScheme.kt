package com.junkfood.seal.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import material.io.color.scheme.Scheme.dark

object ColorScheme {
    const val DEFAULT_SEED_COLOR = 0xFF415f76.toInt()
    fun lightColorSchemeFromColor(color: Int = DEFAULT_SEED_COLOR): ColorScheme {
        val lightScheme = material.io.color.scheme.Scheme.light(color)!!
        return lightColorScheme(
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
    }

    fun darkColorSchemeFromColor(color: Int = DEFAULT_SEED_COLOR): ColorScheme {
        val darkScheme = dark(color)!!
        return darkColorScheme(
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
    }
}