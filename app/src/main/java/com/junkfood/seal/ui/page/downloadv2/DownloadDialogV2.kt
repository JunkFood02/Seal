package com.junkfood.seal.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DrawerSheetSubtitle
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.SingleChoiceChip
import com.junkfood.seal.ui.component.SingleChoiceSegmentedButton
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.downloadv2.ActionButton.Download
import com.junkfood.seal.ui.page.downloadv2.ActionButton.FetchInfo
import com.junkfood.seal.ui.page.downloadv2.ActionButton.StartTask
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.settings.format.VideoQuickSettingsDialog
import com.junkfood.seal.ui.page.settings.network.CookiesQuickSettingsDialog
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.COOKIES
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DOWNLOAD_TYPE_INITIALIZATION
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.DownloadType
import com.junkfood.seal.util.DownloadType.Audio
import com.junkfood.seal.util.DownloadType.Command
import com.junkfood.seal.util.DownloadType.Playlist
import com.junkfood.seal.util.DownloadType.Video
import com.junkfood.seal.util.DownloadType.entries
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FORMAT_SELECTION
import com.junkfood.seal.util.PreferenceStrings
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.THUMBNAIL
import com.junkfood.seal.util.USE_PREVIOUS_SELECTION
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY
import kotlinx.coroutines.launch


@Composable
private fun DownloadType.label(): String = stringResource(
    when (this) {
        Audio -> R.string.audio
        Video -> R.string.video
        Command -> R.string.commands
        Playlist -> R.string.playlist
    }
)

val PreferencesMock = DownloadUtil.DownloadPreferences(
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
                    typeEntries = entries - Command
                ), preference = PreferencesMock, onPreferenceUpdate = {}, settingChips = {}
            ) { }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurePagePreviewPreference() {

    var preference by remember { mutableStateOf(DownloadUtil.DownloadPreferences()) }
    val scope = rememberCoroutineScope()
    var showVideoPresetDialog by remember { mutableStateOf(false) }
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
                    typeEntries = entries - Command
                ),
                preference = preference,
                onPresetEdit = { type ->
                    when (type) {
                        Audio -> {
                            TODO()
                        }

                        Video -> {
                            showVideoPresetDialog = true
                        }

                        else -> {}
                    }
                },
                onPreferenceUpdate = {
                    scope.launch { preference = DownloadUtil.DownloadPreferences() }
                },
                settingChips = {
                    AdditionalSettings(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp),
                        isQuickDownload = false,
                        preference = preference,
                        selectedType = Audio,
                        onPreferenceUpdate = {
                            scope.launch { preference = DownloadUtil.DownloadPreferences() }
                        }
                    )
                }
            ) { }
        }
        if (showVideoPresetDialog) {
            var res by remember(preference) { mutableIntStateOf(preference.videoResolution) }
            var format by remember(preference) { mutableIntStateOf(preference.videoFormat) }
            VideoQuickSettingsDialog(
                res,
                format,
                { res = it },
                { format = it },
                onDismissRequest = { showVideoPresetDialog = false },
                onSave = {
                    VIDEO_FORMAT.updateInt(format)
                    VIDEO_QUALITY.updateInt(res)
                    preference = DownloadUtil.DownloadPreferences()
                }
            )
        }
    }
}

@Composable
private fun ConfigurePageImpl(
    modifier: Modifier = Modifier,
    config: Config,
    preference: DownloadUtil.DownloadPreferences,
    settingChips: @Composable () -> Unit,
    onPresetEdit: (DownloadType?) -> Unit = {},
    onPreferenceUpdate: () -> Unit,
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

    LaunchedEffect(selectedType) {
        if (selectedType == Playlist) {
            useFormatSelection = false
        }
    }

    Column {
        Column(modifier = modifier.padding(horizontal = 20.dp)) {
            Header(modifier = Modifier.align(Alignment.CenterHorizontally))
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
            DownloadTypeSelectionGroup(typeEntries = config.typeEntries,
                selectedType = selectedType,
                onSelect = { selectedType = it }
            )
            DrawerSheetSubtitle(
                text = stringResource(id = R.string.format_selection), modifier = Modifier
            )
            Preset(
                modifier = Modifier.animateContentSize(),
                preference = preference,
                selected = !useFormatSelection,
                downloadType = selectedType,
                onClick = { useFormatSelection = false },
                showEditIcon = !useFormatSelection && selectedType != Playlist,
                onEdit = {
                    onPresetEdit(selectedType)
                }
            )
            Custom(
                selected = useFormatSelection,
                enabled = selectedType != Playlist,
                onClick = { useFormatSelection = true }
            )
        }
        var expanded by remember { mutableStateOf(false) }
        ExpandableTitle(expanded = expanded, onClick = { expanded = true }) {
            settingChips()
        }

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


@Composable
private fun AdditionalSettings(
    modifier: Modifier = Modifier,
    isQuickDownload: Boolean,
    selectedType: DownloadType?,
    preference: DownloadUtil.DownloadPreferences,
    onNavigateToCookieGeneratorPage: (String) -> Unit = {},
    onPreferenceUpdate: () -> Unit
) {
    val cookiesProfiles by DatabaseUtil.getCookiesFlow().collectAsStateWithLifecycle(emptyList())
    var showCookiesDialog by rememberSaveable { mutableStateOf(false) }

    with(preference) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            if (cookiesProfiles.isNotEmpty()) {
                VideoFilterChip(
                    selected = preference.cookies,
                    onClick = {
                        if (isQuickDownload) {
                            COOKIES.updateBoolean(!cookies)
                            onPreferenceUpdate()
                        } else {
                            showCookiesDialog = true
                        }
                    },
                    label = stringResource(id = R.string.cookies)
                )
            }

            VideoFilterChip(
                selected = downloadSubtitle,
                enabled = selectedType != Command,
                onClick = {
                    SUBTITLE.updateBoolean(!downloadSubtitle)
                    onPreferenceUpdate()
                },
                label = stringResource(id = R.string.download_subtitles)
            )
            VideoFilterChip(
                selected = createThumbnail,
                enabled = selectedType != Command,
                onClick = {
                    THUMBNAIL.updateBoolean(!createThumbnail)
                    onPreferenceUpdate()
                },
                label = stringResource(R.string.create_thumbnail)
            )
        }

        if (showCookiesDialog && cookiesProfiles.isNotEmpty()) {
            CookiesQuickSettingsDialog(
                onDismissRequest = { showCookiesDialog = false },
                onConfirm = {},
                cookieProfiles = cookiesProfiles,
                onCookieProfileClicked = {
                    onNavigateToCookieGeneratorPage(it.url)
                },
                isCookiesEnabled = cookies,
                onCookiesToggled = {
                    COOKIES.updateBoolean(!cookies)
                    onPreferenceUpdate()
                }
            )
        }
    }
}

@Composable
fun ExpandableTitle(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = Dp.Hairline, modifier = Modifier.padding(horizontal = 20.dp))
        Column(
            modifier = modifier
                .clickable(
                    onClick = onClick,
                    onClickLabel = stringResource(R.string.show_more_actions),
                    enabled = !expanded
                )
                .padding(top = 12.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = stringResource(R.string.additional_settings),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!expanded) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
            AnimatedVisibility(expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    content()
                }
            }
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
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 32.dp),
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
    showEditIcon: Boolean,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    val description = when (downloadType) {
        Audio -> {
            PreferenceStrings.getAudioPresetText(preference)
        }

        Video -> {
            PreferenceStrings.getVideoPresetText(preference)
        }

        Playlist -> stringResource(R.string.preset_format_selection_desc)
        else -> ""
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
            if (showEditIcon) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.edit),
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        onClick = {
            if (showEditIcon) {
                onEdit()
            } else {
                onClick()
            }
        }
    )
}

@Composable
private fun Custom(
    modifier: Modifier = Modifier, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit
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