package com.junkfood.seal.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SingleChoiceItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
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
        Text(text = text, style = LocalTextStyle.current.copy(fontSize = 16.sp))
    }
}

@Preview
@Composable
fun SingleChoiceItemPreview() {
    Surface {
        Column {
            SingleChoiceItemWithLabel(
                text = "Better compatibility", label = "For sharing to other apps", selected = false
            ) {

            }
            SingleChoiceItemWithLabel(
                text = "Better quality", label = "For watching in compatible apps", selected = true
            ) {

            }
        }

    }

}

@Composable
fun SingleChoiceItemWithLabel(
    modifier: Modifier = Modifier,
    text: String,
    label: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = true,
                onClick = onClick,
            )
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp)
            .padding(vertical = 8.dp),
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
        Column {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            label?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding()
                )
            }
        }
    }
}

@Composable
fun CheckBoxItem(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .toggleable(
                value = checked, enabled = true, onValueChange = onValueChange
            ),
    ) {
        Row(
            modifier = modifier, verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                modifier = Modifier.clearAndSetSemantics { },
                checked = checked, onCheckedChange = onValueChange,
            )
            Text(
                modifier = Modifier, text = text, style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}

@Composable
fun SwitchItem(
    modifier: Modifier = Modifier,
    text: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = value, onValueChange = onValueChange)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        val thumbContent: (@Composable () -> Unit)? = if (value) {
            {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        } else {
            null
        }
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(
                density.density * 0.8f,
                density.fontScale
            )
        ) {
            Switch(
                checked = value,
                onCheckedChange = onValueChange,
                modifier = Modifier.clearAndSetSemantics { },
                thumbContent = thumbContent
            )
        }

    }
}

@Preview
@Composable
private fun SwitchItemPrev() {
    var value by remember { mutableStateOf(false) }
    Surface {
        SwitchItem(text = "Use cookies", value = value) {
            value = it
        }
    }

}