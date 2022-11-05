package com.junkfood.seal.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.PreviewThemeLight
import com.junkfood.seal.ui.theme.applyOpacity
import com.junkfood.seal.ui.theme.harmonizeWithPrimary


@Composable
fun PreferenceItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick, enabled = enabled)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (icon == null) 12.dp else 0.dp)
                    .padding(end = 8.dp)
            ) {
                with(MaterialTheme) {
                    Text(
                        text = title,
                        maxLines = 1,
                        style = typography.titleLarge.copy(fontSize = 20.sp),
                        color = colorScheme.onSurface.applyOpacity(enabled)
                    )
                    if (description != null)
                        Text(
                            text = description,
                            color = colorScheme.onSurfaceVariant.applyOpacity(enabled),
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = typography.bodyMedium,
                        )
                }
            }
        }
    }

}

@Composable
fun PreferenceItemVariant(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (icon == null) 12.dp else 0.dp)
                    .padding(end = 8.dp)
            ) {
                with(MaterialTheme) {

                    Text(
                        text = title,
                        maxLines = 1,
                        style = typography.titleMedium,
                        color = colorScheme.onSurface.applyOpacity(enabled)
                    )
                    if (description != null)
                        Text(
                            text = description,
                            color = colorScheme.onSurfaceVariant.applyOpacity(enabled),
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = typography.bodyMedium,
                        )
                }
            }
        }
    }

}

@Composable
fun PreferenceSingleChoiceItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = text,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface, overflow = TextOverflow.Ellipsis
                )
            }
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier.padding(start = 20.dp, end = 8.dp),
            )
        }
    }
}

@Composable
fun PreferenceSwitch(
    title: String = "",
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    checkedIcon: ImageVector = Icons.Outlined.Check,
    onClick: (() -> Unit) = {},
) {
    val thumbContent: (@Composable () -> Unit)? = if (isChecked) {
        {
            Icon(
                imageVector = checkedIcon,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }
    Surface(
        modifier = Modifier.toggleable(
            value = isChecked,
            enabled = enabled,
            onValueChange = { onClick() }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface.applyOpacity(enabled),
                    overflow = TextOverflow.Ellipsis
                )
                if (!description.isNullOrEmpty())
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled),
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                    )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 20.dp, end = 6.dp),
                enabled = enabled, thumbContent = thumbContent
            )
        }
    }
}


@Composable
@Preview
fun PreferenceSwitchWithDivider(
    title: String = "",
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    checkedIcon: ImageVector = Icons.Outlined.Check,
    onClick: (() -> Unit) = {},
    onChecked: () -> Unit = {}
) {
    val thumbContent: (@Composable () -> Unit)? = if (isChecked) {
        {
            Icon(
                imageVector = checkedIcon,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }
    Surface(
        modifier = Modifier
            .clickable(
                enabled = enabled, onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 20.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface.applyOpacity(enabled),
                    overflow = TextOverflow.Ellipsis
                )
                if (!description.isNullOrEmpty())
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled),
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                    )
            }
            Divider(
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 8.dp)
                    .width(1f.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Switch(
                checked = isChecked,
                onCheckedChange = { onChecked() },
                modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                enabled = enabled, thumbContent = thumbContent
            )
        }
    }
}

@Composable
fun PreferencesCaution(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.errorContainer.harmonizeWithPrimary())
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.error.harmonizeWithPrimary()

            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
        ) {
            with(MaterialTheme) {

                Text(
                    text = title,
                    maxLines = 1,
                    style = typography.titleLarge.copy(fontSize = 20.sp),
                    color = colorScheme.onErrorContainer.harmonizeWithPrimary()
                )
                if (description != null)
                    Text(
                        text = description,
                        color = colorScheme.onErrorContainer.harmonizeWithPrimary(),
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium,
                    )
            }
        }
    }


}

@Composable
@Preview
fun PreferencesHint(
    title: String = "Title ".repeat(2),
    description: String? = "Description text ".repeat(3),
    icon: ImageVector? = Icons.Outlined.Translate,
    onClick: () -> Unit = {},
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
        ) {
            with(MaterialTheme) {

                Text(
                    text = title,
                    maxLines = 1,
                    style = typography.titleLarge.copy(fontSize = 20.sp),
                    color = colorScheme.onSecondaryContainer
                )
                if (description != null)
                    Text(
                        text = description,
                        color = colorScheme.onSecondaryContainer,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium,
                    )
            }
        }
    }
}

@Composable
@Preview
private fun PreferenceSwitchWithContainerPreview() {
    var isChecked by remember { mutableStateOf(false) }
    PreviewThemeLight {
        PreferenceSwitchWithContainer(
            description = null,
            isChecked = isChecked,
            onClick = { isChecked = !isChecked })
    }
}

@Composable
fun PreferenceSwitchWithContainer(
    title: String = "Title ".repeat(2),
    description: String? = "Description text ".repeat(3),
    icon: ImageVector? = Icons.Outlined.Translate,
    isChecked: Boolean = true,
    onClick: () -> Unit = {},
) {
    val thumbContent: (@Composable () -> Unit)? = if (isChecked) {
        {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(with(MaterialTheme.colorScheme) {
                if (isChecked) primaryContainer else outline }
            )
            .selectable(selected = isChecked) { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = with(MaterialTheme.colorScheme) { if (isChecked) secondary else surface }
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
        ) {
            with(MaterialTheme) {

                Text(
                    text = title,
                    maxLines = 1,
                    style = typography.titleLarge.copy(fontSize = 20.sp),
                    color = if (isChecked) colorScheme.onSecondaryContainer else colorScheme.surface
                )
                if (description != null)
                    Text(
                        text = description,
                        color = if (isChecked) colorScheme.onSecondaryContainer else colorScheme.surface,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium,
                    )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            modifier = Modifier.padding(start = 12.dp, end = 6.dp),
            thumbContent = thumbContent
        )
    }
}

@Composable
fun CreditItem(
    title: String,
    license: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                with(MaterialTheme) {
                    Text(
                        text = title,
                        maxLines = 1,
                        style = typography.titleMedium,
                        color = colorScheme.onSurface.applyOpacity(enabled)
                    )
                    Text(
                        text = license.toString(),
                        color = colorScheme.onSurfaceVariant.applyOpacity(enabled),
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium,
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun TemplateItem(
    label: String = "",
    template: String? = null,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onSelect: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onClickLabel = stringResource(R.string.edit_custom_command_template),
            onLongClick = onLongClick,
            onLongClickLabel = stringResource(R.string.remove_template)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                with(MaterialTheme) {
                    Text(
                        text = label,
                        maxLines = 1,
                        style = typography.titleMedium,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = template.toString(),
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium,
                    )
                }
            }
            Divider(
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 12.dp)
                    .width(1f.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            RadioButton(selected = selected, onClick = onSelect)
        }


    }

}

@Composable
fun PreferenceSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 28.dp, bottom = 12.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}

