package com.kyant.monet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes

val LocalTonalPalettes = staticCompositionLocalOf {
    Color(0xFF007FAC).toTonalPalettes()
}

inline val Number.a1: Color
    @Composable
    get() = LocalTonalPalettes.current accent1 toDouble()

inline val Number.a2: Color
    @Composable get() = LocalTonalPalettes.current accent2 toDouble()

inline val Number.a3: Color
    @Composable get() = LocalTonalPalettes.current accent3 toDouble()

inline val Number.n1: Color
    @Composable get() = LocalTonalPalettes.current neutral1 toDouble()

inline val Number.n2: Color
    @Composable get() = LocalTonalPalettes.current neutral2 toDouble()

@Composable
fun dynamicColorScheme(isLight: Boolean = !isSystemInDarkTheme()): ColorScheme {
    return if (isLight) {
        lightColorScheme(
            background = 98.n1,
            inverseOnSurface = 95.n1,
            inversePrimary = 80.a1,
            inverseSurface = 20.n1,
            onBackground = 10.n1,
            onPrimary = 100.a1,
            onPrimaryContainer = 10.a1,
            onSecondary = 100.a2,
            onSecondaryContainer = 10.a2,
            onSurface = 10.n1,
            onSurfaceVariant = 30.n2,
            onTertiary = 100.a3,
            onTertiaryContainer = 10.a3,
            outline = 50.n2,
            outlineVariant = 80.n2,
            primary = 40.a1,
            primaryContainer = 90.a1,
//            scrim = 0.n1,
            secondary = 40.a2,
            secondaryContainer = 90.a2,
            surface = 98.n1,
            surfaceVariant = 90.n2,
            tertiary = 40.a3,
            tertiaryContainer = 90.a3,
            surfaceBright = 98.n1,
            surfaceDim = 87.n1,
            surfaceContainerLowest = 100.n1,
            surfaceContainerLow = 96.n1,
            surfaceContainer = 94.n1,
            surfaceContainerHigh = 92.n1,
            surfaceContainerHighest = 90.n1,
        )
    } else {
        darkColorScheme(
            background = 6.n1,
            inverseOnSurface = 20.n1,
            inversePrimary = 40.a1,
            inverseSurface = 90.n1,
            onBackground = 90.n1,
            onPrimary = 20.a1,
            onPrimaryContainer = 90.a1,
            onSecondary = 20.a2,
            onSecondaryContainer = 90.a2,
            onSurface = 90.n1,
            onSurfaceVariant = 80.n2,
            onTertiary = 20.a3,
            onTertiaryContainer = 90.a3,
            outline = 60.n2,
            outlineVariant = 30.n2,
            primary = 80.a1,
            primaryContainer = 30.a1,
//            scrim = 0.n1,
            secondary = 80.a2,
            secondaryContainer = 30.a2,
            surface = 6.n1,
            surfaceVariant = 30.n2,
            tertiary = 80.a3,
            tertiaryContainer = 30.a3,
            surfaceBright = 24.n1,
            surfaceDim = 6.n1,
            surfaceContainerLowest = 4.n1,
            surfaceContainerLow = 10.n1,
            surfaceContainer = 12.n1,
            surfaceContainerHigh = 17.n1,
            surfaceContainerHighest = 22.n1,
        )
    }
}
