package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R

@Composable
fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    ElevatedAssistChip(
        modifier = modifier.padding(horizontal = 6.dp),
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.elevatedAssistChipColors(),
        enabled = enabled,
        leadingIcon = {
            if (icon != null) Icon(
                imageVector = icon, null, modifier = Modifier.size(18.dp)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipWithIcon(
    modifier: Modifier = Modifier,
    select: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        modifier = modifier.padding(horizontal = 6.dp),
        selected = select,
        onClick = onClick,
        label = {
            Text(text = label)
        },
        trailingIcon = {
            AnimatedVisibility(visible = select) {
                Icon(
                    Icons.Outlined.Check,
                    stringResource(R.string.checked),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
    )
}


@Composable
fun DrawerSheetSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 6.dp, top = 18.dp, bottom = 9.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge
    )
}