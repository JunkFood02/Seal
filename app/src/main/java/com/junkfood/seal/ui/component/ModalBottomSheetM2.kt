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
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun rememberSheetState(
    showSheet: Boolean,
    onVisibilityChange: (isVisible: Boolean) -> Unit,
): ModalBottomSheetState {
    val state =
        rememberModalBottomSheetState(
            skipHalfExpanded = true,
            initialValue = ModalBottomSheetValue.Hidden,
        )
    LaunchedEffect(showSheet) {
        if (showSheet && state.targetValue == ModalBottomSheetValue.Hidden) {
            state.show()
        } else if (!showSheet && state.targetValue == ModalBottomSheetValue.Expanded) {
            state.hide()
        }
    }

    LaunchedEffect(state.targetValue) {
        when (state.targetValue) {
            ModalBottomSheetValue.Hidden -> onVisibilityChange(false)
            ModalBottomSheetValue.Expanded -> onVisibilityChange(true)
            else -> {}
        }
    }
    return state
}

@Preview
@Composable
private fun SheetTest() {
    var showSheet by remember { mutableStateOf(true) }
    val sheetState = rememberSheetState(showSheet = showSheet) { showSheet = it }
    val scope = rememberCoroutineScope()
    Surface {
        Column {
            Text("wtf")
            Text("showSheet = $showSheet")
            Button(onClick = { showSheet = true }) { Text("show sheet!") }
            Button(onClick = { scope.launch { sheetState.show() } }) { Text("sheetState.hide()") }
        }
    }

    SealModalBottomSheetM2(sheetState = sheetState) {
        Column {
            Button(onClick = { scope.launch { sheetState.hide() } }) { Text("sheetState.hide()") }

            Button(onClick = { showSheet = false }) { Text("showSheet = false") }
        }
    }
}

@Composable
fun SealModalBottomSheetM2(
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp),
    sheetGesturesEnabled: Boolean = true,
    sheetContent: @Composable ColumnScope.() -> Unit = {},
) {
    androidx.compose.material.ModalBottomSheetLayout(
        modifier = modifier,
        sheetShape =
            RoundedCornerShape(
                topStart = 28.0.dp,
                topEnd = 28.0.dp,
                bottomEnd = 0.0.dp,
                bottomStart = 0.0.dp,
            ),
        sheetState = sheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        sheetElevation = if (sheetState.isVisible) ModalBottomSheetDefaults.Elevation else 0.dp,
        sheetGesturesEnabled = sheetGesturesEnabled,
        sheetContent = {
            Column {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    Box(modifier = Modifier.padding(contentPadding)) {
                        Row(
                            modifier = modifier.padding(top = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier =
                                    modifier
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
                    modifier =
                        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    sheetGesturesEnabled: Boolean = true,
    sheetContent: @Composable ColumnScope.() -> Unit = {},
) {
    androidx.compose.material.ModalBottomSheetLayout(
        modifier = modifier,
        sheetShape =
            RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp,
            ),
        sheetState = sheetState,
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = if (sheetState.isVisible) ModalBottomSheetDefaults.Elevation else 0.dp,
        sheetGesturesEnabled = sheetGesturesEnabled,
        sheetContent = {
            Column { Box(modifier = Modifier) { Column { sheetContent() } } }
            NavigationBarSpacer(
                modifier =
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .fillMaxWidth()
            )
        },
    ) {}
}
