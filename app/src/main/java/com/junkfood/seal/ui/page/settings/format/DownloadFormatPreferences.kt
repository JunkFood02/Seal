package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.VideoFile
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
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.page.settings.general.AudioFormatDialog
import com.junkfood.seal.ui.page.settings.general.VideoFormatDialog
import com.junkfood.seal.ui.page.settings.general.VideoQualityDialog
import com.junkfood.seal.util.PreferenceUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadFormatPreferences(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    var audioSwitch by remember {
        mutableStateOf(
            PreferenceUtil.getValue(PreferenceUtil.EXTRACT_AUDIO)
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
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.format),
                    )
                }, navigationIcon = {
                    BackButton(modifier = Modifier.padding(start = 8.dp)) {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(Modifier.padding(it)) {
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
                        onClick = {
                            audioSwitch = !audioSwitch
                            PreferenceUtil.updateValue(PreferenceUtil.EXTRACT_AUDIO, audioSwitch)
                        })
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.audio_format),
                        description = audioFormat,
                        icon = Icons.Outlined.AudioFile,
                        enabled = audioSwitch
                    ) { showAudioFormatEditDialog = true }
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.video))
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.video_format_preference),
                        description = videoFormat,
                        icon = Icons.Outlined.VideoFile,
                        enabled = !audioSwitch
                    ) { showVideoFormatDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_quality),
                        description = videoResolution,
                        icon = Icons.Outlined.HighQuality,
                        enabled = !audioSwitch
                    ) { showVideoQualityDialog = true }
                }

                item {
                    var embedSubtitle by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(
                                PreferenceUtil.SUBTITLE
                            )
                        )
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.embed_subtitles),
                        icon = Icons.Outlined.Subtitles,
                        enabled = !audioSwitch,
                        description = stringResource(id = R.string.embed_subtitles_desc),
                        isChecked = embedSubtitle
                    ) {
                        embedSubtitle = !embedSubtitle
                        PreferenceUtil.updateValue(PreferenceUtil.SUBTITLE, embedSubtitle)
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