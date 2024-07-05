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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
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
import com.junkfood.seal.ui.component.SingleChoiceChip
import com.junkfood.seal.ui.component.SingleChoiceSegmentedButton
import com.junkfood.seal.ui.page.downloadv2.ActionButton.Download
import com.junkfood.seal.ui.page.downloadv2.ActionButton.FetchInfo
import com.junkfood.seal.ui.page.downloadv2.ActionButton.StartTask
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DOWNLOAD_TYPE_INITIALIZATION
import com.junkfood.seal.util.DownloadType
import com.junkfood.seal.util.DownloadType.Audio
import com.junkfood.seal.util.DownloadType.Command
import com.junkfood.seal.util.DownloadType.Playlist
import com.junkfood.seal.util.DownloadType.Video
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.FORMAT_QUALITY
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.HIGH
import com.junkfood.seal.util.LOW
import com.junkfood.seal.util.M4A
import com.junkfood.seal.util.MEDIUM
import com.junkfood.seal.util.NOT_SPECIFIED
import com.junkfood.seal.util.OPUS
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PreferenceStrings
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.ULTRA_LOW
import com.junkfood.seal.util.USE_PREVIOUS_SELECTION


@Composable
private fun DownloadType.label(): String = stringResource(
    when (this) {
        Audio -> R.string.audio
        Video -> R.string.video
        Command -> R.string.commands
        Playlist -> R.string.playlist
    }
)

private fun DownloadType.updatePreference() {
    when (this) {
        Audio -> {
            EXTRACT_AUDIO.updateBoolean(true)
            CUSTOM_COMMAND.updateBoolean(false)
        }

        Video -> {
            EXTRACT_AUDIO.updateBoolean(false)
            CUSTOM_COMMAND.updateBoolean(false)
        }

        Command -> {
            CUSTOM_COMMAND.updateBoolean(true)
        }

        Playlist -> {
            PLAYLIST.updateBoolean(true)
            CUSTOM_COMMAND.updateBoolean(false)
        }
    }
}

data class Config(
    val usePreviousType: Boolean = DOWNLOAD_TYPE_INITIALIZATION.getInt() == USE_PREVIOUS_SELECTION,
    val downloadType: DownloadType = PreferenceUtil.getDownloadType(),
    val useFormatSelection: Boolean = FORMAT_SELECTION.getBoolean(),
    val typeEntries: List<DownloadType> = when (CUSTOM_COMMAND.getBoolean()) {
        true -> DownloadType.entries
        false -> DownloadType.entries - Command
    }
) {
    companion object {
        fun updatePreferences(downloadType: DownloadType, useFormatSelection: Boolean) {
            PreferenceUtil.updateDownloadType(downloadType)
            FORMAT_SELECTION.updateBoolean(useFormatSelection)
        }
    }
}

@Composable
fun ConfigurePage(
    modifier: Modifier = Modifier, onActionPosted: (Action) -> Unit
) {

}

@Composable
private fun ConfigurePageImpl(
    modifier: Modifier = Modifier,
    config: Config,
    preference: DownloadUtil.DownloadPreferences,
    onActionPosted: (Action) -> Unit
) {
    var selectedType by remember {
        mutableStateOf(
            if (config.usePreviousType) {
                config.downloadType
            } else {
                null
            }
        )
    }
    val useFormatSelection by remember { mutableStateOf(config.useFormatSelection) }
    val canProceed = selectedType in config.typeEntries

    Column {
        Header()
        DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
        DownloadTypeSelectionGroup(typeEntries = config.typeEntries,
            selectedType = selectedType,
            onSelect = { selectedType = it })
        DrawerSheetSubtitle(
            text = stringResource(id = R.string.format_selection),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        ActionButtons(
            canProceed = canProceed,
            selectedType = selectedType,
            useFormatSelection = useFormatSelection,
            onCancel = { onActionPosted(Action.Hide) },
            onDownload = { onActionPosted(Action.DownloadWithPreset()) },
            onFetchInfo = { onActionPosted() },
            onTaskStart = {},
        )
    }

}

@Preview
@Composable
private fun AdditionalSettings(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = Dp.Hairline, modifier = Modifier.padding(horizontal = 20.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.additional_settings),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

@Preview
@Composable
private fun FormatSelectionAuto() {
    MaterialTheme {
        var selected by remember { mutableStateOf(true) }
        SingleChoiceItem(title = "Preset", desc = "Prefer Quality, 1080p", icon = {
            Icon(
                imageVector = if (selected) Icons.Filled.SettingsSuggest else Icons.Outlined.SettingsSuggest,
                null,
                modifier = Modifier.size(20.dp)
            )
        }, selected = selected, action = {
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null, modifier = Modifier.size(20.dp),
                )
            }
        }) {
            selected = !selected
        }
    }
}

@Composable
fun SingleChoiceItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    selected: Boolean,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        selected = selected,
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.invoke()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            action?.invoke()
        }
    }
}

@Preview
@Composable
private fun FormatSelectionCustom() {
    MaterialTheme {
        var selected by remember { mutableStateOf(false) }
        SingleChoiceItem(
            title = "Custom",
            desc = "Select from formats, subtitles, and customize further",
            icon = {
                Icon(
                    if (selected) Icons.Filled.VideoFile else Icons.Outlined.VideoFile,
                    null,
                    modifier = Modifier.size(20.dp)
                )
            },
            selected = selected
        ) {
            selected = !selected
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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
    }
}

@Composable
private fun DownloadTypeSelectionGroup(
    modifier: Modifier = Modifier,
    typeEntries: List<DownloadType>,
    selectedType: DownloadType?,
    onSelect: (DownloadType) -> Unit
) {
    val typeCount = typeEntries.size
    if (typeCount == DownloadType.entries.size) {
        LazyRow(modifier = modifier) {
            items(typeEntries) { type ->
                SingleChoiceChip(selected = selectedType == type,
                    label = type.label(),
                    onClick = { onSelect(type) })
            }
        }
    } else {
        SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
            typeEntries.forEachIndexed { index, type ->
                SingleChoiceSegmentedButton(
                    selected = selectedType == type,
                    onClick = { onSelect(type) },
                    shape = SegmentedButtonDefaults.itemShape(index, typeCount)
                ) {
                    Text(text = type.label())
                }
            }
        }
    }
}


@Composable
private fun Preset(
    modifier: Modifier = Modifier,
    preference: DownloadUtil.DownloadPreferences,
    type: DownloadType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val description = preference.run {
        when (type) {
            Audio -> {
                val preferredFormat = when (audioFormat) {
                    M4A -> "M4A"
                    OPUS -> "OPUS"
                    else -> null
                }
                val preferredQuality = when (audioQuality) {
                    NOT_SPECIFIED -> stringResource(R.string.unlimited)
                    HIGH -> "192 Kbps"
                    MEDIUM -> "128 Kbps"
                    LOW -> "64 Kbps"
                    ULTRA_LOW -> "32 Kbps"
                    else -> stringResource(R.string.lowest_bitrate)
                }
            }

            Video -> {
                val preferredFormat =
                    stringResource(
                        id = R.string.prefer_placeholder,
                        stringResource(id = if (videoFormat == FORMAT_QUALITY) R.string.quality else R.string.legacy)
                    )
                val preferredResolution = PreferenceStrings.getVideoResolutionDescRes()
                listOf(preferredFormat, preferredResolution).joinToString(separator = ", ")
            }
            Playlist -> TODO()
            Command -> TODO()
        }
    }

    SingleChoiceItem(
        modifier = modifier, title = stringResource(R.string.preset),
        desc = "Prefer Quality, 1080p",
        icon = {
            Icon(
                imageVector = if (!selected) Icons.Filled.SettingsSuggest else Icons.Outlined.SettingsSuggest,
                null,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = !selected,
        action = {
            if (!selected) {
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null, modifier = Modifier.size(20.dp),
                )
            }
        },
        onClick = onClick
    )
}

@Composable
private fun Custom(modifier: Modifier = Modifier) {

}

@Composable
private fun DownloadModeSelectionGroup(
    modifier: Modifier = Modifier,
    preference: DownloadUtil.DownloadPreferences,
    formatSelectionEnabled: Boolean,
    useFormatSelection: Boolean,
    onPresetClicked: () -> Unit,
    onCustomClicked: () -> Unit
) {
    Column(modifier = modifier) {
        SingleChoiceItem(
            title = stringResource(R.string.preset),
            desc = "Prefer Quality, 1080p",
            icon = {
                Icon(
                    imageVector = if (!useFormatSelection) Icons.Filled.SettingsSuggest else Icons.Outlined.SettingsSuggest,
                    null,
                    modifier = Modifier.size(20.dp)
                )
            },
            selected = !useFormatSelection,
            action = {
                if (!useFormatSelection) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null, modifier = Modifier.size(20.dp),
                    )
                }
            },
            onClick = onPresetClicked
        )
    }

}

private enum class ActionButton {
    FetchInfo, Download, StartTask
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    canProceed: Boolean,
    selectedType: DownloadType?,
    useFormatSelection: Boolean,
    onCancel: () -> Unit,
    onFetchInfo: () -> Unit,
    onDownload: () -> Unit,
    onTaskStart: () -> Unit,
) {
    val action = if (selectedType == Command) {
        StartTask
    } else if (selectedType == Playlist || useFormatSelection) {
        FetchInfo
    } else {
        Download
    }

    val actionIcon = when (action) {
        FetchInfo -> Icons.AutoMirrored.Filled.ArrowForward
        Download -> Icons.Filled.Download
        StartTask -> Icons.Filled.DownloadDone
    }

    val actionLabel = stringResource(
        when (action) {
            FetchInfo -> R.string.proceed
            Download -> R.string.download
            StartTask -> R.string.start
        }
    )

    val state = rememberLazyListState()
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.End,
        state = state,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            OutlinedButtonWithIcon(
                modifier = Modifier.padding(horizontal = 12.dp),
                onClick = onCancel,
                icon = Icons.Outlined.Cancel,
                text = stringResource(R.string.cancel)
            )
        }
        item {
            FilledButtonWithIcon(
                modifier = Modifier,
                enabled = canProceed,
                onClick = {
                    when (action) {
                        FetchInfo -> onFetchInfo()
                        Download -> onDownload()
                        StartTask -> onTaskStart()
                    }
                },
                icon = actionIcon,
                text = actionLabel,
            )
        }
    }
}