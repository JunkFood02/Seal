package com.junkfood.seal.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomDrawer(
    modifier: Modifier = Modifier,
    drawerState: ModalBottomSheetState = androidx.compose.material.rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    ),
    sheetContent: @Composable ColumnScope.() -> Unit = {},
    horizontalPadding: PaddingValues = PaddingValues(horizontal = 28.dp),
    content: @Composable () -> Unit = {},
) {
    androidx.compose.material.ModalBottomSheetLayout(
        modifier = modifier,
        sheetShape = RoundedCornerShape(
            topStart = 28.0.dp,
            topEnd = 28.0.dp,
            bottomEnd = 0.0.dp,
            bottomStart = 0.0.dp
        ),
        sheetState = drawerState,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        sheetElevation = if (drawerState.isVisible) ModalBottomSheetDefaults.Elevation else 0.dp,
        sheetContent = {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp,
                ) {
                    Box(modifier = Modifier.padding(horizontalPadding)) {
                        Row(
                            modifier = modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = modifier
                                    .size(32.dp, 4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.4f
                                        )
                                    )
                                    .zIndex(1f)
                            ) {}
                        }
                        Column {
                            Spacer(modifier = Modifier.height(40.dp))
                            sheetContent()
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
                }
                NavigationBarSpacer(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .fillMaxWidth()
                )
            }
        },
        content = content,
    )
}

@Composable
fun DrawerSheetSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}