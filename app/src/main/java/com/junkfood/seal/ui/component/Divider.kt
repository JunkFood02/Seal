package com.junkfood.seal.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun HorizontalDivider(modifier: Modifier=Modifier,color: Color = MaterialTheme.colorScheme.outlineVariant) {
    Divider(
        modifier = modifier
            .fillMaxWidth()
            .size(DividerDefaults.Thickness),
        color = color
    )
}