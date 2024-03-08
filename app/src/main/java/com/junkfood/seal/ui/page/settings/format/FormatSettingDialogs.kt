package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material.icons.outlined._4k
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.common.stringState
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DialogSingleChoiceItem
import com.junkfood.seal.ui.component.DialogSingleChoiceItemWithLabel
import com.junkfood.seal.ui.component.DialogSwitchItem
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.OutlinedButtonChip
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.util.AUDIO_CONVERSION_FORMAT
import com.junkfood.seal.util.AUDIO_CONVERT
import com.junkfood.seal.util.AUDIO_FORMAT
import com.junkfood.seal.util.AUDIO_QUALITY
import com.junkfood.seal.util.CONVERT_M4A
import com.junkfood.seal.util.CONVERT_MP3
import com.junkfood.seal.util.CONVERT_SUBTITLE
import com.junkfood.seal.util.CONVERT_VTT
import com.junkfood.seal.util.DEFAULT
import com.junkfood.seal.util.FORMAT_COMPATIBILITY
import com.junkfood.seal.util.FORMAT_QUALITY
import com.junkfood.seal.util.M4A
import com.junkfood.seal.util.NOT_CONVERT
import com.junkfood.seal.util.NOT_SPECIFIED
import com.junkfood.seal.util.PreferenceStrings
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.SUBTITLE_LANGUAGE
import com.junkfood.seal.util.ULTRA_LOW
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY
import com.junkfood.seal.util.getStringDefault


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioQuickSettingsDialog(onDismissRequest: () -> Unit) {
    var audioQuality by AUDIO_QUALITY.intState
    var audioFormat by AUDIO_FORMAT.intState

    @Composable
    fun audioQualitySelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var audioQualityText by remember { mutableStateOf(PreferenceStrings.getAudioQualityDesc()) }

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
                        text = { Text(PreferenceStrings.getAudioQualityDesc(i)) },
                        onClick = {
                            audioQualityText =
                                PreferenceStrings.getAudioQualityDesc(i)
                            audioQuality = i
                            expanded = false
                        })
            }
        }
    }

    @Composable
    fun audioFormatSelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var audioFormatText by remember { mutableStateOf(PreferenceStrings.getAudioFormatDesc()) }

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
                        text = { Text(PreferenceStrings.getAudioFormatDesc(i)) },
                        onClick = {
                            audioFormatText =
                                PreferenceStrings.getAudioFormatDesc(i)
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
fun VideoQuickSettingsDialog(onDismissRequest: () -> Unit = {}) {
    var videoResolution by VIDEO_QUALITY.intState
    var videoFormat by VIDEO_FORMAT.intState

    @Composable
    fun videoResolutionSelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var videoResolutionText = PreferenceStrings.getVideoResolutionDesc(videoResolution)

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
                        text = { Text(PreferenceStrings.getVideoResolutionDesc(i)) },
                        onClick = {
//                            videoResolutionText =
//                                PreferenceStrings.getVideoResolutionDesc(i)
                            videoResolution = i
                            expanded = false
                        })
            }
        }
    }

    @Composable
    fun videoFormatSelectField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        var videoFormatText by remember { mutableStateOf(PreferenceStrings.getVideoFormatDesc()) }

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
                for (i in listOf(NOT_SPECIFIED, FORMAT_COMPATIBILITY, FORMAT_QUALITY))
                    DropdownMenuItem(
                        text = { Text(PreferenceStrings.getVideoFormatDesc(i)) },
                        onClick = {
                            videoFormatText =
                                PreferenceStrings.getVideoFormatDesc(i)
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
fun AudioConversionDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit = {}
) {
    var audioFormat by remember { mutableStateOf(PreferenceUtil.getAudioConvertFormat()) }
    var convertAudio by AUDIO_CONVERT.booleanState
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
                    DialogSingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceStrings.getAudioConvertDesc(i),
                        selected = audioFormat == i
                    ) { audioFormat = i }
            }
        })
}

@Composable
fun AudioConversionQuickSettingsDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit = {}
) {
    var audioFormat by remember { mutableIntStateOf(PreferenceUtil.getAudioConvertFormat()) }
    var convertAudio by AUDIO_CONVERT.booleanState
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            DismissButton { onDismissRequest() }
        },
        icon = { Icon(Icons.Outlined.Sync, null) },
        title = {
            Text(stringResource(R.string.convert_audio_format))
        }, confirmButton = {
            ConfirmButton {
                AUDIO_CONVERT.updateBoolean(convertAudio)
                AUDIO_CONVERSION_FORMAT.updateInt(audioFormat)
                onConfirm()
                onDismissRequest()
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
                DialogSingleChoiceItem(
                    text = stringResource(id = R.string.not_convert),
                    selected = !convertAudio
                ) {
                    convertAudio = false
                }
                for (i in CONVERT_MP3..CONVERT_M4A)
                    DialogSingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceStrings.getAudioConvertDesc(i),
                        selected = audioFormat == i && convertAudio
                    ) {
                        audioFormat = i
                        convertAudio = true
                    }
            }
        })
}


@Composable
@Preview
fun VideoFormatDialog(
    videoFormatPreference: Int = FORMAT_COMPATIBILITY,
    onDismissRequest: () -> Unit = {},
    onConfirm: (Int) -> Unit = {}
) {
    var preference by remember { mutableIntStateOf(videoFormatPreference) }
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
                onConfirm(preference)
                onDismissRequest()
            }) {
                Text(text = stringResource(R.string.confirm))
            }
        }, text = {
            Column {
                HorizontalDivider()
                LazyColumn(modifier = Modifier, contentPadding = PaddingValues(vertical = 8.dp)) {

                    for (i in listOf(FORMAT_COMPATIBILITY, FORMAT_QUALITY))
                        item {
                            DialogSingleChoiceItemWithLabel(
                                modifier = Modifier,
                                text = PreferenceStrings.getVideoFormatLabel(i),
                                label = PreferenceStrings.getVideoFormatDescComp(i),
                                selected = preference == i,
                            ) { preference = i }
                        }
                }
                HorizontalDivider()

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
                    DialogSingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceStrings.getAudioFormatDesc(i),
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
                for (i in NOT_SPECIFIED..ULTRA_LOW)
                    DialogSingleChoiceItem(
                        modifier = Modifier,
                        text = PreferenceStrings.getAudioQualityDesc(i),
                        selected = audioQuality == i
                    ) { audioQuality = i }
            }
        })
}

@Composable
fun FormatSortingDialog(
    fields: String,
    showSwitch: Boolean = false,
    toggleableValue: Boolean = false,
    onSwitchChecked: (Boolean) -> Unit = {},
    onImport: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {}
) {
    var sortingFields by remember(fields) {
        mutableStateOf(fields)
    }
    SealDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            DismissButton { onDismissRequest() }
        }, icon = { Icon(Icons.AutoMirrored.Outlined.Sort, null) },
        title = {
            Text(stringResource(R.string.format_sorting))
        }, confirmButton = {
            ConfirmButton(text = stringResource(id = R.string.save)) {
                onConfirm(sortingFields)
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
                        .padding(horizontal = 8.dp)
                ) {
                    OutlinedButtonChip(
                        modifier = Modifier.padding(end = 8.dp),
                        label = stringResource(id = R.string.import_from_preferences),
                        icon = Icons.Outlined.SettingsSuggest
                    ) {
                        onImport()
                    }
                    OutlinedButtonChip(
                        label = stringResource(R.string.yt_dlp_docs),
                        icon = Icons.AutoMirrored.Outlined.OpenInNew
                    ) {
                        uriHandler.openUri(sortingFormats)
                    }
                }
                if (showSwitch) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    DialogSwitchItem(
                        text = stringResource(id = R.string.use_format_sorting),
                        value = toggleableValue,
                        onValueChange = onSwitchChecked
                    )
                }
            }
        })
}

@Preview
@Composable
private fun FormatSortingDialogPreview() {
    var value by remember { mutableStateOf(false) }
    FormatSortingDialog(
        fields = "",
        showSwitch = true,
        toggleableValue = value,
        onSwitchChecked = { value = it })
}

@Composable
fun VideoQualityDialog(
    videoQuality: Int = 0,
    onDismissRequest: () -> Unit = {},
    onConfirm: (Int) -> Unit = {}
) {
    var videoResolution by remember { mutableIntStateOf(videoQuality) }

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
                onConfirm(videoResolution)
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
                            DialogSingleChoiceItem(
                                text = PreferenceStrings.getVideoResolutionDesc(i),
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
    SubtitleLanguageDialogImpl(
        onDismissRequest = onDismissRequest,
        initialLanguages = languages,
        onReset = {
            SUBTITLE_LANGUAGE.let {
                languages = it.getStringDefault()
                it.updateString(languages)
            }
        },
        onConfirm = { SUBTITLE_LANGUAGE.updateString(it) },
    )
}


@Composable
@Preview
private fun SubtitleLanguageDialogImpl(
    onDismissRequest: () -> Unit = {},
    initialLanguages: String = "en.*,.*-orig",
    onReset: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    var languages by remember(initialLanguages) { mutableStateOf(initialLanguages) }
    val uriHandler = LocalUriHandler.current
    SealDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.subtitle_language)) },
        icon = { Icon(Icons.Outlined.Language, null) },
        text = {
            Column() {
                Text(
                    text = stringResource(id = R.string.subtitle_language_desc),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProvideTextStyle(value = LocalTextStyle.current.merge(fontFamily = FontFamily.Monospace)) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        value = languages,
                        onValueChange = { languages = it },
                        label = {
                            Text(stringResource(id = R.string.subtitle_language))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    OutlinedButtonChip(
                        modifier = Modifier.padding(end = 8.dp),
                        label = stringResource(id = R.string.reset),
                        icon = Icons.Outlined.Sync
                    ) {
                        onReset()
                    }
                    OutlinedButtonChip(
                        label = stringResource(R.string.yt_dlp_docs),
                        icon = Icons.AutoMirrored.Outlined.OpenInNew
                    ) {
                        uriHandler.openUri(sortingFormats)
                    }
                }
            }
        }, confirmButton = {
            ConfirmButton() {
                onConfirm(languages)
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
                        DialogSingleChoiceItem(
                            text = PreferenceStrings.getSubtitleConversionFormat(format),
                            selected = currentFormat == format
                        ) {
                            currentFormat = format
                        }
                    }
                }
            }
        })
}

@Composable
@Preview
fun VideoQualityPreferenceChip(
    modifier: Modifier = Modifier,
    videoQualityPreference: Int = FORMAT_COMPATIBILITY,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    ElevatedAssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(
                text = PreferenceStrings.getVideoFormatLabel(videoQualityPreference)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.VideoSettings,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        })
}

@Composable
@Preview
fun VideoResolutionChip(
    modifier: Modifier = Modifier,
    videoResolution: Int = 0,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    ElevatedAssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(
                text = PreferenceStrings.getVideoResolutionDescComp(videoResolution)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.VideoSettings,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        })
}