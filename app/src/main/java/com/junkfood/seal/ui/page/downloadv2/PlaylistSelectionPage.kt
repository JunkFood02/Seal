package com.junkfood.seal.ui.page.downloadv2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.TaskFactory
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.component.PlaylistItem
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.SealModalBottomSheetM2Variant
import com.junkfood.seal.ui.page.download.PlaylistSelectionDialog
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.SelectionState
import com.junkfood.seal.ui.page.settings.format.AudioQuickSettingsDialog
import com.junkfood.seal.ui.page.settings.format.VideoQuickSettingsDialog
import com.junkfood.seal.util.AUDIO_CONVERSION_FORMAT
import com.junkfood.seal.util.AUDIO_CONVERT
import com.junkfood.seal.util.AUDIO_FORMAT
import com.junkfood.seal.util.AUDIO_QUALITY
import com.junkfood.seal.util.DownloadType.Audio
import com.junkfood.seal.util.DownloadType.Video
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.USE_CUSTOM_AUDIO_PRESET
import com.junkfood.seal.util.VIDEO_FORMAT
import com.junkfood.seal.util.VIDEO_QUALITY
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelectionPage(
    state: SelectionState.PlaylistSelection,
    downloader: DownloaderV2 = koinInject(),
    onDismissRequest: () -> Unit = {},
) {
    var preferences by remember {
        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
    }
    var showVideoPresetDialog by remember { mutableStateOf(false) }
    var showAudioPresetDialog by remember { mutableStateOf(false) }

    var taskList by remember { mutableStateOf(emptyList<TaskFactory.TaskWithState>()) }

    val sheetState =
        androidx.compose.material.rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
        )

    LaunchedEffect(state) { sheetState.show() }
    val scope = rememberCoroutineScope()
    val onBack: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
    }

    BackHandler(onBack = onBack)

    var showConfigurationSheet by remember { mutableStateOf(false) }

    val configureSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SealModalBottomSheetM2Variant(sheetState = sheetState, sheetGesturesEnabled = false) {
        PlaylistSelectionPageImpl(result = state.result, onDismissRequest = onBack) {
            taskList = it
            showConfigurationSheet = true
        }
    }

    val onDismissConfigurationSheet: () -> Unit = {
        scope
            .launch { configureSheetState.hide() }
            .invokeOnCompletion { showConfigurationSheet = false }
    }

    if (showConfigurationSheet) {

        SealModalBottomSheet(
            sheetState = configureSheetState,
            contentPadding = PaddingValues(),
            onDismissRequest = onDismissConfigurationSheet,
        ) {
            ConfigurePagePlaylistVariant(
                modifier = Modifier,
                initialDownloadType = Video,
                preferences = preferences,
                onPreferencesUpdate = { preferences = it },
                onPresetEdit = { type ->
                    when (type) {
                        Audio -> showAudioPresetDialog = true

                        Video -> showVideoPresetDialog = true

                        else -> {}
                    }
                },
                onDismissRequest = onDismissConfigurationSheet,
                onDownload = {
                    val preferences = preferences.copy(extractAudio = it == Audio)
                    taskList
                        .map { it.copy(task = it.task.copy(preferences = preferences)) }
                        .forEach(downloader::enqueue)
                    onDismissConfigurationSheet()
                    onBack()
                },
            )
        }
    }

    if (showVideoPresetDialog) {
        var res by remember(preferences) { mutableIntStateOf(preferences.videoResolution) }
        var format by remember(preferences) { mutableIntStateOf(preferences.videoFormat) }

        VideoQuickSettingsDialog(
            videoResolution = res,
            videoFormatPreference = format,
            onResolutionSelect = { res = it },
            onFormatSelect = { format = it },
            onDismissRequest = { showVideoPresetDialog = false },
            onSave = {
                VIDEO_FORMAT.updateInt(format)
                VIDEO_QUALITY.updateInt(res)
                preferences = DownloadUtil.DownloadPreferences.createFromPreferences()
            },
        )
    }

    if (showAudioPresetDialog) {
        var quality by remember(preferences) { mutableIntStateOf(preferences.audioQuality) }
        var customPreset by
            remember(preferences) { mutableStateOf(preferences.useCustomAudioPreset) }
        var conversionFmt by
            remember(preferences) { mutableIntStateOf(preferences.audioConvertFormat) }
        var convertAudio by remember(preferences) { mutableStateOf(preferences.convertAudio) }
        var preferredFormat by remember(preferences) { mutableIntStateOf(preferences.audioFormat) }

        AudioQuickSettingsDialog(
            modifier = Modifier,
            preferences = preferences,
            audioQuality = quality,
            onQualitySelect = { quality = it },
            useCustomAudioPreset = customPreset,
            onCustomPresetToggle = { customPreset = it },
            convertAudio = convertAudio,
            onConvertToggled = { convertAudio = it },
            conversionFormat = conversionFmt,
            onConversionSelect = { conversionFmt = it },
            preferredFormat = preferredFormat,
            onPreferredSelect = { preferredFormat = it },
            onDismissRequest = { showAudioPresetDialog = false },
            onSave = {
                AUDIO_QUALITY.updateInt(quality)
                USE_CUSTOM_AUDIO_PRESET.updateBoolean(customPreset)
                AUDIO_CONVERSION_FORMAT.updateInt(conversionFmt)
                AUDIO_CONVERT.updateBoolean(convertAudio)
                AUDIO_FORMAT.updateInt(preferredFormat)
                preferences = DownloadUtil.DownloadPreferences.createFromPreferences()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelectionPageImpl(
    result: PlaylistResult,
    onDismissRequest: () -> Unit = {},
    onConfirmSelection: (List<TaskFactory.TaskWithState>) -> Unit,
) {
    val view = LocalView.current

    val selectedItems =
        rememberSaveable(
            saver =
                listSaver<MutableList<Int>, Int>(
                    save = {
                        if (it.isNotEmpty()) {
                            it.toList()
                        } else {
                            emptyList()
                        }
                    },
                    restore = { it.toMutableStateList() },
                )
        ) {
            mutableStateListOf()
        }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showDialog by remember { mutableStateOf(false) }
    val playlistCount = result.entries?.size ?: 0

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            if (selectedItems.isEmpty())
                                stringResource(id = R.string.download_playlist)
                            else
                                stringResource(id = R.string.selected_item_count)
                                    .format(selectedItems.size),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Outlined.Close, stringResource(R.string.close))
                    }
                },
                actions = {
                    TextButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            view.slightHapticFeedback()
                            onConfirmSelection(
                                TaskFactory.createWithPlaylistResult(
                                    playlistUrl =
                                        result.originalUrl ?: result.webpageUrl.toString(),
                                    indexList = selectedItems,
                                    playlistResult = result,
                                    preferences = DownloadUtil.DownloadPreferences.EMPTY,
                                )
                            )
                        },
                        enabled = selectedItems.isNotEmpty(),
                    ) {
                        Text(text = stringResource(R.string.start_download))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.Center,
            ) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier =
                            Modifier.selectable(
                                selected =
                                    selectedItems.size == playlistCount && selectedItems.size != 0,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    view.slightHapticFeedback()
                                    if (selectedItems.size == playlistCount) selectedItems.clear()
                                    else {
                                        selectedItems.clear()
                                        selectedItems.addAll(1..playlistCount)
                                    }
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            modifier = Modifier.padding(16.dp),
                            checked =
                                selectedItems.size == playlistCount && selectedItems.size != 0,
                            onCheckedChange = null,
                        )
                        Text(
                            text = stringResource(R.string.select_all),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = {
                            view.slightHapticFeedback()

                            showDialog = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            contentDescription = stringResource(R.string.download_range_selection),
                        )
                    }
                }
            }
        },
    ) { paddings ->
        Column(modifier = Modifier.padding(paddings)) {
            LazyColumn {
                item {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text =
                            stringResource(R.string.download_selection_desc).format(result.title),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                itemsIndexed(items = result.entries ?: emptyList()) { indexFromZero, entry ->
                    val index = indexFromZero + 1
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(text = entry.title ?: index.toString()) } },
                    ) {
                        PlaylistItem(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            imageModel = entry.thumbnails?.lastOrNull()?.url ?: "",
                            title = entry.title ?: index.toString(),
                            author =
                                entry.channel
                                    ?: entry.uploader
                                    ?: result.channel
                                    ?: result.uploader,
                            selected = selectedItems.contains(index),
                            onClick = {
                                if (selectedItems.contains(index)) selectedItems.remove(index)
                                else selectedItems.add(index)
                            },
                        )
                    }
                }
            }
        }
    }
    if (showDialog) {
        PlaylistSelectionDialog(
            playlistInfo = result,
            onDismissRequest = { showDialog = false },
            onConfirm = {
                selectedItems.clear()
                selectedItems.addAll(it)
            },
        )
    }
}
