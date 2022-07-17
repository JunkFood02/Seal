package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.SingleChoiceItem
import com.junkfood.seal.util.PreferenceUtil

@Composable
fun AudioFormatDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
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
                PreferenceUtil.updateInt(PreferenceUtil.AUDIO_FORMAT, audioFormat)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    text = stringResource(R.string.audio_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in 0..2)
                    SingleChoiceItem(
                        text = PreferenceUtil.getAudioFormatDesc(i),
                        selected = audioFormat == i
                    ) { audioFormat = i }
            }
        })
}


@Composable
fun VideoFormatDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var videoFormat by remember { mutableStateOf(PreferenceUtil.getVideoFormat()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        title = {
            Text(stringResource(R.string.video_format_preference))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.updateInt(PreferenceUtil.VIDEO_FORMAT, videoFormat)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    text = stringResource(R.string.video_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in 0..2)
                    SingleChoiceItem(
                        text = PreferenceUtil.getVideoFormatDesc(i),
                        selected = videoFormat == i
                    ) { videoFormat = i }
            }
        })
}


@Composable
fun VideoQualityDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var videoQuality by remember { mutableStateOf(PreferenceUtil.getVideoQuality()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        title = {
            Text(stringResource(R.string.video_quality))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.updateInt(PreferenceUtil.VIDEO_QUALITY, videoQuality)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            LazyColumn {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        text = stringResource(R.string.video_quality_desc),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                for (i in 0..4)
                    item {
                        SingleChoiceItem(
                            text = PreferenceUtil.getVideoQualityDesc(i),
                            selected = videoQuality == i
                        ) { videoQuality = i }
                    }
            }
        })
}