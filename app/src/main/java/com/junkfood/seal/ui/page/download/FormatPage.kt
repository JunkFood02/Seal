package com.junkfood.seal.ui.page.download

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.FormatItem
import com.junkfood.seal.ui.component.FormatSubtitle
import com.junkfood.seal.ui.component.FormatVideoPreview
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SealSearchBar
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.ui.component.VideoFilterChip
import com.junkfood.seal.ui.page.settings.general.DialogCheckBoxItem
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.ui.theme.generateLabelColor
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.MERGE_MULTI_AUDIO_STREAM
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateString
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.SUBTITLE_LANGUAGE
import com.junkfood.seal.util.SubtitleFormat
import com.junkfood.seal.util.VIDEO_CLIP
import com.junkfood.seal.util.VideoClip
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.toHttpsUrl
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "FormatPage"

@Composable
fun FormatPage(downloadViewModel: DownloadViewModel, onBackPressed: () -> Unit = {}) {
    val videoInfo by downloadViewModel.videoInfoFlow.collectAsStateWithLifecycle()
    if (videoInfo.formats.isNullOrEmpty()) return
    val audioOnly = EXTRACT_AUDIO.getBoolean()
    val mergeAudioStream = MERGE_MULTI_AUDIO_STREAM.getBoolean()
    val subtitleLanguageRegex = SUBTITLE_LANGUAGE.getString()
    val downloadSubtitle = SUBTITLE.getBoolean()
    val initialSelectedSubtitles = if (downloadSubtitle) {
        videoInfo.run { subtitles.keys + automaticCaptions.keys }
            .filterWithRegex(subtitleLanguageRegex)
    } else {
        emptySet()
    }

    var showUpdateSubtitleDialog by remember { mutableStateOf(false) }

    var diffSubtitleLanguages by remember { mutableStateOf(emptySet<String>()) }

    FormatPageImpl(
        videoInfo = videoInfo,
        onBackPressed = onBackPressed,
        audioOnly = audioOnly,
        mergeAudioStream = !audioOnly && mergeAudioStream,
        selectedSubtitleCodes = initialSelectedSubtitles,
        isClippingAvailable = VIDEO_CLIP.getBoolean() && (videoInfo.duration ?: .0) >= 0
    ) { formatList, videoClips, splitByChapter, title, selectedSubtitleCodes ->

        diffSubtitleLanguages = selectedSubtitleCodes.run {
            this - this.filterWithRegex(subtitleLanguageRegex)
        }.toSet()

        Downloader.downloadVideoWithConfigurations(
            videoInfo = videoInfo,
            formatList = formatList,
            videoClips = videoClips,
            splitByChapter = splitByChapter,
            newTitle = title,
            selectedSubtitleCodes = selectedSubtitleCodes,
        )

        if (diffSubtitleLanguages.isNotEmpty()) {
            showUpdateSubtitleDialog = true
        } else {
            onBackPressed()
        }
    }

    if (showUpdateSubtitleDialog) {
        UpdateSubtitleLanguageDialog(
            modifier = Modifier,
            languages = diffSubtitleLanguages,
            onDismissRequest = {
                showUpdateSubtitleDialog = false
                onBackPressed()
            },
            onConfirm = {
                SUBTITLE_LANGUAGE.updateString(
                    (diffSubtitleLanguages + subtitleLanguageRegex).joinToString(
                        separator = ",",
                    ) { it })
                showUpdateSubtitleDialog = false
                onBackPressed()
            }
        )
    }

}


private const val NOT_SELECTED = -1

@Preview
@Composable
private fun FormatPagePreview() {
    val captionsMap = mapOf(
        "en-en" to listOf(SubtitleFormat(ext = "", url = "", name = "English from English")),
        "ja-en" to listOf(SubtitleFormat(ext = "", url = "", name = "Japanese from English")),
        "zh-Hans-en" to listOf(
            SubtitleFormat(
                ext = "",
                url = "",
                name = "Chinese (Simplified) from English"
            )
        ),
        "zh-Hant-en" to listOf(
            SubtitleFormat(
                ext = "",
                url = "",
                name = "Chinese (Traditional) from English"
            )
        ),
    )

    val subMap = buildMap {
        put(
            "en", listOf(
                SubtitleFormat(
                    ext = "ass",
                    url = "",
                    name = "English"
                )
            )
        )
        put(
            "ja", listOf(
                SubtitleFormat(
                    ext = "ass",
                    url = "",
                    name = "Japanese"
                )
            )
        )
    }
    val videoInfo =
        VideoInfo(
            formats = buildList {
                repeat(7) {
                    add(Format(formatId = "$it"))
                }
                repeat(7) {
                    add(Format(formatId = "$it", vcodec = "avc1", acodec = "none"))
                }
                repeat(7) {
                    add(Format(formatId = "$it", acodec = "aac", vcodec = "none"))
                }
            },
            subtitles = subMap, automaticCaptions = captionsMap,
            requestedFormats = buildList {
                add(
                    Format(
                        formatId = "616",
                        format = "616 - 1920x1080 (Premium)",
                        acodec = "none",
                        vcodec = "vp09.00.40.08",
                        ext = "webm"
                    )
                )
                add(
                    Format(
                        formatId = "251",
                        format = "251 - audio only (medium)",
                        acodec = "opus",
                        vcodec = "none",
                        ext = "webm"
                    )
                )
            },
            duration = 100000.0
        )
    SealTheme {
        FormatPageImpl(
            videoInfo = videoInfo,
            isClippingAvailable = true,
            mergeAudioStream = true,
            selectedSubtitleCodes = setOf("en", "ja-en")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatPageImpl(
    videoInfo: VideoInfo = VideoInfo(),
    audioOnly: Boolean = false,
    mergeAudioStream: Boolean = false,
    isClippingAvailable: Boolean = false,
    selectedSubtitleCodes: Set<String>,
    onBackPressed: () -> Unit = {},
    onDownloadPressed: (List<Format>, List<VideoClip>, Boolean, String, List<String>) -> Unit = { _, _, _, _, _ -> }
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (videoInfo.formats.isNullOrEmpty()) return
    val videoOnlyFormats =
        videoInfo.formats.filter { it.vcodec != "none" && it.acodec == "none" }.reversed()
    val audioOnlyFormats =
        videoInfo.formats.filter { it.acodec != "none" && it.vcodec == "none" }.reversed()
    val videoAudioFormats =
        videoInfo.formats.filter { it.acodec != "none" && it.vcodec != "none" }.reversed()

    var videoOnlyItemLimit by remember { mutableIntStateOf(6) }
    var audioOnlyItemLimit by remember { mutableIntStateOf(6) }
    var videoAudioItemLimit by remember { mutableIntStateOf(6) }


    var isSuggestedFormatSelected by remember { mutableStateOf(true) }
    var selectedVideoAudioFormat by remember { mutableIntStateOf(NOT_SELECTED) }
    var selectedVideoOnlyFormat by remember { mutableIntStateOf(NOT_SELECTED) }
    val selectedAudioOnlyFormats = remember { mutableStateListOf<Int>() }
    val context = LocalContext.current

    val uriHandler = LocalUriHandler.current
    val hapticFeedback = LocalHapticFeedback.current

    fun String?.share() = this?.let {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        context.startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, it)
        }, null), null)
    }

    var isClippingVideo by remember { mutableStateOf(false) }
    var isSplittingVideo by remember { mutableStateOf(false) }
    val isSplitByChapterAvailable = !videoInfo.chapters.isNullOrEmpty()

    val videoDuration = 0f..(videoInfo.duration?.toFloat() ?: 0f)
    var showVideoClipDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSubtitleSelectionDialog by remember { mutableStateOf(false) }

    var videoClipDuration by remember { mutableStateOf(videoDuration) }
    var videoTitle by remember { mutableStateOf("") }

    val suggestedSubtitleMap: Map<String, List<SubtitleFormat>> =
        videoInfo.subtitles.takeIf { it.isNotEmpty() }
            ?: videoInfo.automaticCaptions.filterKeys { it.endsWith("-orig") }

    val otherSubtitleMap: Map<String, List<SubtitleFormat>> =
        videoInfo.subtitles + videoInfo.automaticCaptions - suggestedSubtitleMap.keys

    LaunchedEffect(isClippingVideo) {
        delay(200)
        videoClipDuration = videoDuration
    }

    val formatList: List<Format> by remember {
        derivedStateOf {
            mutableListOf<Format>().apply {
                selectedAudioOnlyFormats.forEach { index ->
                    add(audioOnlyFormats.elementAt(index))
                }
                videoAudioFormats.getOrNull(selectedVideoAudioFormat)?.let { add(it) }
                videoOnlyFormats.getOrNull(selectedVideoOnlyFormat)?.let { add(it) }
            }
        }

    }


    val selectedLanguageList =
        remember { mutableStateListOf<String>().apply { addAll(selectedSubtitleCodes) } }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.format_selection),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
            }, scrollBehavior = scrollBehavior, navigationIcon = {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
            }, actions = {
                TextButton(onClick = {
                    onDownloadPressed(
                        formatList,
                        if (isClippingVideo) listOf(VideoClip(videoClipDuration)) else emptyList(),
                        isSplittingVideo,
                        videoTitle,
                        selectedLanguageList
                    )
                }, enabled = isSuggestedFormatSelected || formatList.isNotEmpty()) {
                    Text(text = stringResource(R.string.download))
                }
            })
        }) { paddingValues ->

        LazyVerticalGrid(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            videoInfo.run {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatVideoPreview(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = videoTitle.ifEmpty { title },
                        author = uploader ?: channel ?: uploaderId.toString(),
                        thumbnailUrl = thumbnail.toHttpsUrl(),
                        duration = (duration ?: .0).roundToInt(),
                        isClippingVideo = isClippingVideo,
                        isSplittingVideo = isSplittingVideo,
                        isClippingAvailable = isClippingAvailable,
                        isSplitByChapterAvailable = isSplitByChapterAvailable,
                        onClippingToggled = { isClippingVideo = !isClippingVideo },
                        onSplittingToggled = { isSplittingVideo = !isSplittingVideo },
                        onRename = {
                            showRenameDialog = true
                        },
                        onOpenThumbnail = {
                            uriHandler.openUri(thumbnail.toHttpsUrl())
                        },
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    var shouldUpdateClipDuration by remember { mutableStateOf(false) }

                    Column {
                        AnimatedVisibility(visible = isClippingVideo) {
                            Column {
                                val state = remember(isClippingVideo, showVideoClipDialog) {
                                    RangeSliderState(
                                        activeRangeStart = videoClipDuration.start,
                                        activeRangeEnd = videoClipDuration.endInclusive,
                                        valueRange = videoDuration,
                                        onValueChangeFinished = {
                                            shouldUpdateClipDuration = true
                                        }
                                    )
                                }
                                DisposableEffect(shouldUpdateClipDuration) {
                                    videoClipDuration = state.activeRangeStart..state.activeRangeEnd
                                    onDispose {
                                        shouldUpdateClipDuration = false
                                    }
                                }

                                VideoSelectionSlider(
                                    modifier = Modifier.fillMaxWidth(),
                                    state = state,
                                    onDiscard = { isClippingVideo = false },
                                    onDurationClick = { showVideoClipDialog = true },
                                )
                                HorizontalDivider()
                            }
                        }

                        AnimatedVisibility(visible = isSplittingVideo) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.split_video_msg, chapters?.size ?: 0
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButtonWithIcon(
                                        onClick = { isSplittingVideo = false },
                                        icon = Icons.Outlined.Delete,
                                        text = stringResource(id = R.string.discard),
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                }


                if (suggestedSubtitleMap.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                    .padding(top = 12.dp)
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.subtitle_language),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )

                                ClickableTextAction(
                                    visible = true,
                                    text = stringResource(id = androidx.appcompat.R.string.abc_activity_chooser_view_see_all)
                                ) {
                                    showSubtitleSelectionDialog = true
                                }

                            }

                            LazyRow(modifier = Modifier.padding()) {
                                for ((code, formats) in suggestedSubtitleMap) {
                                    item {
                                        VideoFilterChip(
                                            selected = selectedLanguageList.contains(code),
                                            onClick = {
                                                if (selectedLanguageList.contains(code)) {
                                                    selectedLanguageList.remove(code)
                                                } else {
                                                    selectedLanguageList.add(code)
                                                }
                                            },
                                            label = formats.first()
                                                .run { name ?: protocol ?: code })
                                    }
                                }
                            }

                        }

                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .padding(horizontal = 12.dp)
                    ) {
                        FormatSubtitle(text = stringResource(R.string.suggested))
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val onClick = {
                        isSuggestedFormatSelected = true
                        selectedAudioOnlyFormats.clear()
                        selectedVideoAudioFormat = NOT_SELECTED
                        selectedVideoOnlyFormat = NOT_SELECTED
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FormatItem(
                            modifier = Modifier.weight(1f),
                            formatDesc = format.toString(),
                            resolution = resolution.toString(),
                            acodec = acodec,
                            vcodec = vcodec,
                            ext = ext,
                            bitRate = tbr?.toFloat() ?: 0f,
                            fileSize = fileSize ?: fileSizeApprox ?: .0,
                            selected = isSuggestedFormatSelected,
                            onClick = onClick
                        )
                    }
                }
            }

            if (audioOnlyFormats.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    FormatSubtitle(
                        text = stringResource(R.string.audio),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    )

                    ClickableTextAction(
                        visible = audioOnlyItemLimit < audioOnlyFormats.size,
                        text = stringResource(R.string.show_all_items, audioOnlyFormats.size),
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        audioOnlyItemLimit = Int.MAX_VALUE
                    }
                }


            }

            itemsIndexed(
                audioOnlyFormats.subList(
                    fromIndex = 0,
                    toIndex = min(audioOnlyItemLimit, audioOnlyFormats.size)
                )
            ) { index, formatInfo ->
                FormatItem(formatInfo = formatInfo,
                    selected = selectedAudioOnlyFormats.contains(index),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    outlineColor = MaterialTheme.colorScheme.secondary,
                    onLongClick = { formatInfo.url.share() }
                ) {
                    if (selectedAudioOnlyFormats.contains(index)) {
                        selectedAudioOnlyFormats.remove(index)
                    } else {
                        if (!mergeAudioStream) {
                            selectedAudioOnlyFormats.clear()
                        }
                        isSuggestedFormatSelected = false
                        selectedAudioOnlyFormats.add(index)
                    }
                }
            }


            if (!audioOnly) {
                if (videoOnlyFormats.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 12.dp)
                    ) {
                        FormatSubtitle(
                            text = stringResource(R.string.video_only),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )

                        ClickableTextAction(
                            visible = videoOnlyItemLimit < videoOnlyFormats.size,
                            text = stringResource(
                                R.string.show_all_items,
                                videoOnlyFormats.size
                            ),
                        ) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            videoOnlyItemLimit = Int.MAX_VALUE
                        }
                    }
                }
                itemsIndexed(
                    videoOnlyFormats.subList(
                        0,
                        min(videoOnlyItemLimit, videoOnlyFormats.size)
                    )
                ) { index, formatInfo ->
                    FormatItem(formatInfo = formatInfo,
                        selected = selectedVideoOnlyFormat == index,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        outlineColor = MaterialTheme.colorScheme.tertiary,
                        onLongClick = {
                            formatInfo.url.share()
                        }) {
                        selectedVideoOnlyFormat =
                            if (selectedVideoOnlyFormat == index) NOT_SELECTED else {
                                selectedVideoAudioFormat = NOT_SELECTED
                                isSuggestedFormatSelected = false
                                index
                            }
                    }
                }
            }
            if (videoAudioFormats.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 12.dp)
                    ) {
                        FormatSubtitle(
                            text = stringResource(R.string.video),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )
                        ClickableTextAction(
                            visible = videoAudioItemLimit < videoAudioFormats.size,
                            text = stringResource(
                                R.string.show_all_items,
                                videoAudioFormats.size
                            ),
                        ) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            videoAudioItemLimit = Int.MAX_VALUE
                        }
                    }

                }
                itemsIndexed(
                    videoAudioFormats.subList(
                        0,
                        min(videoAudioItemLimit, videoAudioFormats.size)
                    )
                ) { index, formatInfo ->
                    FormatItem(formatInfo = formatInfo,
                        selected = selectedVideoAudioFormat == index,
                        onLongClick = { formatInfo.url.share() }) {
                        selectedVideoAudioFormat =
                            if (selectedVideoAudioFormat == index) NOT_SELECTED else {
                                selectedAudioOnlyFormats.clear()
                                selectedVideoOnlyFormat = NOT_SELECTED
                                isSuggestedFormatSelected = false
                                index
                            }
                    }
                }
            }

            if (!audioOnly && audioOnlyFormats.isNotEmpty() && videoOnlyFormats.isNotEmpty()) item(
                span = {
                    GridItemSpan(maxLineSpan)
                }) {
                PreferenceInfo(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    text = stringResource(R.string.abs_hint),
                    applyPaddings = false
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

        }
    }
    if (showVideoClipDialog) VideoClipDialog(onDismissRequest = { showVideoClipDialog = false },
        initialValue = videoClipDuration,
        valueRange = videoDuration,
        onConfirm = { videoClipDuration = it })

    if (showRenameDialog) RenameDialog(initialValue = videoTitle.ifEmpty { videoInfo.title },
        onDismissRequest = { showRenameDialog = false }) {
        videoTitle = it
    }
    if (showSubtitleSelectionDialog) SubtitleSelectionDialog(
        suggestedSubtitles = suggestedSubtitleMap,
        autoCaptions = otherSubtitleMap,
        selectedSubtitleCodes = selectedLanguageList,
        onDismissRequest = { showSubtitleSelectionDialog = false },
        onConfirm = {
            selectedLanguageList.run {
                clear()
                addAll(it)
            }
            showSubtitleSelectionDialog = false

        }
    )
}

@Composable
private fun RenameDialog(
    initialValue: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var filename by remember { mutableStateOf(initialValue) }
    SealDialog(onDismissRequest = onDismissRequest, confirmButton = {
        ConfirmButton {
            onConfirm(filename)
            onDismissRequest()
        }
    }, dismissButton = {
        DismissButton { onDismissRequest() }
    }, title = { Text(text = stringResource(id = R.string.rename)) }, icon = {
        Icon(
            imageVector = Icons.Outlined.Edit, contentDescription = null
        )
    }, text = {
        Column {
            OutlinedTextField(modifier = Modifier.padding(horizontal = 24.dp),
                value = filename,
                onValueChange = { filename = it },
                label = { Text(text = stringResource(id = R.string.title)) },
                trailingIcon = { if (filename == initialValue) ClearButton { filename = "" } })
        }
    })
}

private fun (Map<String, List<SubtitleFormat>>).filterWithSearchText(searchText: String): Map<String, List<SubtitleFormat>> {
    return this.filter {
        it.run {
            searchText.isBlank()
                    || key.contains(searchText, ignoreCase = true)
                    || value.any { format ->
                format.name?.contains(searchText, ignoreCase = true) ?: false
            }
        }
    }
}

private fun Map<String, List<SubtitleFormat>>.sortedWithSelection(selectedKeys: List<String>): Map<String, List<SubtitleFormat>> {
    return this.toList().sortedWith { entry1, entry2 ->
        when {
            entry1.first in selectedKeys && entry2.first in selectedKeys -> entry1.compareTo(entry2) // Both in selectedKeys - equal priority
            entry1.first in selectedKeys -> -1 // str1 has priority
            entry2.first in selectedKeys -> 1 // str2 has priority
            else -> entry1.compareTo(entry2)
        }
    }.toMap()
}

/**
 * Prioritizes comparison of subtitle names (via `getSubtitleName()`) if available,
 * otherwise compares the `key` portion of the pairs.
 *
 * Examples: `zh` (Chinese) should be greater than `en` (English) according to their names
 */
private fun (Pair<String, List<SubtitleFormat>>).compareTo(
    other: (Pair<String, List<SubtitleFormat>>),
): Int {
    val (key, list) = this
    val (otherKey, otherList) = other

    val name = list.getSubtitleName()
    val otherName = otherList.getSubtitleName()

    return if (name != null && otherName != null) {
        name.compareTo(otherName)
    } else {
        key.compareTo(otherKey)
    }
}

private fun (List<SubtitleFormat>).getSubtitleName(): String? = firstOrNull()?.name

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubtitleSelectionDialog(
    suggestedSubtitles: Map<String, List<SubtitleFormat>>,
    autoCaptions: Map<String, List<SubtitleFormat>>,
    selectedSubtitleCodes: List<String>,
    onDismissRequest: () -> Unit = {},
    onConfirm: (List<String>) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    val selectedSubtitles =
        remember { mutableStateListOf<String>().apply { addAll(selectedSubtitleCodes) } }

    val suggestedSubtitlesFiltered =
        suggestedSubtitles.filterWithSearchText(searchText).sortedWithSelection(selectedSubtitles)
    val autoCaptionsFiltered =
        autoCaptions.filterWithSearchText(searchText).sortedWithSelection(selectedSubtitles)

    SealDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton {
                onConfirm(selectedSubtitles)
            }
        }, dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        },
        title = { Text(text = stringResource(id = R.string.subtitle_language)) },
        icon = { Icon(imageVector = Icons.Outlined.Subtitles, contentDescription = null) },
        text = {
            Column {
                if (autoCaptions.size + suggestedSubtitles.size > 5) {
                    SealSearchBar(
                        text = searchText,
                        placeholderText = stringResource(R.string.search_in_subtitles),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) { searchText = it }
                }

                LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                    if (suggestedSubtitlesFiltered.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.suggested),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )
                        }
                    }
                    for ((code, formats) in suggestedSubtitlesFiltered) {
                        item(key = code) {
                            DialogCheckBoxItem(
                                modifier = Modifier.animateItemPlacement(),
                                checked = selectedSubtitles.contains(code),
                                onClick = {
                                    if (selectedSubtitles.contains(code)) {
                                        selectedSubtitles.remove(code)
                                    } else {
                                        selectedSubtitles.add(code)
                                    }
                                },
                                text = formats.first().run { name ?: protocol ?: code })
                        }
                    }

                    if (autoCaptionsFiltered.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.auto_subtitle),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = 24.dp,
                                    vertical = 12.dp
                                )
                            )
                        }
                        for ((code, formats) in autoCaptionsFiltered) {
                            item(key = code) {
                                DialogCheckBoxItem(
                                    modifier = Modifier.animateItemPlacement(),
                                    checked = selectedSubtitles.contains(code),
                                    onClick = {
                                        if (selectedSubtitles.contains(code)) {
                                            selectedSubtitles.remove(code)
                                        } else {
                                            selectedSubtitles.add(code)
                                        }
                                    },
                                    text = formats.first().run { name ?: protocol ?: code })
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        })
}

@Preview
@Composable
private fun SubtitleSelectionDialogPreview() {
    val captionsMap = mapOf(
        "en-en" to listOf(SubtitleFormat(ext = "", url = "", name = "English from English")),
        "ja-en" to listOf(SubtitleFormat(ext = "", url = "", name = "Japanese from English")),
        "zh-Hans-en" to listOf(
            SubtitleFormat(
                ext = "",
                url = "",
                name = "Chinese (Simplified) from English"
            )
        ),
        "zh-Hant-en" to listOf(
            SubtitleFormat(
                ext = "",
                url = "",
                name = "Chinese (Traditional) from English"
            )
        ),
    )

    val subMap = buildMap {
        put(
            "en", listOf(
                SubtitleFormat(
                    ext = "ass",
                    url = "",
                    name = "English"
                )
            )
        )
        put(
            "ja", listOf(
                SubtitleFormat(
                    ext = "ass",
                    url = "",
                    name = "Japanese"
                )
            )
        )
    }

    SealTheme {
        SubtitleSelectionDialog(
            suggestedSubtitles = subMap,
            autoCaptions = captionsMap,
            selectedSubtitleCodes = listOf()
        )
    }

}

@Composable
private fun ClickableTextAction(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = spring())
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
            modifier = modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp, horizontal = 12.dp)
        )
    }
}

fun <T : Collection<String>> T.filterWithRegex(subtitleLanguageRegex: String): Set<String> {
    val regexGroup = subtitleLanguageRegex.split(',')
    return filter { language ->
        regexGroup.any {
            Regex(it).matchEntire(language) != null
        }
    }.toSet()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun UpdateSubtitleLanguageDialog(
    modifier: Modifier = Modifier,
    languages: Set<String> = setOf("en", "ja"),
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.update_subtitle_languages),
                textAlign = TextAlign.Center
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Subtitles,
                contentDescription = null
            )
        },
        text = {
            Column {
                Text(text = stringResource(R.string.update_language_msg))

                Spacer(modifier = Modifier.height(24.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    languages.forEach {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(16.dp)
                                    .background(
                                        color = it
                                            .hashCode()
                                            .generateLabelColor(), shape = CircleShape
                                    )
                                    .clearAndSetSemantics { }
                            ) {}
                            Text(
                                text = it,
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.okay))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.no_thanks))
            }
        })

}
