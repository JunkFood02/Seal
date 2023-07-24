package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined._4k
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.common.stringState
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.ui.component.OutlinedButtonChip
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SingleChoiceItem
import com.junkfood.seal.util.AUDIO_CONVERSION_FORMAT
import com.junkfood.seal.util.AUDIO_FORMAT
import com.junkfood.seal.util.AUDIO_QUALITY
import com.junkfood.seal.util.AV1
import com.junkfood.seal.util.CONVERT_M4A
import com.junkfood.seal.util.CONVERT_MP3
import com.junkfood.seal.util.CONVERT_SUBTITLE
import com.junkfood.seal.util.CONVERT_VTT
import com.junkfood.seal.util.DEFAULT
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.DownloadUtil.toFormatSorter
import com.junkfood.seal.util.LOW
import com.junkfood.seal.util.M4A
import com.junkfood.seal.util.NOT_CONVERT
import com.junkfood.seal.util.NOT_SPECIFIED
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.SORTING_FIELDS
import com.junkfood.seal.util.SUBTITLE_LANGUAGE
import com.junkfood.seal.util.ULTRA_LOW
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioQuickSettingsDialog(onDismissRequest: () -> Unit) {
    var audioQuality by AUDIO_QUALITY.intState
    var audioFormat by AUDIO_FORMAT.intState

    @Composable
    fun audioQualitySelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var audioQualityText by remember { mutableStateOf(PreferenceUtil.getAudioQualityDesc()) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = audioQualityText,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.HighQuality, null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                label = { Text(stringResource(id = R.string.audio_quality)) }
            )
            ExposedDropdownMenu(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                for (i in NOT_SPECIFIED..ULTRA_LOW)
                    DropdownMenuItem(
                        text = { Text(PreferenceUtil.getAudioQualityDesc(i)) },
                        onClick = {
                            audioQualityText =
                                PreferenceUtil.getAudioQualityDesc(i)
                            audioQuality = i
                            expanded = false
                        })
            }
        }
    }

    @Composable
    fun audioFormatSelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var audioFormatText by remember { mutableStateOf(PreferenceUtil.getAudioFormatDesc()) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = audioFormatText,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.AudioFile, null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                label = { Text(stringResource(id = R.string.audio_format_preference)) }
            )
            ExposedDropdownMenu(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                for (i in NOT_SPECIFIED..M4A)
                    DropdownMenuItem(
                        text = { Text(PreferenceUtil.getAudioFormatDesc(i)) },
                        onClick = {
                            audioFormatText =
                                PreferenceUtil.getAudioFormatDesc(i)
                            audioFormat = i
                            expanded = false
                        })
            }
        }
    }


    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.AudioFile, null) },
        title = {
            Text(
                text = stringResource(id = R.string.audio_format)
            )
        }, dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        }, confirmButton = {
            ConfirmButton {
                AUDIO_FORMAT.updateInt(audioFormat)
                AUDIO_QUALITY.updateInt(audioQuality)
                onDismissRequest()
            }
        }, text = {
            LazyColumn() {
                item {
                    audioFormatSelectField()
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    audioQualitySelectField()
                }
            }
        })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQuickSettingsDialog(onDismissRequest: () -> Unit) {
    var videoResolution by VIDEO_QUALITY.intState
    var videoFormat by VIDEO_FORMAT.intState

    @Composable
    fun videoResolutionSelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var videoResolutionText by remember { mutableStateOf(PreferenceUtil.getVideoResolutionDesc()) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = videoResolutionText,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined._4k, null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                label = { Text(stringResource(id = R.string.video_resolution)) }
            )
            ExposedDropdownMenu(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                for (i in 0..7)
                    DropdownMenuItem(
                        text = { Text(PreferenceUtil.getVideoResolutionDesc(i)) },
                        onClick = {
                            videoResolutionText =
                                PreferenceUtil.getVideoResolutionDesc(i)
                            videoResolution = i
                            expanded = false
                        })
            }
        }
    }

    @Composable
    fun videoFormatSelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var videoFormatText by remember { mutableStateOf(PreferenceUtil.getVideoFormatDesc()) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = videoFormatText,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.VideoFile, null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                label = { Text(stringResource(id = R.string.video_format_preference)) }
            )
            ExposedDropdownMenu(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                for (i in NOT_SPECIFIED..AV1)
                    DropdownMenuItem(
                        text = { Text(PreferenceUtil.getVideoFormatDesc(i)) },
                        onClick = {
                            videoFormatText =
                                PreferenceUtil.getVideoFormatDesc(i)
                            videoFormat = i
                            expanded = false
                        })
            }
        }
    }


    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.VideoFile, null) },
        title = {
            Text(
                text = stringResource(id = R.string.video_format)
            )
        }, dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        }, confirmButton = {
            ConfirmButton {
                VIDEO_FORMAT.updateInt(videoFormat)
                VIDEO_QUALITY.updateInt(videoResolution)
                onDismissRequest()
            }
        }, text = {
            LazyColumn() {
                item {
                    videoFormatSelectField()
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    videoResolutionSelectField()
                }
            }
        })
}


@Composable
fun AudioConversionDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var audioFormat by remember { mutableStateOf(PreferenceUtil.getAudioConvertFormat()) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        icon = { Icon(Icons.Outlined.Sync, null) },
        title = {
            Text(stringResource(R.string.convert_audio_format))
        }, confirmButton = {
            TextButton(onClick = {
                AUDIO_CONVERSION_FORMAT.updateInt(audioFormat)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .padding(horizontal = 24.dp),
                    text = stringResource(R.string.convert_audio_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in CONVERT_MP3..CONVERT_M4A)
                    SingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceUtil.getAudioConvertDesc(i),
                        selected = audioFormat == i
                    ) { audioFormat = i }
            }
        })
}


@Composable
fun VideoFormatDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var videoFormat by remember { mutableStateOf(PreferenceUtil.getVideoFormat()) }
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        }, icon = { Icon(Icons.Outlined.VideoFile, null) },
        title = {
            Text(stringResource(R.string.video_format_preference))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.encodeInt(VIDEO_FORMAT, videoFormat)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .padding(horizontal = 24.dp),
                    text = stringResource(R.string.preferred_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in 0..3)
                    SingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceUtil.getVideoFormatDesc(i),
                        selected = videoFormat == i
                    ) { videoFormat = i }
            }
        })
}

@Composable
fun AudioFormatDialog(onDismissRequest: () -> Unit) {
    var audioFormat by AUDIO_FORMAT.intState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        }, icon = { Icon(Icons.Outlined.AudioFile, null) },
        title = {
            Text(stringResource(R.string.audio_format_preference))
        }, confirmButton = {
            ConfirmButton {
                AUDIO_FORMAT.updateInt(audioFormat)
                onDismissRequest()
            }
        }, text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .padding(horizontal = 24.dp),
                    text = stringResource(R.string.preferred_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in DEFAULT..M4A)
                    SingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceUtil.getAudioFormatDesc(i),
                        selected = audioFormat == i
                    ) { audioFormat = i }
            }
        })
}

@Composable
fun AudioQualityDialog(onDismissRequest: () -> Unit) {
    var audioQuality by AUDIO_QUALITY.intState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            DismissButton { onDismissRequest() }
        }, icon = { Icon(Icons.Outlined.HighQuality, null) },
        title = {
            Text(stringResource(R.string.audio_quality))
        }, confirmButton = {
            ConfirmButton {
                AUDIO_QUALITY.updateInt(audioQuality)
                onDismissRequest()
            }
        }, text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .padding(horizontal = 24.dp),
                    text = stringResource(R.string.audio_quality_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in NOT_SPECIFIED..LOW)
                    SingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceUtil.getAudioQualityDesc(i),
                        selected = audioQuality == i
                    ) { audioQuality = i }
            }
        })
}

@Composable
fun FormatSortingDialog(onDismissRequest: () -> Unit) {
    var sortingFields by SORTING_FIELDS.stringState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            DismissButton { onDismissRequest() }
        }, icon = { Icon(Icons.Outlined.Sort, null) },
        title = {
            Text(stringResource(R.string.format_sorting))
        }, confirmButton = {
            ConfirmButton {
                SORTING_FIELDS.updateString(sortingFields)
                onDismissRequest()
            }
        }, text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp),
                    text = stringResource(R.string.format_sorting_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    value = sortingFields,
                    onValueChange = { sortingFields = it },
                    leadingIcon = { Text(text = "-S", fontFamily = FontFamily.Monospace) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                val uriHandler = LocalUriHandler.current
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp)
                ) {
                    OutlinedButtonChip(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        label = stringResource(id = R.string.import_from_preferences),
                        icon = Icons.Outlined.SettingsSuggest
                    ) {
                        sortingFields = DownloadUtil.DownloadPreferences().toFormatSorter()
                    }
                    OutlinedButtonChip(
                        label = stringResource(R.string.yt_dlp_docs),
                        icon = Icons.Outlined.OpenInNew
                    ) {
                        uriHandler.openUri(sortingFormats)
                    }
                }
            }
        })
}

@Composable
fun VideoQualityDialog(onDismissRequest: () -> Unit = {}, onConfirm: () -> Unit = {}) {
    var videoResolution by remember { mutableStateOf(PreferenceUtil.getVideoResolution()) }

    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        icon = { Icon(Icons.Outlined.HighQuality, null) },
        title = {
            Text(stringResource(R.string.video_quality))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.encodeInt(VIDEO_QUALITY, videoResolution)
                onConfirm()
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column() {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .padding(horizontal = 24.dp),
                    text = stringResource(R.string.video_quality_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                LazyColumn() {
//                    item { videoResolutionSelectField() }
                    for (i in 0..7) {
                        item {
                            SingleChoiceItem(
                                text = PreferenceUtil.getVideoResolutionDesc(i),
                                selected = videoResolution == i
                            ) {
                                videoResolution = i
                            }
                        }
                    }
                }
            }
        })
}

private const val subtitleOptions = "https://github.com/yt-dlp/yt-dlp#subtitle-options"
private const val sortingFormats = "https://github.com/yt-dlp/yt-dlp#sorting-formats"

@Composable
fun SubtitleLanguageDialog(onDismissRequest: () -> Unit) {
    var languages by SUBTITLE_LANGUAGE.stringState
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.subtitle_language)) },
        icon = { Icon(Icons.Outlined.Language, null) },
        text = {
            Column() {
                OutlinedTextField(
                    modifier = Modifier.padding(bottom = 8.dp),
                    value = languages,
                    onValueChange = { languages = it },
                    label = {
                        Text(stringResource(id = R.string.subtitle_language))
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                LinkButton(link = subtitleOptions)
            }
        }, confirmButton = {
            ConfirmButton() {
                SUBTITLE_LANGUAGE.updateString(languages)
                onDismissRequest()
            }
        }, dismissButton = {
            DismissButton() {
                onDismissRequest()
            }
        })
}

@Composable
fun SubtitleConversionDialog(onDismissRequest: () -> Unit) {
    var currentFormat by CONVERT_SUBTITLE.intState
    SealDialog(onDismissRequest = onDismissRequest, confirmButton = {
        ConfirmButton {
            CONVERT_SUBTITLE.updateInt(currentFormat)
            onDismissRequest()
        }
    }, dismissButton = {
        DismissButton { onDismissRequest() }
    }, title = { Text(text = stringResource(id = R.string.convert_subtitle)) },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Sync,
                contentDescription = null
            )
        }, text = {
            LazyColumn {
                item {
                    Text(
                        text = stringResource(id = R.string.convert_subtitle_desc),
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                for (format in NOT_CONVERT..CONVERT_VTT) {
                    item {
                        SingleChoiceItem(
                            text = PreferenceUtil.getSubtitleConversionFormat(format),
                            selected = currentFormat == format
                        ) {
                            currentFormat = format
                        }
                    }
                }
            }
        })
}
