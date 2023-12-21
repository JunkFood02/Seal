package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArtTrack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.util.AUDIO_CONVERT
import com.junkfood.seal.util.CROP_ARTWORK
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.DownloadUtil.toFormatSorter
import com.junkfood.seal.util.EMBED_METADATA
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.FORMAT_SORTING
import com.junkfood.seal.util.PreferenceStrings
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.SORTING_FIELDS
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.VIDEO_CLIP
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadFormatPreferences(onBackPressed: () -> Unit, navigateToSubtitlePage: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState(),
            canScroll = { true })

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
    var embedMetadata by EMBED_METADATA.booleanState

    var showAudioFormatDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showAudioConvertDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }
    var showFormatSorterDialog by remember { mutableStateOf(false) }
    var showVideoClipDialog by remember { mutableStateOf(false) }

    var videoFormat by VIDEO_FORMAT.intState
    var videoQuality by VIDEO_QUALITY.intState
    var convertFormat by remember { mutableStateOf(PreferenceStrings.getAudioConvertDesc()) }
    var sortingFields by remember(showFormatSorterDialog) { mutableStateOf(SORTING_FIELDS.getString()) }
    val audioFormat by remember(showAudioFormatDialog) { mutableStateOf(PreferenceStrings.getAudioFormatDesc()) }
    var convertAudio by AUDIO_CONVERT.booleanState
    var isFormatSortingEnabled by FORMAT_SORTING.booleanState
    val audioQuality by remember(showAudioQualityDialog) { mutableStateOf(PreferenceStrings.getAudioQualityDesc()) }
    var isVideoClipEnabled by VIDEO_CLIP.booleanState
    var isFormatSelectionEnabled by FORMAT_SELECTION.booleanState

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
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
        },
        content = {
            val isCustomCommandEnabled by remember {
                mutableStateOf(
                    PreferenceUtil.getValue(CUSTOM_COMMAND)
                )
            }
            LazyColumn(Modifier.padding(it)) {
                if (isCustomCommandEnabled) item {
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
                    PreferenceItem(title = stringResource(id = R.string.audio_format_preference),
                        description = audioFormat,
                        icon = Icons.Outlined.AudioFile,
                        enabled = !isCustomCommandEnabled && !isFormatSortingEnabled,
                        onClick = { showAudioFormatDialog = true })
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_quality),
                        description = audioQuality,
                        icon = Icons.Outlined.HighQuality,
                        onClick = { showAudioQualityDialog = true },
                        enabled = !isCustomCommandEnabled && !isFormatSortingEnabled
                    )
                }
                item {
                    PreferenceSwitchWithDivider(title = stringResource(R.string.convert_audio_format),
                        description = convertFormat,
                        icon = Icons.Outlined.Sync,
                        enabled = audioSwitch && !isCustomCommandEnabled,
                        onClick = { showAudioConvertDialog = true },
                        isChecked = convertAudio,
                        onChecked = {
                            convertAudio = !convertAudio
                            AUDIO_CONVERT.updateBoolean(convertAudio)
                        })
                }
                item {
                    PreferenceSwitch(title = stringResource(id = R.string.embed_metadata),
                        description = stringResource(
                            id = R.string.embed_metadata_desc
                        ),
                        enabled = audioSwitch && !isCustomCommandEnabled,
                        isChecked = embedMetadata,
                        icon = Icons.Outlined.ArtTrack,
                        onClick = {
                            embedMetadata = !embedMetadata
                            EMBED_METADATA.updateBoolean(embedMetadata)
                        })
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.crop_artwork),
                        description = stringResource(R.string.crop_artwork_desc),
                        icon = Icons.Outlined.Crop,
                        enabled = embedMetadata && audioSwitch && !isCustomCommandEnabled,
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
                        description = PreferenceStrings.getVideoFormatLabel(videoFormat),
                        icon = Icons.Outlined.VideoFile,
                        enabled = !audioSwitch && !isCustomCommandEnabled && !isFormatSortingEnabled
                    ) { showVideoFormatDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_quality),
                        description = PreferenceStrings.getVideoResolutionDescComp(videoQuality),
                        icon = Icons.Outlined.HighQuality,
                        enabled = !audioSwitch && !isCustomCommandEnabled && !isFormatSortingEnabled
                    ) { showVideoQualityDialog = true }
                }/*                item {
                                    var embedThumbnail by EMBED_THUMBNAIL.booleanState

                                    PreferenceSwitch(
                                        title = stringResource(id = R.string.embed_thumbnail),
                                        description = stringResource(id = R.string.embed_thumbnail_desc),
                                        icon = Icons.Outlined.Photo,
                                        isChecked = embedThumbnail,
                                        enabled = !isCustomCommandEnabled && !audioSwitch
                                    ) {
                                        embedThumbnail = !embedThumbnail
                                        EMBED_THUMBNAIL.updateBoolean(embedThumbnail)
                                    }
                                }*/

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.advanced_settings))
                }
                item {

                    PreferenceItem(
                        title = stringResource(id = R.string.subtitle),
                        icon = Icons.Outlined.Subtitles,
                        enabled = !isCustomCommandEnabled,
                        description = stringResource(id = R.string.subtitle_desc),
                    ) { navigateToSubtitlePage() }
                }
                item {
                    PreferenceSwitchWithDivider(title = stringResource(id = R.string.format_sorting),
                        icon = Icons.Outlined.Sort,
                        description = stringResource(id = R.string.format_sorting_desc),
                        enabled = !isCustomCommandEnabled,
                        isChecked = isFormatSortingEnabled,
                        onChecked = {
                            isFormatSortingEnabled = !isFormatSortingEnabled
                            FORMAT_SORTING.updateBoolean(isFormatSortingEnabled)
                        },
                        onClick = { showFormatSorterDialog = true })
                }
                item {
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
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.clip_video),
                        description = stringResource(
                            id = R.string.clip_video_desc
                        ),
                        icon = Icons.Outlined.ContentCut,
                        isChecked = isVideoClipEnabled,
                        enabled = !isCustomCommandEnabled && isFormatSelectionEnabled
                    ) {
                        if (!isVideoClipEnabled) showVideoClipDialog = true
                        else {
                            isVideoClipEnabled = false
                            VIDEO_CLIP.updateBoolean(false)
                        }
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
            convertFormat = PreferenceStrings.getAudioConvertDesc()
        }
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(videoQuality = videoQuality,
            onDismissRequest = { showVideoQualityDialog = false }) {
            videoQuality = it
            VIDEO_QUALITY.updateInt(it)
        }
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(videoFormatPreference = videoFormat, onDismissRequest = {
            showVideoFormatDialog = false
        }) {
            PreferenceUtil.encodeInt(VIDEO_FORMAT, it)
            videoFormat = it
        }
    }
    if (showFormatSorterDialog) {
        FormatSortingDialog(
            fields = sortingFields,
            onImport = {
                sortingFields = DownloadUtil.DownloadPreferences().toFormatSorter()
            },
            onDismissRequest = { showFormatSorterDialog = false },
            showSwitch = false, onConfirm = {
                sortingFields = it
                SORTING_FIELDS.updateString(sortingFields)
            }
        )
    }
    if (showVideoClipDialog) {
        AlertDialog(onDismissRequest = { showVideoClipDialog = false },
            icon = { Icon(Icons.Outlined.ContentCut, null) },
            confirmButton = {
                ConfirmButton {
                    isVideoClipEnabled = true
                    VIDEO_CLIP.updateBoolean(true)
                    showVideoClipDialog = false
                }
            },
            dismissButton = {
                DismissButton {
                    showVideoClipDialog = false
                }
            },
            text = {
                Text(stringResource(id = R.string.clip_video_dialog_msg))
            },
            title = {
                Text(
                    stringResource(id = R.string.enable_experimental_feature),
                    textAlign = TextAlign.Center
                )
            })
    }
}