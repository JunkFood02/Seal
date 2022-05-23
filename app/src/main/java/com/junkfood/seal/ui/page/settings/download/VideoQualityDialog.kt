package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun VideoQualityDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    var videoQuality by remember { mutableStateOf(PreferenceUtil.getVideoQuality()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        title = {
            Text(stringResource(R.string.quality))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.updateValue(PreferenceUtil.VIDEO_QUALITY, videoQuality)
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
                    text = stringResource(R.string.video_quality_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for(i in 0..3)
                SingleChoiceItem(
                    text = PreferenceUtil.getVideoQualityDesc(i),
                    selected = videoQuality == i
                ) { videoQuality = i }

            }
        })
}