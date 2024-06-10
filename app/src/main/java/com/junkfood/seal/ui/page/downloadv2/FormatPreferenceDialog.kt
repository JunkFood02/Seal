package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DialogSingleChoiceItem
import com.junkfood.seal.ui.component.DialogSubtitle
import com.junkfood.seal.ui.component.SealDialog

@Composable
fun FormatPreferenceDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit) {
    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        icon = { Icon(imageVector = Icons.Outlined.VideoFile, contentDescription = null) },
        title = { Text(stringResource(id = R.string.format_preference)) },
        text = {
            Column {
                DialogSubtitle(text = stringResource(R.string.presets))
                LazyColumn {
                    item {
                        DialogSingleChoiceItem(
                            text = "1080p",
                            selected = false
                        ) { }
                    }
                    item {
                        DialogSingleChoiceItem(
                            text = "720p",
                            selected = false
                        ) { }
                    }
                }
            }
        },

        )
}

@Composable
fun AudioFormatPreferences(modifier: Modifier = Modifier) {

}

@Composable
fun VideoFormatPreference(modifier: Modifier = Modifier) {

}

@Preview
@Composable
private fun Preview() {
    FormatPreferenceDialog { }
}