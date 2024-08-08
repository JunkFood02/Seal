package com.junkfood.seal.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R

@Composable
fun DialogSingleChoiceItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = true,
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = interactionSource
            )
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clearAndSetSemantics { },
            selected = selected,
            onClick = null,
            interactionSource = interactionSource
        )
        Text(text = text, style = LocalTextStyle.current.copy(fontSize = 16.sp))
    }
}

@Preview
@Composable
fun SingleChoiceItemPreview() {
    Surface {
        Column(modifier = Modifier.width(400.dp)) {
            DialogSingleChoiceItemVariant(
                title = "Better compatibility",
                desc = stringResource(R.string.prefer_compatibility_desc),
                selected = false
            ) {

            }
            DialogSingleChoiceItemVariant(
                title = "Better quality",
                desc = stringResource(R.string.prefer_quality_desc),
                selected = true
            ) {

            }
            DialogSingleChoiceItemVariant(
                title = "Better quality",
                desc = stringResource(R.string.prefer_quality_desc),
                selected = true,
                action = {
                    Spacer(modifier = Modifier.width(8.dp))
                    VerticalDivider(modifier = Modifier.height(32.dp))
                    IconButton(onClick = {}) { Icon(Icons.Outlined.Settings, null) }
                }
            ) {

            }
            DialogSingleChoiceItem(text = "Preview", selected = true) {

            }
        }

    }

}

@Composable
fun DialogSingleChoiceItemVariant(
    modifier: Modifier = Modifier,
    title: String,
    desc: String?,
    selected: Boolean,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = true,
                onClick = onClick,
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .clearAndSetSemantics { },
                    selected = selected,
                    onClick = null
                )
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            desc?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 48.dp)
                )
            }
        }
        action?.invoke() ?: Spacer(modifier = Modifier.width(16.dp))
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
fun DialogSwitchItem(
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
        val thumbContent: (@Composable () -> Unit)? = rememberThumbContent(isChecked = value)

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
        DialogSwitchItem(text = "Use cookies", value = value) {
            value = it
        }
    }

}