@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class)

package com.junkfood.seal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp


val Typography =
    Typography().run {
        copy(
            bodyLarge = bodyLarge.applyLinebreak().applyTextDirection(),
            bodyMedium = bodyMedium.applyLinebreak().applyTextDirection(),
            bodySmall = bodySmall.applyLinebreak().applyTextDirection(),
            titleLarge = titleLarge.applyTextDirection(),
            titleMedium = titleMedium.applyTextDirection(),
            titleSmall = titleSmall.applyTextDirection(),
            headlineSmall = headlineSmall.applyTextDirection(),
            headlineMedium = headlineMedium.applyTextDirection(),
            headlineLarge = headlineLarge.applyTextDirection(),
            displaySmall = displaySmall.applyTextDirection(),
            displayMedium = displayMedium.applyTextDirection(),
            displayLarge = displayLarge.applyTextDirection(),
            labelLarge = labelLarge.applyTextDirection(),
            labelMedium = labelMedium.applyTextDirection(),
            labelSmall = labelSmall.applyTextDirection(),
        )
    }

private fun TextStyle.applyLinebreak(): TextStyle = this.copy(lineBreak = LineBreak.Paragraph)
private fun TextStyle.applyTextDirection(): TextStyle =
    this.copy(textDirection = TextDirection.Content)


val preferenceTitle = Typography.titleMedium
