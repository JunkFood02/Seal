package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        modifier = modifier.padding(horizontal = 6.dp),
        selected = selected,
        onClick = onClick,
        label = {
            Text(text = label)
        },
        trailingIcon = {
            AnimatedVisibility(visible = selected) {
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
fun OutlinedButtonWithIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    )
    {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = icon,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text
        )
    }
}

@Composable
fun FilledTonalButtonWithIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    FilledTonalButton(
        modifier = modifier,
        onClick = onClick
    )
    {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = icon,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text
        )
    }
}

@Composable
fun FilledButtonWithIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String
) {
    Button(
        modifier = modifier,
        onClick = onClick
    )
    {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = icon,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text
        )
    }
}

@Composable
fun ConfirmButton(text: String = stringResource(R.string.confirm), onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text)
    }
}

@Composable
fun DismissButton(text: String = stringResource(R.string.dismiss), onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text)
    }
}


