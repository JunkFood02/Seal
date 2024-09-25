package com.junkfood.seal.ui.component

import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
@Deprecated(
    "",
    level = DeprecationLevel.ERROR,
    replaceWith =
        ReplaceWith(
            "androidx.compose.material3.HorizontalDivider(modifier = modifier, color = color)",
            "androidx",
        ),
)
fun HorizontalDivider(modifier: Modifier = Modifier, color: Color = DividerDefaults.color) =
    androidx.compose.material3.HorizontalDivider(modifier = modifier, color = color)
