package com.junkfood.seal.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun PreferenceItem(
    title: String,
    description: String,
    icon: ImageVector?,
    enable: Boolean,

    onClick: () -> Unit,
) {
    Surface(
        modifier = if (enable) Modifier.clickable { onClick() } else Modifier
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
                        color = if (enable) colorScheme.onSurface else colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )

                    Text(
                        text = description,
                        color = if (enable) colorScheme.onSurface.copy(alpha = 0.7f) else colorScheme.onSurface.copy(
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
    onClick: (() -> Unit),
    isChecked: Boolean,
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            androidx.compose.material3.Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 16.dp, end = 6.dp)
            )
        }
    }
}
