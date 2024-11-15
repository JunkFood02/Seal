package com.junkfood.seal.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SealModalBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState =
        with(LocalDensity.current) {
            SheetState(
                initialValue = SheetValue.Expanded,
                skipPartiallyExpanded = true,
                velocityThreshold = { 56.dp.toPx() },
                positionalThreshold = { 125.dp.toPx() },
            )
        },
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp),
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        properties = properties,
    ) {
        Column(modifier = Modifier.padding(paddingValues = contentPadding)) {
            content()
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun DrawerSheetSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge,
    )
}
