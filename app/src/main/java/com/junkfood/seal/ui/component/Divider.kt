package com.junkfood.seal.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
@Deprecated(
    "",
    replaceWith = ReplaceWith("androidx.compose.material3.HorizontalDivider(modifier,color)"),
)
fun HorizontalDivider(modifier: Modifier = Modifier, color: Color = DividerDefaults.color) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = color,
        thickness = DividerDefaults.Thickness,
    )
}
