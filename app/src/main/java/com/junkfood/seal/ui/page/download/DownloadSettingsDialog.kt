package com.junkfood.seal.ui.page.download

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.ui.page.settings.download.AudioFormatDialog
import com.junkfood.seal.ui.page.settings.download.VideoFormatDialog
import com.junkfood.seal.ui.page.settings.download.VideoQualityDialog
import com.junkfood.seal.util.PreferenceUtil

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadSettingDialog(
    drawerState: ModalBottomSheetState,
    confirm: () -> Unit,
    cancel: () -> Unit
) {
    var audio by remember { mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.EXTRACT_AUDIO)) }
    var thumbnail by remember { mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.THUMBNAIL)) }
//    var open by remember { mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.OPEN_IMMEDIATELY)) }
    var showAudioFormatEditDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }

    BottomDrawer(drawerState = drawerState, sheetContent = {
        Icon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            imageVector = Icons.Outlined.DoneAll,
            contentDescription = stringResource(R.string.settings)
        )
        Text(
            text = stringResource(R.string.settings_before_download),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )
        Text(
            text = stringResource(R.string.settings_before_download_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        DrawerSheetSubtitle(text = stringResource(id = R.string.general_settings))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FilterChipWithAnimatedIcon(
                selected = audio,
                onClick = { audio = !audio },
                label = stringResource(R.string.extract_audio)
            )
            FilterChipWithAnimatedIcon(
                selected = thumbnail,
                onClick = { thumbnail = !thumbnail },
                label = stringResource(R.string.create_thumbnail)
            )
/*            FilterChipWithAnimatedIcon(
                selected = open,
                onClick = { open = !open },
                label = stringResource(R.string.open_when_finish)
            )*/
        }

        DrawerSheetSubtitle(text = stringResource(id = R.string.format))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(visible = !audio) {
                ButtonChip(
                    onClick = { showVideoFormatDialog = true },
                    label = stringResource(R.string.video_format),
                    icon = Icons.Outlined.VideoFile
                )
            }
            AnimatedVisibility(visible = !audio) {
                ButtonChip(
                    onClick = { showVideoQualityDialog = true },
                    label = stringResource(R.string.quality),
                    icon = Icons.Outlined._4k
                )
            }
            AnimatedVisibility(visible = audio) {
                ButtonChip(
                    onClick = { showAudioFormatEditDialog = true },
                    label = stringResource(R.string.convert_audio),
                    icon = Icons.Outlined.AudioFile
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp), horizontalArrangement = Arrangement.End
        ) {

            OutlinedButtonWithIcon(
                modifier = Modifier.padding(horizontal = 12.dp),
                onClick = cancel,
                icon = Icons.Outlined.Cancel,
                text = stringResource(R.string.cancel)
            )

            FilledButtonWithIcon(
                onClick = {
                    PreferenceUtil.updateValue(PreferenceUtil.EXTRACT_AUDIO, audio)
                    PreferenceUtil.updateValue(PreferenceUtil.THUMBNAIL, thumbnail)
//                    PreferenceUtil.updateValue(PreferenceUtil.OPEN_IMMEDIATELY, open)
                    cancel()
                    confirm()
                }, icon = Icons.Outlined.DownloadDone,
                text = stringResource(R.string.start_download)
            )

        }
    }
    )

    if (showAudioFormatEditDialog) {
        AudioFormatDialog(onDismissRequest = { showAudioFormatEditDialog = false }) {}
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(onDismissRequest = { showVideoQualityDialog = false }) {}
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(onDismissRequest = { showVideoFormatDialog = false }) {}
    }
}