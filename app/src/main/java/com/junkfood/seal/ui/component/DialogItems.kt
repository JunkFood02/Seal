package com.junkfood.seal.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

@Composable
fun SingleChoiceItem(
    modifier: Modifier = Modifier, text: String, selected: Boolean, onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = true,
                onClick = onClick,
            )
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            modifier = Modifier
                .padding(end = 8.dp)
                .clearAndSetSemantics { },
            selected = selected,
            onClick = onClick
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun CheckBoxItem(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .selectable(selected = checked,
                enabled = true,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier.clearAndSetSemantics { },
            checked = checked, onCheckedChange = { onClick() },
        )
        Text(
            modifier = Modifier,
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}