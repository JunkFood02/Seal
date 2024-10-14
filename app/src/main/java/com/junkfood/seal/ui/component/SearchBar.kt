package com.junkfood.seal.ui.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.SealTheme

@Composable
fun SealSearchBar(
    modifier: Modifier = Modifier,
    text: String,
    placeholderText: String,
    onValueChange: (String) -> Unit,
) {
    val view = LocalView.current

    Surface(
        modifier = modifier.widthIn(360.dp, 720.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SealAutoFocusTextField(
                value = text,
                onValueChange = onValueChange,
                placeholder = { Text(text = placeholderText) },
                modifier = Modifier.weight(1f),
                contentDescription = stringResource(id = R.string.search),
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onValueChange("")
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = stringResource(id = R.string.clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun SearchBarPreview() {
    var text by remember { mutableStateOf("") }
    SealTheme {
        Surface {
            SealSearchBar(
                text = text,
                placeholderText = stringResource(R.string.search_in_downloads),
            ) {
                text = it
            }
        }
    }
}
