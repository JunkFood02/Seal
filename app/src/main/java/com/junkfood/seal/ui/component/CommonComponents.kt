package com.junkfood.seal.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavigationBarSpacer(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier.height(
            with(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()) {
                if (this.value > 30f) this else 0f.dp
            }
        )
    )
}