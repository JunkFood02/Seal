package com.junkfood.seal.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import com.junkfood.seal.desktop.download.DownloadPreferences
import com.junkfood.seal.desktop.download.DownloadType

/**
 * Download dialog mirroring the Android app's: URL input plus per-download options (type,
 * quality, format, playlist/subtitle/embed toggles, custom yt-dlp arguments) seeded from the
 * saved defaults, and an entry point into the format selector.
 *
 * The URL field is prefilled from [initialUrl] (a link shared from another app) when given,
 * falling back to a link on the clipboard.
 */
@Composable
fun DownloadDialog(
    defaultPreferences: DownloadPreferences,
    onDismiss: () -> Unit,
    onConfirm: (url: String, preferences: DownloadPreferences) -> Unit,
    onSelectFormats: (url: String, preferences: DownloadPreferences) -> Unit,
    initialUrl: String? = null,
) {
    val clipboard = LocalClipboardManager.current
    // Keyed on initialUrl so a link shared while the dialog is already open replaces the field.
    var url by remember(initialUrl) {
        val shared = initialUrl?.trim().orEmpty()
        val clip = runCatching { clipboard.getText()?.text?.trim() }.getOrNull().orEmpty()
        mutableStateOf(
            when {
                shared.isNotEmpty() -> shared
                clip.startsWith("http://") || clip.startsWith("https://") -> clip
                else -> ""
            }
        )
    }
    var prefs by remember { mutableStateOf(defaultPreferences.copy(formatId = null)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(560.dp),
        title = { Text("New download") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://...") },
                    label = { Text("Video URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = prefs.downloadType == DownloadType.Video,
                        onClick = { prefs = prefs.copy(downloadType = DownloadType.Video) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Outlined.VideoFile, contentDescription = null) },
                        label = { Text("Video") },
                    )
                    SegmentedButton(
                        selected = prefs.downloadType == DownloadType.Audio,
                        onClick = { prefs = prefs.copy(downloadType = DownloadType.Audio) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Outlined.AudioFile, contentDescription = null) },
                        label = { Text("Audio") },
                    )
                }

                if (prefs.downloadType == DownloadType.Video) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownSelector(
                            label = "Quality",
                            options = DownloadPreferences.videoQualityOptions,
                            selected = prefs.videoQuality,
                            onSelect = { prefs = prefs.copy(videoQuality = it) },
                            modifier = Modifier.weight(1f),
                        )
                        DropdownSelector(
                            label = "Format",
                            options = DownloadPreferences.videoFormatOptions,
                            selected = prefs.videoFormat,
                            onSelect = { prefs = prefs.copy(videoFormat = it) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    DropdownSelector(
                        label = "Audio format",
                        options = DownloadPreferences.audioFormatOptions,
                        selected = prefs.audioFormat,
                        onSelect = { prefs = prefs.copy(audioFormat = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OptionChip("Playlist", prefs.downloadPlaylist) {
                        prefs = prefs.copy(downloadPlaylist = it)
                    }
                    OptionChip("Subtitles", prefs.downloadSubtitles) {
                        prefs = prefs.copy(downloadSubtitles = it)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OptionChip("Thumbnail", prefs.embedThumbnail) {
                        prefs = prefs.copy(embedThumbnail = it)
                    }
                    OptionChip("Metadata", prefs.embedMetadata) {
                        prefs = prefs.copy(embedMetadata = it)
                    }
                    OptionChip("SponsorBlock", prefs.sponsorBlock) {
                        prefs = prefs.copy(sponsorBlock = it)
                    }
                }

                OutlinedTextField(
                    value = prefs.customArgs,
                    onValueChange = { prefs = prefs.copy(customArgs = it) },
                    label = { Text("Extra yt-dlp arguments") },
                    placeholder = { Text("--cookies-from-browser firefox …") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(url.trim(), prefs) }, enabled = url.isNotBlank()) {
                Text("Download")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(
                    onClick = { onSelectFormats(url.trim(), prefs) },
                    enabled = url.isNotBlank() && !prefs.downloadPlaylist,
                ) {
                    Text("Select formats")
                }
            }
        },
    )
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(selected = selected, onClick = { onToggle(!selected) }, label = { Text(label) })
}

@Composable
private fun <T> DropdownSelector(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(
                    options.firstOrNull { it.first == selected }?.second ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelect(value)
                    },
                )
            }
        }
    }
}
