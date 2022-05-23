package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.SingleChoiceItem
import com.junkfood.seal.util.PreferenceUtil

@Composable
fun AudioFormatDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    var audioFormat by remember { mutableStateOf(PreferenceUtil.getAudioFormat()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        title = {
            Text(stringResource(R.string.audio_format))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.updateValue(PreferenceUtil.AUDIO_FORMAT, audioFormat)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column {
                SingleChoiceItem(
                    text = PreferenceUtil.getAudioFormatDesc(0),
                    selected = audioFormat == 0
                ) { audioFormat = 0 }
                SingleChoiceItem(
                    text = PreferenceUtil.getAudioFormatDesc(1),
                    selected = audioFormat == 1
                ) { audioFormat = 1 }
                SingleChoiceItem(
                    text = PreferenceUtil.getAudioFormatDesc(2),
                    selected = audioFormat == 2
                ) { audioFormat = 2 }
            }
        })
}