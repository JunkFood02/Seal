package com.junkfood.seal.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.SingleChoiceChip
import com.junkfood.seal.ui.component.SingleChoiceSegmentedButton
import com.junkfood.seal.ui.page.downloadv2.ActionButton.Download
import com.junkfood.seal.ui.page.downloadv2.ActionButton.FetchInfo
import com.junkfood.seal.ui.page.downloadv2.ActionButton.StartTask
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.CONVERT_MP3
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DOWNLOAD_TYPE_INITIALIZATION
import com.junkfood.seal.util.DownloadType
import com.junkfood.seal.util.DownloadType.Audio
import com.junkfood.seal.util.DownloadType.Command
import com.junkfood.seal.util.DownloadType.Playlist
import com.junkfood.seal.util.DownloadType.Video
import com.junkfood.seal.util.DownloadType.entries
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

private val PreferencesMock = DownloadUtil.DownloadPreferences(
    extractAudio = false,
    createThumbnail = false,
    downloadPlaylist = false,
    subdirectoryExtractor = false,
    subdirectoryPlaylistTitle = false,
    commandDirectory = "",
    downloadSubtitle = false,
    embedSubtitle = false,
    keepSubtitle = false,
    subtitleLanguage = "",
    autoSubtitle = false,
    autoTranslatedSubtitles = false,
    convertSubtitle = 0,
    concurrentFragments = 0,
    sponsorBlock = false,
    sponsorBlockCategory = "",
    cookies = false,
    aria2c = false,
    audioFormat = 0,
    audioQuality = 0,
    convertAudio = false,
    formatSorting = false,
    sortingFields = "",
    audioConvertFormat = 0,
    videoFormat = 0,
    formatIdString = "",
    videoResolution = 0,
    privateMode = false,
    rateLimit = false,
    maxDownloadRate = "",
    privateDirectory = false,
    cropArtwork = false,
    sdcard = false,
    sdcardUri = "",
    embedThumbnail = false,
    videoClips = emptyList(),
    splitByChapter = false,
    debug = false,
    proxy = false,
    proxyUrl = "",
    newTitle = "",
    userAgentString = "",
    outputTemplate = "",
    useDownloadArchive = false,
    embedMetadata = false,
    restrictFilenames = false,
    supportAv1HardwareDecoding = false,
    forceIpv4 = false,
    mergeAudioStream = false,
    mergeToMkv = false
)

data class Config(
    val usePreviousType: Boolean = DOWNLOAD_TYPE_INITIALIZATION.getInt() == USE_PREVIOUS_SELECTION,
    val downloadType: DownloadType = PreferenceUtil.getDownloadType(),
    val typeEntries: List<DownloadType> = when (CUSTOM_COMMAND.getBoolean()) {
        true -> DownloadType.entries
        false -> DownloadType.entries - Command
    },
    val useFormatSelection: Boolean = FORMAT_SELECTION.getBoolean(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)

@Composable
private fun ConfigurePagePreview() {
    SealTheme() {
        SealModalBottomSheet(
            sheetState = SheetState(
                skipPartiallyExpanded = true,
                LocalDensity.current,
                SheetValue.Expanded,
                { true },
                false,
            ),
            onDismissRequest = {}, contentPadding = PaddingValues()
        ) {
            ConfigurePageImpl(
                config = Config(
                    usePreviousType = false,
                    downloadType = Audio,
                    useFormatSelection = true,
                    typeEntries = entries - DownloadType.Command
                ), preference = PreferencesMock
            ) { }
        }
    }
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
    var useFormatSelection by remember { mutableStateOf(config.useFormatSelection) }
    val canProceed = selectedType in config.typeEntries

    Column {
        Column(modifier = modifier.padding(horizontal = 20.dp)) {
            Header(modifier = Modifier.align(Alignment.CenterHorizontally))
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
            DownloadTypeSelectionGroup(typeEntries = config.typeEntries,
                selectedType = selectedType,
                onSelect = { selectedType = it })
            DrawerSheetSubtitle(
                text = stringResource(id = R.string.format_selection), modifier = Modifier
            )
            Preset(modifier = Modifier.animateContentSize(),
                preference = preference,
                selected = !useFormatSelection,
                downloadType = selectedType,
                onClick = { useFormatSelection = false },
                showIconButton = !useFormatSelection && selectedType != Playlist,
                onIconButtonClicked = {
                    // TODO: show preset dialog
                })
            Custom(
                selected = useFormatSelection,
                enabled = selectedType != Playlist,
                onClick = { useFormatSelection = true }
            )
        }

        AdditionalSettings()

        ActionButtons(
            modifier = Modifier.padding(horizontal = 20.dp),
            canProceed = canProceed,
            selectedType = selectedType,
            useFormatSelection = useFormatSelection,
            onCancel = { onActionPosted(Action.Hide) },
            onDownload = { },
            onFetchInfo = { },
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
            Spacer(modifier = Modifier.width(28.dp))
        }
    }
}

@Composable
private fun SingleChoiceItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    selected: Boolean,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        selected = selected,
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .padding(vertical = 4.dp)
            .run {
                if (!enabled) alpha(0.32f) else this
            },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
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
    downloadType: DownloadType?,
    selected: Boolean,
    showIconButton: Boolean,
    onIconButtonClicked: () -> Unit,
    onClick: () -> Unit
) {
    val description = preference.run {
        when (downloadType) {
            Audio -> {
                if (convertAudio) {
                    when (audioConvertFormat) {
                        CONVERT_MP3 -> stringResource(R.string.convert_to, "MP3")
                        else -> stringResource(R.string.convert_to, "M4A")
                    }
                } else {
                    val preferredFormat = when (audioFormat) {
                        M4A -> stringResource(R.string.prefer_placeholder, "M4A")
                        OPUS -> stringResource(R.string.prefer_placeholder, "OPUS")
                        else -> null
                    }
                    val preferredQuality =
                        stringResource(R.string.audio_quality) + ": " + when (audioQuality) {
                            NOT_SPECIFIED -> stringResource(R.string.unlimited)
                            HIGH -> "192 Kbps"
                            MEDIUM -> "128 Kbps"
                            LOW -> "64 Kbps"
                            ULTRA_LOW -> "32 Kbps"
                            else -> stringResource(R.string.lowest_bitrate)
                        }
                    listOfNotNull(preferredFormat, preferredQuality).joinToString(separator = ", ")
                }
            }

            Video -> {
                val preferredFormat = stringResource(
                    id = R.string.prefer_placeholder,
                    stringResource(id = if (videoFormat == FORMAT_QUALITY) R.string.quality else R.string.legacy)
                )
                val preferredResolution =
                    PreferenceStrings.getVideoResolutionDescRes(videoResolution)
                listOf(preferredFormat, preferredResolution).joinToString(separator = ", ")
            }

            Playlist -> stringResource(R.string.preset_format_selection_desc)
            else -> ""
        }
    }

    SingleChoiceItem(
        modifier = modifier,
        title = stringResource(R.string.preset),
        desc = description,
        icon = {
            Icon(
                imageVector = if (selected) Icons.Filled.SettingsSuggest else Icons.Outlined.SettingsSuggest,
                null,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = selected,
        action = {
            if (showIconButton) {
                IconButton(onClick = onIconButtonClicked) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null, modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
private fun Custom(
    modifier: Modifier = Modifier, selected: Boolean, enabled: Boolean, onClick: () -> Unit
) {
    SingleChoiceItem(
        modifier = modifier,
        title = stringResource(R.string.custom),
        desc = stringResource(R.string.custom_format_selection_desc),
        icon = {
            Icon(
                if (selected) Icons.Filled.VideoFile else Icons.Outlined.VideoFile,
                null,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = selected,
        enabled = enabled,
        onClick = onClick
    )
}

private enum class ActionButton {
    FetchInfo, Download, StartTask
}

@Composable
private fun ActionButton.Icon() {
    Icon(
        imageVector = when (this) {
            FetchInfo -> Icons.AutoMirrored.Filled.ArrowForward
            Download -> Icons.Filled.Download
            StartTask -> Icons.Filled.DownloadDone
        }, contentDescription = null, modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun ActionButton.Label() {
    Text(
        stringResource(
            when (this) {
                FetchInfo -> R.string.proceed
                Download -> R.string.download
                StartTask -> R.string.start
            }
        ), modifier = Modifier.padding(start = 8.dp)
    )
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

    val state = rememberLazyListState()
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
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
            Button(
                modifier = Modifier,
                onClick = {
                    when (action) {
                        FetchInfo -> onFetchInfo()
                        Download -> onDownload()
                        StartTask -> onTaskStart()
                    }
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                enabled = canProceed
            ) {
                AnimatedContent(targetState = action, label = "", transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90))).togetherWith(
                        fadeOut(
                            animationSpec = tween(90)
                        )
                    )
                }) { action ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        action.Icon()
                        action.Label()
                    }
                }

            }
        }
    }
}