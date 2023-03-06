package com.junkfood.seal.ui.page.download

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BottomDrawer
import com.junkfood.seal.ui.component.ButtonChip
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.FilterChipWithIcons
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.settings.command.CommandTemplateDialog
import com.junkfood.seal.ui.page.settings.format.AudioQuickSettingsDialog
import com.junkfood.seal.ui.page.settings.format.SubtitleLanguageDialog
import com.junkfood.seal.ui.page.settings.format.VideoQuickSettingsDialog
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.templateStateFlow
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.TEMPLATE_ID
import com.junkfood.seal.util.THUMBNAIL
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterialApi::class,
)
@Composable
fun DownloadSettingDialog(
    useDialog: Boolean = false,
    dialogState: Boolean = false,
    isShareActivity: Boolean = false,
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

    var showAudioSettingsDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoSettingsDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showCustomCommandDialog by remember { mutableStateOf(0) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var selectedTemplateId by TEMPLATE_ID.intState

    val templateList by templateStateFlow.collectAsStateWithLifecycle(ArrayList())
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(templateList.size, customCommand) {
        if (customCommand) {
            templateList.indexOfFirst { it.id == selectedTemplateId }
                .run { if (!equals(-1)) scrollState.scrollToItem(this) }
        }
    }
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            DrawerSheetSubtitle(text = stringResource(id = R.string.general_settings))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                VideoFilterChip(
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
                )
            }
            DrawerSheetSubtitle(text = stringResource(id = R.string.advanced_settings))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                VideoFilterChip(
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
                        onClick = {
                            showVideoSettingsDialog = true
                        },
                        enabled = !customCommand && !audio,
                        label = stringResource(R.string.video_format),
                        icon = Icons.Outlined.VideoFile
                    )
                    ButtonChip(
                        onClick = {
                            showAudioSettingsDialog = true
                        },
                        enabled = !customCommand,
                        label = stringResource(R.string.audio_format),
                        icon = Icons.Outlined.AudioFile
                    )
                    ButtonChip(
                        onClick = { showSubtitleDialog = true },
                        label = stringResource(id = R.string.subtitle_language),
                        icon = Icons.Outlined.Language,
                        enabled = !customCommand && !audio && subtitle
                    )
                }
            }
            AnimatedVisibility(visible = customCommand) {
                LazyRow(state = scrollState, modifier = Modifier.selectableGroup()) {
                    items(templateList) { item ->
                        FilterChipWithIcons(
                            selected = item.id == selectedTemplateId, onClick = {
                                selectedTemplateId = item.id
                                updatePreferences()
                            }, label = item.name
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            sheetContent()
            val state = rememberLazyListState()
            LaunchedEffect(drawerState.isVisible) {
                state.scrollToItem(1)
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End,
                state = state
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
    if (showVideoSettingsDialog) {
        VideoQuickSettingsDialog(onDismissRequest = { showVideoSettingsDialog = false })
    }
    when (showCustomCommandDialog) {
        (-1) -> CommandTemplateDialog(onDismissRequest = { showCustomCommandDialog = 0 },
            confirmationCallback = {
                scope.launch {
                    selectedTemplateId = it
                    PreferenceUtil.encodeInt(TEMPLATE_ID, it)
                    templateList.indexOfFirst { it.id == selectedTemplateId }
                        .run { if (!equals(-1)) scrollState.scrollToItem(this) }
                }
            })

        (1) -> CommandTemplateDialog(commandTemplate = templateList.find { it.id == selectedTemplateId }
            ?: CommandTemplate(0, "", ""), onDismissRequest = { showCustomCommandDialog = 0 })
    }
    if (showSubtitleDialog) SubtitleLanguageDialog { showSubtitleDialog = false }
}