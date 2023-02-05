package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Sync
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
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.util.AUDIO_CONVERT
import com.junkfood.seal.util.CROP_ARTWORK
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.FORMAT_SORTING
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.SORTING_FIELDS
import com.junkfood.seal.util.SUBTITLE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadFormatPreferences(onBackPressed: () -> Unit, navigateToSubtitlePage: () -> Unit) {
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
            PreferenceUtil.getValue(SUBTITLE)
        )
    }

    var showAudioFormatDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showAudioConvertDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }
    var showFormatSorterDialog by remember { mutableStateOf(false) }

    var videoFormat by remember { mutableStateOf(PreferenceUtil.getVideoFormatDesc()) }
    var videoResolution by remember { mutableStateOf(PreferenceUtil.getVideoResolutionDesc()) }
    var convertFormat by remember { mutableStateOf(PreferenceUtil.getAudioConvertDesc()) }
    val sortingFields by remember { mutableStateOf(SORTING_FIELDS.getString()) }
    val audioFormat by remember(showAudioFormatDialog) { mutableStateOf(PreferenceUtil.getAudioFormatDesc()) }
    var convertAudio by AUDIO_CONVERT.booleanState
    var formatSorting by FORMAT_SORTING.booleanState
    val audioQuality by remember(showAudioQualityDialog) { mutableStateOf(PreferenceUtil.getAudioQualityDesc()) }

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
                    PreferenceUtil.getValue(CUSTOM_COMMAND)
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
                        title = stringResource(id = R.string.audio_format_preference),
                        description = audioFormat,
                        icon = Icons.Outlined.AudioFile,
                        enabled = !isCustomCommandEnabled,
                        onClick = { showAudioFormatDialog = true }
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_quality),
                        description = audioQuality,
                        icon = Icons.Outlined.HighQuality,
                        onClick = { showAudioQualityDialog = true },
                        enabled = !isCustomCommandEnabled
                    )
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.convert_audio_format),
                        description = convertFormat,
                        icon = Icons.Outlined.Sync,
                        enabled = audioSwitch && !isCustomCommandEnabled,
                        onClick = { showAudioConvertDialog = true },
                        isChecked = convertAudio,
                        onChecked = {
                            convertAudio = !convertAudio
                            AUDIO_CONVERT.updateBoolean(convertAudio)
                        }
                    )
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

                    PreferenceItem(
                        title = stringResource(id = R.string.subtitle),
                        icon = Icons.Outlined.Subtitles,
                        enabled = !audioSwitch && !isCustomCommandEnabled,
                        description = stringResource(id = R.string.subtitle_desc),
                    ) { navigateToSubtitlePage() }
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.advanced_settings))
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(id = R.string.format_sorting),
                        icon = Icons.Outlined.Sort,
                        description = stringResource(id = R.string.format_sorting_desc),
                        enabled = !isCustomCommandEnabled,
                        isChecked = formatSorting,
                        onChecked = {
                            formatSorting = !formatSorting
                            FORMAT_SORTING.updateBoolean(formatSorting)
                        }, onClick = { showFormatSorterDialog = true }
                    )
                }
                item {
                    var isFormatSelectionEnabled by FORMAT_SELECTION.booleanState
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
    if (showAudioFormatDialog) {
        AudioFormatDialog { showAudioFormatDialog = false }
    }
    if (showAudioQualityDialog) {
        AudioQualityDialog { showAudioQualityDialog = false }
    }
    if (showAudioConvertDialog) {
        AudioConversionDialog(onDismissRequest = { showAudioConvertDialog = false }) {
            convertFormat = PreferenceUtil.getAudioConvertDesc()
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
    if (showFormatSorterDialog) {
        FormatSortingDialog { showFormatSorterDialog = false }
    }
}