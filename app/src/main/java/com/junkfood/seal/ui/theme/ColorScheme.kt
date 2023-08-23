package com.junkfood.seal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.kyant.monet.a1
import com.kyant.monet.a2
import com.kyant.monet.a3
import io.material.hct.Hct

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

object FixedAccentColors {
    val primaryFixed: Color
        @Composable get() = 90.a1
    val primaryFixedDim: Color
        @Composable get() = 80.a1
    val onPrimaryFixed: Color
        @Composable get() = 10.a1
    val onPrimaryFixedVariant: Color
        @Composable get() = 30.a1
    val secondaryFixed: Color
        @Composable get() = 90.a2
    val secondaryFixedDim: Color
        @Composable get() = 80.a2
    val onSecondaryFixed: Color
        @Composable get() = 10.a2
    val onSecondaryFixedVariant: Color
        @Composable get() = 30.a2
    val tertiaryFixed: Color
        @Composable get() = 90.a3
    val tertiaryFixedDim: Color
        @Composable get() = 80.a3
    val onTertiaryFixed: Color
        @Composable get() = 10.a3
    val onTertiaryFixedVariant: Color
        @Composable get() = 30.a3
}

const val DEFAULT_SEED_COLOR = 0xa3d48d

/**
 * @receiver Seed number used for generating color
 * @return a [Color] generated using [Hct] algorithm, harmonized with `primary` color
 */
@Composable
fun Int.generateLabelColor(): Color =
    Color(
        Hct.from(
            hue = (this % 360).toDouble(),
            chroma = 36.0,
            tone = 80.0
        ).toInt()
    ).harmonizeWithPrimary()

