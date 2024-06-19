package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.PasteButton
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PreferenceUtil.updateBoolean

enum class DownloadType {
    Audio, Video, Playlist, Command
}

@Composable
private fun DownloadType.label(): String = stringResource(
    when (this) {
        DownloadType.Audio -> R.string.audio
        DownloadType.Video -> R.string.video
        DownloadType.Command -> R.string.commands
        DownloadType.Playlist -> R.string.playlist
    }
)

private fun DownloadType.updatePreference() {
    when (this) {
        DownloadType.Audio -> {
            EXTRACT_AUDIO.updateBoolean(true)
            CUSTOM_COMMAND.updateBoolean(false)
        }

        DownloadType.Video -> {
            EXTRACT_AUDIO.updateBoolean(false)
            CUSTOM_COMMAND.updateBoolean(false)
        }

        DownloadType.Command -> {
            CUSTOM_COMMAND.updateBoolean(true)
        }

        DownloadType.Playlist -> {
            PLAYLIST.updateBoolean(true)
            CUSTOM_COMMAND.updateBoolean(false)
        }
    }
}


@Composable
@Preview
fun DownloadDialogV2() {
    Surface {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
/*            DrawerSheetSubtitle(text = stringResource(id = R.string.video_url))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                trailingIcon = { PasteButton() }
            )*/


            var selectedType: DownloadType? by remember { mutableStateOf(DownloadType.Video) }
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedType == DownloadType.Audio,
                    onClick = { selectedType = DownloadType.Audio },
                    modifier = Modifier.height(36.dp),
                    shape = SegmentedButtonDefaults.itemShape(0, 3)
                ) {
                    Text(text = stringResource(id = R.string.audio))
                }
                SegmentedButton(
                    selected = selectedType == DownloadType.Video,
                    onClick = { selectedType = DownloadType.Video },
                    modifier = Modifier.height(36.dp),
                    shape = SegmentedButtonDefaults.itemShape(1, 3)
                ) {
                    Text(text = stringResource(id = R.string.video))
                }
                SegmentedButton(
                    selected = selectedType == DownloadType.Playlist,
                    onClick = { selectedType = DownloadType.Playlist },
                    modifier = Modifier.height(36.dp),
                    shape = SegmentedButtonDefaults.itemShape(2, 3)
                ) {
                    Text(text = stringResource(id = R.string.playlist))
                }
            }

            DrawerSheetSubtitle(text = stringResource(id = R.string.format_selection))

            FormatSelectionAuto()
            FormatSelectionCustom()

            AdditionalSettings()

            Spacer(Modifier.height(12.dp))


            val state = rememberLazyListState()
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                state = state,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    OutlinedButtonWithIcon(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {},
                        icon = Icons.Outlined.Cancel,
                        text = stringResource(R.string.cancel)
                    )
                }
                item {
                    FilledButtonWithIcon(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {},
                        icon = Icons.Filled.Download,
                        text = stringResource(R.string.start_download),
                        enabled = selectedType != null
                    )
                }
            }
        }
    }

}

@Preview
@Composable
private fun AdditionalSettings() {
    Surface {

        Column {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = Dp.Hairline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Additional settings",
                    style = MaterialTheme.typography.labelLarge,
//                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                )

            }
        }
    }

}

@Preview
@Composable
private fun FormatSelectionAuto() {
    MaterialTheme {
        var selected by remember { mutableStateOf(false) }
        Surface {
            SingleChoiceItem(
                title = stringResource(R.string.presets),
                desc = "Prefer Quality, 1080p",
                icon = Icons.Outlined.VideoFile,
                selected = selected
            ) {
                selected = !selected
            }
        }
    }
}

@Composable
fun SingleChoiceItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = icon, contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(12.dp))
        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}

@Preview
@Composable
private fun FormatSelectionCustom() {
    MaterialTheme {
        var selected by remember { mutableStateOf(true) }
        Surface {
            SingleChoiceItem(
                title = "Custom",
                desc = "Select from formats, subtitles, and customize further",
                icon = Icons.Outlined.VideoSettings,
                selected = selected
            ) {
                selected = !selected
            }
        }
    }
}