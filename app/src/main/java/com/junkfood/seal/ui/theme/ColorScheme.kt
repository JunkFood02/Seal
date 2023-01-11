package com.junkfood.seal.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.kyant.monet.dynamicColorScheme

@Composable
infix fun Color.withNight(nightColor: Color): Color {
    return if (LocalDarkTheme.current.isDarkTheme()) nightColor else this
}

const val DEFAULT_SEED_COLOR = 0xFF415f76.toInt()

