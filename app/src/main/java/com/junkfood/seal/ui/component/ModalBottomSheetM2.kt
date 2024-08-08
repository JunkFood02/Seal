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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SealModalBottomSheetM2(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = androidx.compose.material.rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp),
    sheetGesturesEnabled: Boolean = true,
    sheetContent: @Composable ColumnScope.() -> Unit = {},
) {
    androidx.compose.material.ModalBottomSheetLayout(
        modifier = modifier,
        sheetShape = RoundedCornerShape(
            topStart = 28.0.dp,
            topEnd = 28.0.dp,
            bottomEnd = 0.0.dp,
            bottomStart = 0.0.dp
        ),
        sheetState = sheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        sheetElevation = if (sheetState.isVisible) ModalBottomSheetDefaults.Elevation else 0.dp,
        sheetGesturesEnabled = sheetGesturesEnabled,
        sheetContent = {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Box(modifier = Modifier.padding(contentPadding)) {
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
    ) {}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SealModalBottomSheetM2Variant(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = androidx.compose.material.rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    ),
    sheetGesturesEnabled: Boolean = true,
    sheetContent: @Composable ColumnScope.() -> Unit = {},
) {
    androidx.compose.material.ModalBottomSheetLayout(
        modifier = modifier,
        sheetShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        ),
        sheetState = sheetState,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = if (sheetState.isVisible) ModalBottomSheetDefaults.Elevation else 0.dp,
        sheetGesturesEnabled = sheetGesturesEnabled,
        sheetContent = {
            Column {
                Box(modifier = Modifier) {
                    Column {
                        sheetContent()
                    }
                }
            }
            NavigationBarSpacer(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .fillMaxWidth()
            )
        },
    ) {}
}