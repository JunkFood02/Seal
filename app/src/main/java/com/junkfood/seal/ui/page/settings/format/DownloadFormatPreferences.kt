package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.AUTO_SUBTITLE
import com.junkfood.seal.util.PreferenceUtil.CROP_ARTWORK
import com.junkfood.seal.util.PreferenceUtil.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.FORMAT_SELECTION

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadFormatPreferences(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    var audioSwitch by remember {
        mutableStateOf(PreferenceUtil.getValue(EXTRACT_AUDIO))
    }
    var isArtworkCroppingEnabled by remember {
        mutableStateOf(PreferenceUtil.getValue(CROP_ARTWORK))
    }
    var embedSubtitle by remember {
        mutableStateOf(
            PreferenceUtil.getValue(PreferenceUtil.SUBTITLE)
        )
    }

    var videoFormat by remember { mutableStateOf(PreferenceUtil.getVideoFormatDesc()) }
    var videoResolution by remember { mutableStateOf(PreferenceUtil.getVideoResolutionDesc()) }
    var audioFormat by remember { mutableStateOf(PreferenceUtil.getAudioFormatDesc()) }

    var showAudioFormatEditDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.format),
                    )
                }, navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            val isCustomCommandEnabled by remember {
                mutableStateOf(
                    PreferenceUtil.getValue(PreferenceUtil.CUSTOM_COMMAND)
                )
            }
            LazyColumn(Modifier.padding(it)) {
                if (isCustomCommandEnabled)
                    item {
                        PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
                    }
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.audio))
                }
                item {
                    PreferenceSwitch(title = stringResource(id = R.string.extract_audio),
                        description = stringResource(
                            id = R.string.extract_audio_summary
                        ),
                        icon = Icons.Outlined.MusicNote,
                        isChecked = audioSwitch,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            audioSwitch = !audioSwitch
                            PreferenceUtil.updateValue(EXTRACT_AUDIO, audioSwitch)
                        })
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.audio_format),
                        description = audioFormat,
                        icon = Icons.Outlined.AudioFile,
                        enabled = audioSwitch && !isCustomCommandEnabled
                    ) { showAudioFormatEditDialog = true }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.crop_artwork),
                        description = stringResource(R.string.crop_artwork_desc),
                        icon = Icons.Outlined.Crop,
                        enabled = audioSwitch && !isCustomCommandEnabled,
                        isChecked = isArtworkCroppingEnabled
                    ) {
                        isArtworkCroppingEnabled = !isArtworkCroppingEnabled
                        PreferenceUtil.updateValue(CROP_ARTWORK, isArtworkCroppingEnabled)
                    }
                }
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.video))
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.video_format_preference),
                        description = videoFormat,
                        icon = Icons.Outlined.VideoFile,
                        enabled = !audioSwitch && !isCustomCommandEnabled
                    ) { showVideoFormatDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_quality),
                        description = videoResolution,
                        icon = Icons.Outlined.HighQuality,
                        enabled = !audioSwitch && !isCustomCommandEnabled
                    ) { showVideoQualityDialog = true }
                }

                item {

                    PreferenceSwitch(
                        title = stringResource(id = R.string.embed_subtitles),
                        icon = Icons.Outlined.Subtitles,
                        enabled = !audioSwitch && !isCustomCommandEnabled,
                        description = stringResource(id = R.string.embed_subtitles_desc),
                        isChecked = embedSubtitle
                    ) {
                        embedSubtitle = !embedSubtitle
                        PreferenceUtil.updateValue(PreferenceUtil.SUBTITLE, embedSubtitle)
                    }
                }
                item {
                    var autoSubtitle by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(
                                AUTO_SUBTITLE
                            )
                        )
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.auto_subtitle),
                        icon = Icons.Outlined.ClosedCaption,
                        enabled = !isCustomCommandEnabled && embedSubtitle && !audioSwitch,
                        description = stringResource(R.string.auto_subtitle_desc),
                        isChecked = autoSubtitle
                    ) {
                        autoSubtitle = !autoSubtitle
                        PreferenceUtil.updateValue(AUTO_SUBTITLE, autoSubtitle)
                    }
                }
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.advanced_settings))
                }

                item {
                    var isFormatSelectionEnabled by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(
                                FORMAT_SELECTION, true
                            )
                        )
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.format_selection),
                        icon = Icons.Outlined.VideoSettings,
                        enabled = !isCustomCommandEnabled,
                        description = stringResource(id = R.string.format_selection_desc),
                        isChecked = isFormatSelectionEnabled
                    ) {
                        isFormatSelectionEnabled = !isFormatSelectionEnabled
                        PreferenceUtil.updateValue(FORMAT_SELECTION, isFormatSelectionEnabled)
                    }

                }
            }
        })
    if (showAudioFormatEditDialog) {
        AudioFormatDialog(onDismissRequest = { showAudioFormatEditDialog = false }) {
            audioFormat = PreferenceUtil.getAudioFormatDesc()
        }
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(onDismissRequest = { showVideoQualityDialog = false }) {
            videoResolution = PreferenceUtil.getVideoResolutionDesc()
        }
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(onDismissRequest = {
            showVideoFormatDialog = false
        }) { videoFormat = PreferenceUtil.getVideoFormatDesc() }
    }
}