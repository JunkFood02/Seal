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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.junkfood.seal.ui.component.SegmentedButtonValues
import com.junkfood.seal.ui.component.SingleChoiceSegmentedButton
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

            var selectedType: DownloadType? by remember { mutableStateOf(DownloadType.Video) }
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SingleChoiceSegmentedButton(
                    text = stringResource(id = R.string.audio),
                    selected = selectedType == DownloadType.Audio,
                    position = SegmentedButtonValues.START
                ) {

                }
                SingleChoiceSegmentedButton(
                    text = stringResource(id = R.string.video),
                    selected = selectedType == DownloadType.Video
                ) {

                }
                SingleChoiceSegmentedButton(
                    text = stringResource(id = R.string.playlist),
                    selected = selectedType == DownloadType.Playlist,
                    position = SegmentedButtonValues.END
                ) {

                }
            }

            DrawerSheetSubtitle(text = stringResource(id = R.string.format_selection))

            FormatSelectionAuto()
            FormatSelectionCustom()
//            LazyRow(modifier = Modifier.fillMaxWidth()) {
//                item {
//                    SingleChoiceChip(selected = true, label = stringResource(R.string.auto)) {
//
//                    }
//                }
//                item {
//                    SingleChoiceChip(selected = false, label = stringResource(R.string.custom)) {
//
//                    }
//                }
//            }

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
    Surface {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(4.dp))

                Icon(imageVector = Icons.Outlined.VideoFile, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.presets),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Prefer Quality, 1080p",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Checkbox(checked = true, onCheckedChange = null)

            }
        }
    }
}

@Preview
@Composable
private fun FormatSelectionCustom() {
    Surface {

        Column {
            Spacer(Modifier.height(4.dp))
//            HorizontalDivider(thickness = Dp.Hairline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(4.dp))

                Icon(imageVector = Icons.Outlined.VideoSettings, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Custom",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Select from all formats, more preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Checkbox(checked = false, onCheckedChange = null)

            }
        }
    }
}