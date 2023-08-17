package com.junkfood.seal.ui.page.download

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BottomDrawer
import com.junkfood.seal.ui.component.ButtonChip
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.OutlinedButtonChip
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SegmentedButtonValues
import com.junkfood.seal.ui.component.SingleChoiceChip
import com.junkfood.seal.ui.component.SingleChoiceSegmentedButton
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.command.TemplatePickerDialog
import com.junkfood.seal.ui.page.settings.command.CommandTemplateDialog
import com.junkfood.seal.ui.page.settings.format.AudioQuickSettingsDialog
import com.junkfood.seal.ui.page.settings.format.VideoFormatDialog
import com.junkfood.seal.ui.page.settings.format.VideoQualityDialog
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PreferenceStrings
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.TEMPLATE_ID
import com.junkfood.seal.util.THUMBNAIL
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
)
@Composable
fun DownloadSettingDialog(
    useDialog: Boolean = false,
    dialogState: Boolean = false,
    isQuickDownload: Boolean = false,
    drawerState: ModalBottomSheetState,
    confirm: () -> Unit,
    hide: () -> Unit
) {
    var audio by remember { mutableStateOf(PreferenceUtil.getValue(EXTRACT_AUDIO)) }
    var thumbnail by remember { mutableStateOf(PreferenceUtil.getValue(THUMBNAIL)) }
    var customCommand by remember { mutableStateOf(PreferenceUtil.getValue(CUSTOM_COMMAND)) }
    var playlist by remember { mutableStateOf(PreferenceUtil.getValue(PLAYLIST)) }
    var subtitle by remember { mutableStateOf(PreferenceUtil.getValue(SUBTITLE)) }
    var formatSelection by FORMAT_SELECTION.booleanState
    var videoFormatPreference by VIDEO_FORMAT.intState
    var videoQuality by VIDEO_QUALITY.intState

    var showAudioSettingsDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var selectedTemplateId by TEMPLATE_ID.intState

    var showTemplateSelectionDialog by remember { mutableStateOf(false) }
    var showTemplateCreatorDialog by remember { mutableStateOf(false) }
    var showTemplateEditorDialog by remember { mutableStateOf(false) }

    val template by remember(
        showTemplateCreatorDialog,
        showTemplateSelectionDialog,
        showTemplateEditorDialog
    ) {
        mutableStateOf(PreferenceUtil.getTemplate())
    }

    val scope = rememberCoroutineScope()

    val updatePreferences = {
        scope.launch {
            PreferenceUtil.updateValue(EXTRACT_AUDIO, audio)
            PreferenceUtil.updateValue(THUMBNAIL, thumbnail)
            PreferenceUtil.updateValue(CUSTOM_COMMAND, customCommand)
            PreferenceUtil.updateValue(PLAYLIST, playlist)
            PreferenceUtil.updateValue(SUBTITLE, subtitle)
            PreferenceUtil.encodeInt(TEMPLATE_ID, selectedTemplateId)
        }
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
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { }
            )
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())

            ) {
                val audioSelected by remember { derivedStateOf { audio && !customCommand } }
                val videoSelected by remember { derivedStateOf { !audio && !customCommand } }
                val commandSelected by remember { derivedStateOf { customCommand } }

                SingleChoiceSegmentedButtonRow {
                    SingleChoiceSegmentedButton(
                        text = stringResource(id = R.string.audio),
                        selected = audioSelected,
                        position = SegmentedButtonValues.START
                    ) {
                        audio = true
                        customCommand = false
                        updatePreferences()
                    }
                    SingleChoiceSegmentedButton(
                        text = stringResource(id = R.string.video),
                        selected = videoSelected
                    ) {
                        audio = false
                        customCommand = false
                        updatePreferences()
                    }
                    SingleChoiceSegmentedButton(
                        text = stringResource(id = R.string.commands),
                        selected = commandSelected,
                        position = SegmentedButtonValues.END
                    ) {
                        customCommand = true
                        updatePreferences()
                    }
                }
                /*VideoFilterChip(
                    selected = audio, enabled = !customCommand, onClick = {
                        audio = !audio
                        updatePreferences()
                    }, label = stringResource(R.string.extract_audio)
                )
                if (!isShareActivity) {
                    VideoFilterChip(
                        selected = playlist, enabled = !customCommand, onClick = {
                            playlist = !playlist
                            updatePreferences()
                        }, label = stringResource(R.string.download_playlist)
                    )
                    VideoFilterChip(
                        selected = formatSelection,
                        enabled = !customCommand && !playlist,
                        onClick = {
                            formatSelection = !formatSelection
                            FORMAT_SELECTION.updateBoolean(formatSelection)
                        },
                        label = stringResource(R.string.format_selection)
                    )
                }
                VideoFilterChip(
                    selected = subtitle, enabled = !customCommand && !audio, onClick = {
                        subtitle = !subtitle
                        updatePreferences()
                    }, label = stringResource(id = R.string.download_subtitles)
                )
                VideoFilterChip(
                    selected = thumbnail, enabled = !customCommand, onClick = {
                        thumbnail = !thumbnail
                        updatePreferences()
                    }, label = stringResource(R.string.create_thumbnail)
                )*/
            }
            if (!isQuickDownload) {
                DrawerSheetSubtitle(text = stringResource(id = R.string.format_selection))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    SingleChoiceChip(
                        selected = !formatSelection, onClick = {
                            formatSelection = false
                            updatePreferences()
                        }, enabled = !customCommand, label = stringResource(id = R.string.auto)
                    )
                    SingleChoiceChip(
                        selected = formatSelection,
                        onClick = {
                            formatSelection = true
                            updatePreferences()
                        },
                        enabled = !customCommand && !playlist,
                        label = stringResource(id = R.string.custom)
                    )
                }
            }

            DrawerSheetSubtitle(text = stringResource(id = R.string.format_preference))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ButtonChip(
                    onClick = {
                        showVideoFormatDialog = true
                    },
                    enabled = !customCommand && !audio,
                    label = PreferenceStrings.getVideoFormatLabel(videoFormatPreference),
                    icon = Icons.Outlined.VideoFile,
                    iconDescription = stringResource(id = R.string.video_format_preference)
                )
                ButtonChip(
                    label = PreferenceStrings.getVideoResolutionDescComp(),
                    icon = Icons.Outlined.HighQuality,
                    enabled = !customCommand && !audio,
                    iconDescription = stringResource(id = R.string.video_quality)
                ) {
                    showVideoQualityDialog = true
                }
                ButtonChip(
                    onClick = {
                        showAudioSettingsDialog = true
                    },
                    enabled = !customCommand,
                    label = stringResource(R.string.audio_format),
                    icon = Icons.Outlined.AudioFile
                )
                /*                VideoFilterChip(
                                    selected = customCommand, onClick = {
                                        customCommand = !customCommand
                                        updatePreferences()
                                    }, label = stringResource(R.string.custom_command)
                                )
                                ButtonChip(
                                    onClick = { showCustomCommandDialog = -1 }, label = stringResource(
                                        R.string.new_template
                                    ), icon = Icons.Outlined.Add, enabled = customCommand
                                )
                                ButtonChip(
                                    onClick = { showCustomCommandDialog = 1 }, label = stringResource(
                                        R.string.edit
                                    ), icon = Icons.Outlined.EditNote, enabled = customCommand
                                )*/
            }

            DrawerSheetSubtitle(
                text = stringResource(
                    if (customCommand) R.string.template_selection else R.string.additional_settings
                )
            )

            AnimatedVisibility(visible = !customCommand) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    if (!isQuickDownload) {
                        VideoFilterChip(
                            selected = playlist, enabled = !customCommand, onClick = {
                                playlist = !playlist
                                formatSelection = false
                                updatePreferences()
                            }, label = stringResource(R.string.download_playlist)
                        )
                    }
                    if (!audio)
                        VideoFilterChip(
                            selected = subtitle, enabled = !customCommand, onClick = {
                                subtitle = !subtitle
                                updatePreferences()
                            }, label = stringResource(id = R.string.download_subtitles)
                        )
                    VideoFilterChip(
                        selected = thumbnail, enabled = !customCommand, onClick = {
                            thumbnail = !thumbnail
                            updatePreferences()
                        }, label = stringResource(R.string.create_thumbnail)
                    )
                }
            }
            AnimatedVisibility(visible = customCommand) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedButtonChip(
                            icon = Icons.Outlined.Code,
                            label = template.name,
                            onClick = { showTemplateSelectionDialog = true }
                        )
                    }
                    item {
                        OutlinedButtonChip(
                            icon = Icons.Outlined.NewLabel,
                            label = stringResource(id = R.string.new_template),
                            onClick = { showTemplateCreatorDialog = true }
                        )
                    }
                    item {
                        OutlinedButtonChip(
                            icon = Icons.Outlined.Edit,
                            label = stringResource(id = R.string.edit_template, template.name),
                            onClick = { showTemplateEditorDialog = true }
                        )
                    }
                }

            }
        }
    }
    if (!useDialog) {
        BottomDrawer(
            drawerState = drawerState,
            horizontalPadding = PaddingValues(horizontal = 20.dp),
            sheetContent = {
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                sheetContent()
                val state = rememberLazyListState()
                LaunchedEffect(drawerState.isVisible) {
                    state.scrollToItem(0)
                }
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    state = state,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        OutlinedButtonWithIcon(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            onClick = hide,
                            icon = Icons.Outlined.Cancel,
                            text = stringResource(R.string.cancel)
                        )
                    }
                    item {
                        FilledButtonWithIcon(
                            onClick = downloadButtonCallback,
                            icon = Icons.Outlined.DownloadDone,
                            text = stringResource(R.string.start_download)
                        )
                    }
                }
            })
    } else if (dialogState) {
        AlertDialog(onDismissRequest = hide, confirmButton = {
            TextButton(onClick = downloadButtonCallback) {
                Text(text = stringResource(R.string.start_download))
            }
        }, dismissButton = { DismissButton { hide() } }, icon = {
            Icon(
                imageVector = Icons.Outlined.DoneAll, contentDescription = null
            )
        }, title = {
            Text(
                stringResource(R.string.settings_before_download), textAlign = TextAlign.Center
            )
        }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                sheetContent()
            }
        })
    }



    if (showAudioSettingsDialog) {
        AudioQuickSettingsDialog(onDismissRequest = { showAudioSettingsDialog = false })
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(videoFormatPreference = videoFormatPreference,
            onDismissRequest = { showVideoFormatDialog = false },
            onConfirm = {
                videoFormatPreference = it
                VIDEO_FORMAT.updateInt(it)
            })
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(videoQuality = videoQuality,
            onDismissRequest = { showVideoQualityDialog = false },
            onConfirm = {
                VIDEO_QUALITY.updateInt(it)
                videoQuality = it
            })
    }


    if (showTemplateSelectionDialog) {
        TemplatePickerDialog() { showTemplateSelectionDialog = false }
    }
    if (showTemplateCreatorDialog) {
        CommandTemplateDialog(
            onDismissRequest = { showTemplateCreatorDialog = false },
            confirmationCallback = {
                scope.launch {
                    TEMPLATE_ID.updateInt(it)
                }
            })
    }
    if (showTemplateEditorDialog) {
        CommandTemplateDialog(
            commandTemplate = template,
            onDismissRequest = { showTemplateEditorDialog = false }
        )
    }
}