package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.FixedAccentColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonChip(
    modifier: Modifier = Modifier,
    label: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconDescription: String? = null,
    onClick: () -> Unit,
) {
    ElevatedAssistChip(
        modifier = modifier.padding(horizontal = 4.dp),
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.elevatedAssistChipColors(leadingIconContentColor = iconColor),
        enabled = enabled,
        leadingIcon = {
            if (icon != null)
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    modifier = Modifier.size(AssistChipDefaults.IconSize))
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatButtonChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    AssistChip(
        modifier = modifier.padding(horizontal = 4.dp),
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                labelColor = labelColor,
                leadingIconContentColor = iconColor),
        border = null,
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                Modifier.size(AssistChipDefaults.IconSize))
        },
        label = { Text(text = label) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedButtonChip(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    label: String,
    shape: Shape = AssistChipDefaults.shape,
    onClick: () -> Unit
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        leadingIcon = {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    Modifier.size(AssistChipDefaults.IconSize))
            }
        },
        label = { Text(text = label) },
        shape = shape)
}

@Composable
fun SingleChoiceChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    label: String,
    leadingIcon: ImageVector = Icons.Outlined.Check,
    onClick: () -> Unit,
) {
    FilterChip(
        modifier = modifier.padding(horizontal = 4.dp),
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        label = { Text(text = label) },
        leadingIcon = {
            Row {
                AnimatedVisibility(visible = selected) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize))
                }
            }
        },
    )
}

@Composable
fun VideoFilterChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    label: String,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        modifier = modifier.padding(horizontal = 4.dp),
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = { leadingIcon?.let { Icon(imageVector = it, contentDescription = null) } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutChip(
    modifier: Modifier = Modifier,
    text: String,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    AssistChip(
        modifier = modifier.padding(horizontal = 4.dp),
        onClick = { onClick?.invoke() },
        label = { Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingIcon = {
            onRemove?.let {
                IconButton(
                    onClick = onRemove, modifier = Modifier.size(InputChipDefaults.IconSize)) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(id = R.string.remove),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(InputChipDefaults.IconSize))
                    }
            }
        })
}

@Composable
fun SingleSelectChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: SelectableChipColors =
        FilterChipDefaults.filterChipColors(
            selectedContainerColor = FixedAccentColors.secondaryFixed,
            selectedLabelColor = FixedAccentColors.onSecondaryFixed,
            selectedLeadingIconColor = FixedAccentColors.onSecondaryFixed,
            selectedTrailingIconColor = FixedAccentColors.onSecondaryFixed,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            iconColor = MaterialTheme.colorScheme.onSurface,
            labelColor = MaterialTheme.colorScheme.onSurface),
    border: BorderStroke? = null,
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = {},
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.extraLarge,
        colors = colors,
        elevation = elevation,
        border = border,
        interactionSource = interactionSource)
}
