@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class)

package com.junkfood.seal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp


val Typography =
    Typography().run {
        copy(
            bodyLarge = bodyLarge.applyLinebreak(),
            bodyMedium = bodyMedium.applyLinebreak(),
            bodySmall = bodySmall.applyLinebreak(),
            titleLarge = titleLarge,
            titleMedium = titleMedium,
            titleSmall = titleSmall,
            headlineSmall = headlineSmall,
            headlineMedium = headlineMedium,
            headlineLarge = headlineLarge,
            displaySmall = displaySmall,
            displayMedium = displayMedium,
            displayLarge = displayLarge,
            labelLarge = labelLarge,
            labelMedium = labelMedium,
            labelSmall = labelSmall
        )
    }

@OptIn(ExperimentalTextApi::class)
private fun TextStyle.applyLinebreak(): TextStyle = this.copy(lineBreak = LineBreak.Paragraph)

@OptIn(ExperimentalTextApi::class)
val preferenceTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp, lineHeight = 24.sp,
    lineBreak = LineBreak.Paragraph,
)
