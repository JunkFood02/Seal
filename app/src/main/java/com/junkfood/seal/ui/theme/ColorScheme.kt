package com.junkfood.seal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.kyant.monet.n1

@Composable
fun Number.autoDark(isDarkTheme: Boolean = LocalDarkTheme.current.isDarkTheme()): Double =
    if (!isDarkTheme) this.toDouble()
    else when (this.toDouble()) {
        6.0 -> 98.0
        10.0 -> 99.0
        20.0 -> 95.0
        25.0 -> 90.0
        30.0 -> 90.0
        40.0 -> 80.0
        50.0 -> 60.0
        60.0 -> 50.0
        70.0 -> 40.0
        80.0 -> 40.0
        90.0 -> 30.0
        95.0 -> 20.0
        98.0 -> 10.0
        99.0 -> 10.0
        100.0 -> 20.0
        else -> this.toDouble()
    }

object Surfaces {
    const val LOWEST = 0
    const val LOW = 1
    const val STANDARD = 2
    const val HIGH = 3
    const val HIGHEST = 4

    @Composable
    fun isDark() = LocalDarkTheme.current.isDarkTheme()

    @Composable
    fun surfaceContainer(level: Int = STANDARD): Color =
        when (level) {
            LOWEST -> (if (isDark()) 4.0 else 100.0).n1
            LOW -> (if (isDark()) 10.0 else 96.0).n1
            STANDARD -> (if (isDark()) 12.0 else 94.0).n1
            HIGH -> (if (isDark()) 17.0 else 92.0).n1
            HIGHEST -> (if (isDark()) 22.0 else 90.0).n1
            else -> (if (isDark()) 12.0 else 94.0).n1
        }


    val surfaceContainer: Color
        @Composable get() = surfaceContainer(STANDARD)

    val surfaceDim: Color
        @Composable get() = (if (isDark()) 6.0 else 87.0).n1
    val surface: Color
        @Composable get() = (if (isDark()) 6.0 else 98.0).n1
    val surfaceBright: Color
        @Composable get() = (if (isDark()) 24.0 else 98.0).n1

}

const val DEFAULT_SEED_COLOR = 0xFF415f76.toInt()

