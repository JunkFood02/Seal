package com.junkfood.seal.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.PreviewThemeLight
import com.junkfood.seal.ui.theme.applyOpacity
import com.junkfood.seal.ui.theme.harmonizeWithPrimary
import com.junkfood.seal.ui.theme.preferenceTitle

private const val horizontal = 8
private const val vertical = 16

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceItem(
    title: String,
    description: String? = null,
    icon: Any? = null,
    enabled: Boolean = true,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onClickLabel = onClickLabel,
            enabled = enabled,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                    )
                }

                is Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                    )
                }

                is Int -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .size(24.dp)
                            .padding(2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (icon == null) 12.dp else 0.dp)
                    .padding(end = 8.dp)
            ) {
                PreferenceItemTitle(text = title, enabled = enabled)
                if (description != null) PreferenceItemDescription(
                    text = description,
                    enabled = enabled
                )
            }
        }
    }

}

@Composable
@Preview
fun PreferenceItemPreview() {
    Column {
        PreferenceItem(title = "title", description = "description", icon = 0)
        PreferenceItem(title = "title", description = "description", icon = Icons.Outlined.Update)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceItemVariant(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onLongClickLabel: String? = null,
    onLongClick: () -> Unit = {},
    onClickLabel: String? = null,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.combinedClickable(
            enabled = enabled,
            onClick = onClick,
            onClickLabel = onClickLabel,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp, vertical.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
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
                    if (description != null) Text(
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
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 18.dp),
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.selectable(
            selected = selected, onClick = onClick
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(contentPadding),
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
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                modifier = Modifier
                    .padding()
                    .clearAndSetSemantics { },
            )
        }
    }
}

@Composable
internal fun PreferenceItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 2,
    style: TextStyle = preferenceTitle,
    enabled: Boolean,
    color: Color = MaterialTheme.colorScheme.onBackground,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        style = style,
        color = color.applyOpacity(enabled),
        overflow = overflow
    )
}

@Composable
internal fun PreferenceItemDescription(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    enabled: Boolean,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        modifier = modifier.padding(top = 2.dp),
        text = text,
        maxLines = maxLines,
        style = style,
        color = color.applyOpacity(enabled),
        overflow = overflow
    )
}

@Composable
@Preview
fun PreferenceSwitchPreview() {
    PreferenceSwitch(
        title = "PreferenceSwitch",
        description = "Supporting text",
        icon = Icons.Outlined.ToggleOn,
    )
}

@Composable
@Preview
fun PreferenceSwitchWithDividerPreview() {
    PreferenceSwitchWithDivider(
        title = "PreferenceSwitch",
        description = "Supporting text",
        icon = Icons.Outlined.ToggleOn,
    )
}

@Composable
fun PreferenceSwitch(
    title: String,
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
        modifier = Modifier.toggleable(value = isChecked,
            enabled = enabled,
            onValueChange = { onClick() })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp)
                .padding(start = if (icon == null) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                PreferenceItemTitle(
                    text = title, enabled = enabled
                )
                if (!description.isNullOrEmpty()) PreferenceItemDescription(
                    text = description, enabled = enabled
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 20.dp, end = 6.dp),
                enabled = enabled,
                thumbContent = thumbContent
            )
        }
    }
}


@Composable
fun PreferenceSwitchWithDivider(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isSwitchEnabled: Boolean = enabled,
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
        modifier = Modifier.clickable(
            enabled = enabled,
            onClick = onClick,
            onClickLabel = stringResource(id = R.string.open_settings)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                PreferenceItemTitle(text = title, enabled = enabled)
                if (!description.isNullOrEmpty()) PreferenceItemDescription(
                    text = description,
                    enabled = enabled
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
                modifier = Modifier
                    .padding(start = 12.dp, end = 6.dp)
                    .semantics {
                        contentDescription = title
                    },
                enabled = isSwitchEnabled,
                thumbContent = thumbContent
            )
        }
    }
}

@Composable
fun PreferencesCautionCard(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
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
                if (description != null) Text(
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
fun PreferencesHintCard(
    title: String = "Title ".repeat(2),
    description: String? = "Description text ".repeat(3),
    icon: ImageVector? = Icons.Outlined.Translate,
    isDarkTheme: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.run { if (isDarkTheme) onPrimaryContainer else secondaryContainer },
    contentColor: Color = MaterialTheme.colorScheme.run { if (isDarkTheme) surface else onSecondaryContainer },
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(backgroundColor)
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
                tint = contentColor
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
                    color = contentColor
                )
                if (description != null) Text(
                    text = description,
                    color = contentColor,
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
            title = "Title ".repeat(2),
            isChecked = isChecked,
            onClick = { isChecked = !isChecked },
            icon = null
        )
    }
}

@Composable
fun PreferenceSwitchWithContainer(
    title: String,
    icon: ImageVector? = null,
    isChecked: Boolean,
    onClick: () -> Unit,
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
                if (isChecked) primaryContainer else outline
            })
            .toggleable(value = isChecked) { onClick() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = with(MaterialTheme.colorScheme) { if (isChecked) onSurfaceVariant else surface })
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
        ) {
            with(MaterialTheme) {
                Text(
                    text = title,
                    maxLines = 2,
                    style = preferenceTitle,
                    color = if (isChecked) colorScheme.onSurface else colorScheme.surface
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
    Surface(modifier = Modifier.clickable { onClick() }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
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
                    license?.let {
                        Text(
                            text = it,
                            color = colorScheme.onSurfaceVariant.applyOpacity(enabled),
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = typography.bodyMedium,
                        )
                    }
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
            onClickLabel = stringResource(R.string.edit),
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
                    template?.let {
                        Text(
                            text = it,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = typography.bodyMedium,
                        )
                    }
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
            RadioButton(
                modifier = Modifier.semantics { contentDescription = label },
                selected = selected,
                onClick = onSelect
            )
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
            .padding(start = 18.dp, top = 24.dp, bottom = 12.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun PreferenceInfo(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector = Icons.Outlined.Info,
    applyPaddings: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth().run {
        if (applyPaddings) padding(horizontal = 16.dp, vertical = 16.dp)
        else this
    }) {
        Icon(
            modifier = Modifier.padding(), imageVector = icon, contentDescription = null
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreferenceInfoPreview() {
    PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
}