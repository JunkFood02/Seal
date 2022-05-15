package com.junkfood.seal.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun PreferenceItem(
    title: String,
    description: String,
    icon: ImageVector?,
    enabled: Boolean,

    onClick: () -> Unit,
) {
    Surface(
        modifier = if (enabled) Modifier.clickable { onClick() } else Modifier
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
                    contentDescription = title,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(28.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (icon == null) 12.dp else 0.dp)
            ) {
                with(MaterialTheme) {

                    Text(
                        text = title,
                        maxLines = 1,
                        style = typography.titleLarge,
                        color = if (enabled) colorScheme.onSurface else colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )

                    Text(
                        text = description,
                        color = if (enabled) colorScheme.onSurface.copy(alpha = 0.7f) else colorScheme.onSurface.copy(
                            alpha = 0.5f
                        ),
                        maxLines = 1,
                        style = typography.bodyMedium,
                    )
                }
            }
        }
    }

}

@Composable
fun PreferenceSwitch(
    title: String,
    description: String,
    icon: ImageVector?,
    enabled: Boolean = true,
    onClick: (() -> Unit),
    isChecked: Boolean,
) {
    Surface(
        modifier = if (enabled) Modifier.clickable { onClick() } else Modifier
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
                    contentDescription = title,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(28.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (icon == null) 12.dp else 0.dp)
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
                )
                Text(
                    text = description,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    ),
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 16.dp, end = 6.dp), enabled = enabled
            )
        }
    }
}

@Composable
fun Subtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 28.dp, top = 28.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}