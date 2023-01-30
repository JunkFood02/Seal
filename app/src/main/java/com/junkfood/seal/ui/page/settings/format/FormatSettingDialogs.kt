package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.stringState
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.ui.component.SingleChoiceItem
import com.junkfood.seal.util.AUDIO_FORMAT
import com.junkfood.seal.util.MAX_FILE_SIZE
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.SUBTITLE_LANGUAGE
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY

@Composable
fun AudioFormatDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var audioFormat by remember { mutableStateOf(PreferenceUtil.getAudioFormat()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        icon = { Icon(Icons.Outlined.AudioFile, null) },
        title = {
            Text(stringResource(R.string.audio_format))
        }, confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.encodeInt(AUDIO_FORMAT, audioFormat)
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
                        .padding(bottom = 12.dp),
                    text = stringResource(R.string.audio_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in 0..2)
                    SingleChoiceItem(
                        text = PreferenceUtil.getAudioFormatDesc(i),
                        selected = audioFormat == i
                    ) { audioFormat = i }
            }
        })
}


@Composable
fun VideoFormatDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit = {}) {
    var videoFormat by remember { mutableStateOf(PreferenceUtil.getVideoFormat()) }
    AlertDialog(
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
                        .padding(bottom = 12.dp),
                    text = stringResource(R.string.video_format_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                for (i in 0..3)
                    SingleChoiceItem(
                        text = PreferenceUtil.getVideoFormatDesc(i),
                        selected = videoFormat == i
                    ) { videoFormat = i }
            }
        })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualityDialog(onDismissRequest: () -> Unit = {}, onConfirm: () -> Unit = {}) {
    var videoResolution by remember { mutableStateOf(PreferenceUtil.getVideoResolution()) }
    var fileSize by MAX_FILE_SIZE.stringState

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
    fun videoSizeTextField(modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        val notSpecified = stringResource(R.string.not_specified)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = fileSize,
                onValueChange = {
                    fileSize =
                        if (it.isDigitsOnly() || it == notSpecified) it else ""
                },
                leadingIcon = { Icon(Icons.Outlined.VideoFile, null) },
                trailingIcon = { Text("MB") },
                label = { Text(stringResource(id = R.string.video_file_size)) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(notSpecified) },
                    onClick = {
                        fileSize = notSpecified
                        expanded = false
                    })
            }
        }
    }

    AlertDialog(
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
                PreferenceUtil.encodeString(MAX_FILE_SIZE, fileSize)
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
                        .padding(bottom = 12.dp),
                    text = stringResource(R.string.video_quality_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                LazyColumn() {
                    item { videoResolutionSelectField() }
                    item { videoSizeTextField(modifier = Modifier.padding(top = 12.dp)) }
                }
            }
        })
}

private const val subtitleOptions = "https://github.com/yt-dlp/yt-dlp#subtitle-options"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SubtitleLanguageDialog(onDismissRequest: () -> Unit) {
    var languages by SUBTITLE_LANGUAGE.stringState
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
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
