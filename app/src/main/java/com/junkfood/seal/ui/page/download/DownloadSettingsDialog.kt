package com.junkfood.seal.ui.page.download

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BottomDrawer
import com.junkfood.seal.ui.component.ButtonChip
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.FilterChip
import com.junkfood.seal.ui.component.FilterChipWithIcons
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.page.settings.download.*
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.PLAYLIST
import com.junkfood.seal.util.PreferenceUtil.SUBTITLE
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE_INDEX
import com.junkfood.seal.util.PreferenceUtil.THUMBNAIL

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun DownloadSettingDialog(
    useDialog: Boolean = false,
    dialogState: Boolean = false,
    drawerState: ModalBottomSheetState,
    confirm: () -> Unit,
    hide: () -> Unit
) {
    var audio by remember { mutableStateOf(PreferenceUtil.getValue(EXTRACT_AUDIO)) }
    var thumbnail by remember { mutableStateOf(PreferenceUtil.getValue(THUMBNAIL)) }
    var customCommand by remember { mutableStateOf(PreferenceUtil.getValue(CUSTOM_COMMAND)) }
    var playlist by remember { mutableStateOf(PreferenceUtil.getValue(PLAYLIST)) }
    var subtitle by remember { mutableStateOf(PreferenceUtil.getValue(SUBTITLE)) }

    var showAudioFormatEditDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }
    var showCustomCommandDialog by remember { mutableStateOf(0) }
    var selectedTemplateIndex by remember {
        mutableStateOf(PreferenceUtil.getInt(TEMPLATE_INDEX, 0))
    }
    var showRenameDialog by remember { mutableStateOf(false) }

    val templateList = DatabaseUtil.getTemplateFlow().collectAsState(ArrayList()).value

    val updatePreferences = {
        PreferenceUtil.updateValue(EXTRACT_AUDIO, audio)
        PreferenceUtil.updateValue(THUMBNAIL, thumbnail)
        PreferenceUtil.updateValue(CUSTOM_COMMAND, customCommand)
        PreferenceUtil.updateValue(PLAYLIST, playlist)
        PreferenceUtil.updateValue(SUBTITLE, subtitle)
        PreferenceUtil.updateInt(TEMPLATE_INDEX, selectedTemplateIndex)
    }

    val downloadButtonCallback = {
        updatePreferences()
        hide()
        confirm()
    }


    val sheetContent: @Composable () -> Unit = {
        Column {
            Text(
                text = stringResource(R.string.settings_before_download_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            DrawerSheetSubtitle(text = stringResource(id = R.string.general_settings))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = audio,
                    enabled = !customCommand,
                    onClick = {
                        audio = !audio
                        updatePreferences()
                    },
                    label = stringResource(R.string.extract_audio)
                )
                FilterChip(
                    selected = playlist,
                    enabled = !customCommand,
                    onClick = {
                        playlist = !playlist
                        updatePreferences()
                    },
                    label = stringResource(R.string.download_playlist)
                )
                FilterChip(
                    selected = subtitle,
                    enabled = !customCommand && !audio,
                    onClick = {
                        subtitle = !subtitle
                        updatePreferences()
                    },
                    label = stringResource(id = R.string.embed_subtitles)
                )
                FilterChip(
                    selected = thumbnail,
                    enabled = !customCommand,
                    onClick = {
                        thumbnail = !thumbnail
                        updatePreferences()
                    },
                    label = stringResource(R.string.create_thumbnail)
                )
            }

            DrawerSheetSubtitle(text = stringResource(id = R.string.rename))
            ButtonChip(
                onClick = { showRenameDialog = true }, label = stringResource(
                    R.string.rename
                ), icon = Icons.Outlined.Add
            )

            DrawerSheetSubtitle(text = stringResource(id = R.string.advanced_settings))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = customCommand,
                    onClick = {
                        customCommand = !customCommand
                        updatePreferences()
                    },
                    label = stringResource(R.string.custom_command)
                )
                ButtonChip(
                    onClick = { showCustomCommandDialog = -1 }, label = stringResource(
                        R.string.new_template
                    ), icon = Icons.Outlined.Add, enabled = customCommand
                )
                ButtonChip(
                    onClick = { showCustomCommandDialog = 1 }, label = stringResource(
                        R.string.edit_custom_command_template
                    ), icon = Icons.Outlined.EditNote, enabled = customCommand
                )
            }

            DrawerSheetSubtitle(
                text = stringResource(
                    if (customCommand) R.string.template_selection else R.string.additional_settings
                )
            )

            AnimatedVisibility(visible = !customCommand) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    ButtonChip(
                        onClick = { showVideoFormatDialog = true },
                        enabled = !customCommand && !audio,
                        label = stringResource(R.string.video_format),
                        icon = Icons.Outlined.VideoFile
                    )
                    ButtonChip(
                        onClick = { showVideoQualityDialog = true },
                        enabled = !customCommand && !audio,
                        label = stringResource(R.string.video_quality),
                        icon = Icons.Outlined.HighQuality
                    )
                    ButtonChip(
                        onClick = { showAudioFormatEditDialog = true },
                        enabled = !customCommand && audio,
                        label = stringResource(R.string.convert_audio),
                        icon = Icons.Outlined.AudioFile
                    )
                }
            }
            AnimatedVisibility(visible = customCommand) {
                LazyRow {
                    itemsIndexed(templateList) { index, item ->
                        FilterChipWithIcons(
                            selected = index == selectedTemplateIndex,
                            onClick = {
                                selectedTemplateIndex = index
                                updatePreferences()
                            },
                            label = item.name
                        )
                    }
                }

            }
        }
    }
    if (!useDialog) {
        BottomDrawer(drawerState = drawerState, sheetContent = {
            Icon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                imageVector = Icons.Outlined.DoneAll,
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.settings_before_download),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            sheetContent()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {

                OutlinedButtonWithIcon(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = hide,
                    icon = Icons.Outlined.Cancel,
                    text = stringResource(R.string.cancel)
                )

                FilledButtonWithIcon(
                    onClick = downloadButtonCallback,
                    icon = Icons.Outlined.DownloadDone,
                    text = stringResource(R.string.start_download)
                )
            }
        })
    } else if (dialogState) {
        AlertDialog(onDismissRequest = hide, confirmButton = {
            TextButton(onClick = downloadButtonCallback) {
                Text(text = stringResource(R.string.start_download))
            }
        }, dismissButton = { DismissButton { hide() } }, icon = {
            Icon(
                imageVector = Icons.Outlined.DoneAll,
                contentDescription = null
            )
        }, title = { Text(stringResource(R.string.settings_before_download)) }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                sheetContent()
            }
        })
    }



    if (showAudioFormatEditDialog) {
        AudioFormatDialog(onDismissRequest = { showAudioFormatEditDialog = false })
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(onDismissRequest = { showVideoQualityDialog = false })
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(onDismissRequest = { showVideoFormatDialog = false })
    }
    when (showCustomCommandDialog) {
        (-1) -> CommandTemplateDialog(newTemplate = true,
            onDismissRequest = { showCustomCommandDialog = 0 })

        (1) -> CommandTemplateDialog(commandTemplate = templateList[selectedTemplateIndex],
            newTemplate = false,
            onDismissRequest = { showCustomCommandDialog = 0 })
    }
    if (showRenameDialog) {
        RenameDialog(onDismissRequest = { showRenameDialog = false })
    }
}