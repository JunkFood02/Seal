package com.junkfood.seal.ui.page.download

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
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
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.VIDEO_CLIP
import com.junkfood.seal.util.VideoClip
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.connectWithBlank
import com.junkfood.seal.util.toHttpsUrl
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val TAG = "FormatPage"

@Composable
fun FormatPage(downloadViewModel: DownloadViewModel, onBackPressed: () -> Unit = {}) {
    val videoInfo by downloadViewModel.videoInfoFlow.collectAsStateWithLifecycle()
    if (videoInfo.formats.isNullOrEmpty()) return
    FormatPageImpl(
        videoInfo = videoInfo, onBackPressed = onBackPressed
    ) { formatList, videoClips, splitByChapter, title ->
        Log.d(TAG, formatList.toString())
        Downloader.downloadVideoWithFormatId(
            videoInfo = videoInfo,
            formatList = formatList,
            videoClips = videoClips,
            splitByChapter = splitByChapter,
            newTitle = title
        )
        onBackPressed()
    }
}


private const val NOT_SELECTED = -1

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FormatPageImpl(
    videoInfo: VideoInfo = VideoInfo(),
    onBackPressed: () -> Unit = {},
    onDownloadPressed: (List<Format>, List<VideoClip>, Boolean, String) -> Unit = { _, _, _, _ -> }
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    if (videoInfo.formats.isNullOrEmpty()) return
    val videoOnlyFormats =
        videoInfo.formats.filter { it.vcodec != "none" && it.acodec == "none" }.reversed()
    val audioOnlyFormats =
        videoInfo.formats.filter { it.acodec != "none" && it.vcodec == "none" }.reversed()
    val videoAudioFormats =
        videoInfo.formats.filter { it.acodec != "none" && it.vcodec != "none" }.reversed()

    var isSuggestedFormatSelected by remember { mutableStateOf(true) }
    var selectedVideoAudioFormat by remember { mutableStateOf(NOT_SELECTED) }
    var selectedVideoOnlyFormat by remember { mutableStateOf(NOT_SELECTED) }
    var selectedAudioOnlyFormat by remember { mutableStateOf(NOT_SELECTED) }
    val context = LocalContext.current

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

    val isClippingAvailable = VIDEO_CLIP.getBoolean() && (videoInfo.duration ?: .0) >= 0
    val isSplitByChapterAvailable = !videoInfo.chapters.isNullOrEmpty()

    val videoDuration = 0f..(videoInfo.duration?.toFloat() ?: 0f)
    var showVideoClipDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    var videoClipDuration by remember { mutableStateOf(videoDuration) }
    var videoTitle by remember { mutableStateOf("") }


    LaunchedEffect(isClippingVideo) {
        delay(200)
        videoClipDuration = videoDuration
    }

    val formatList: List<Format> by remember {
        derivedStateOf {
            mutableListOf<Format>().apply {
                audioOnlyFormats.getOrNull(selectedAudioOnlyFormat)?.let { add(it) }
                videoAudioFormats.getOrNull(selectedVideoAudioFormat)?.let { add(it) }
                videoOnlyFormats.getOrNull(selectedVideoOnlyFormat)?.let { add(it) }
            }
        }

    }


    val clipboardManager = LocalClipboardManager.current


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
                        videoTitle
                    )
                }, enabled = isSuggestedFormatSelected || formatList.isNotEmpty()) {
                    Text(text = stringResource(R.string.download))
                }
            })
        }) { paddingValues ->

        LazyVerticalGrid(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(150.dp)
        ) {
            videoInfo.run {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        FormatVideoPreview(
                            title = videoTitle.ifEmpty { title },
                            author = uploader ?: channel.toString(),
                            thumbnailUrl = thumbnail.toHttpsUrl(),
                            duration = (duration ?: .0).roundToInt(),
                            showButton = isClippingAvailable || isSplitByChapterAvailable,
                            isClippingVideo = isClippingVideo,
                            isSplittingVideo = isSplittingVideo,
                            isClippingAvailable = isClippingAvailable,
                            isSplitByChapterAvailable = isSplitByChapterAvailable,
                            onClippingToggled = { isClippingVideo = !isClippingVideo },
                            onSplittingToggled = { isSplittingVideo = !isSplittingVideo },
                            onTitleClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showRenameDialog = true
                            },
                            onImageClicked = {
                                thumbnail.toHttpsUrl().share()
                            },
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        AnimatedVisibility(visible = isClippingVideo) {
                            Column {
                                VideoSelectionSlider(modifier = Modifier.fillMaxWidth(),
                                    value = videoClipDuration,
                                    duration = duration?.roundToInt() ?: 0,
                                    onDiscard = { isClippingVideo = false },
                                    onValueChange = { videoClipDuration = it },
                                    onDurationClick = { showVideoClipDialog = true })
                                HorizontalDivider()
                            }
                        }
                    }

                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
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

                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatSubtitle(text = stringResource(R.string.suggested))
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatItem(
                        formatDesc = format.toString(),
                        resolution = resolution.toString(),
                        codec = connectWithBlank(
                            vcodec.toString().substringBefore("."),
                            acodec.toString().substringBefore(".")
                        ).run { if (isNotBlank()) "($this)" else this },
                        ext = ext,
                        bitRate = tbr?.toFloat() ?: 0f,
                        fileSize = fileSize ?: fileSizeApprox ?: .0,
                        selected = isSuggestedFormatSelected,
                    ) {
                        isSuggestedFormatSelected = true
                        selectedAudioOnlyFormat = NOT_SELECTED
                        selectedVideoAudioFormat = NOT_SELECTED
                        selectedVideoOnlyFormat = NOT_SELECTED
                    }
                }
            }

            if (videoAudioFormats.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                FormatSubtitle(text = stringResource(R.string.video))
            }
            itemsIndexed(videoAudioFormats) { index, formatInfo ->
                FormatItem(formatInfo = formatInfo,
                    selected = selectedVideoAudioFormat == index,
                    onLongClick = { formatInfo.url.share() }) {
                    selectedVideoAudioFormat =
                        if (selectedVideoAudioFormat == index) NOT_SELECTED else {
                            selectedAudioOnlyFormat = NOT_SELECTED
                            selectedVideoOnlyFormat = NOT_SELECTED
                            isSuggestedFormatSelected = false
                            index
                        }
                }
            }

            if (audioOnlyFormats.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                FormatSubtitle(
                    text = stringResource(R.string.audio),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            itemsIndexed(audioOnlyFormats) { index, formatInfo ->
                FormatItem(formatInfo = formatInfo,
                    selected = selectedAudioOnlyFormat == index,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    outlineColor = MaterialTheme.colorScheme.secondary,
                    onLongClick = { formatInfo.url.share() }

                ) {
                    selectedAudioOnlyFormat =
                        if (selectedAudioOnlyFormat == index) NOT_SELECTED else {
                            selectedVideoAudioFormat = NOT_SELECTED
                            isSuggestedFormatSelected = false
                            index
                        }
                }
            }



            if (videoOnlyFormats.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                FormatSubtitle(
                    text = stringResource(R.string.video_only),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            itemsIndexed(videoOnlyFormats) { index, formatInfo ->
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
            if (audioOnlyFormats.isNotEmpty() && videoOnlyFormats.isNotEmpty()) item(span = {
                GridItemSpan(
                    maxLineSpan
                )
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
}

@Composable
fun RenameDialog(initialValue: String, onDismissRequest: () -> Unit, onConfirm: (String) -> Unit) {
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
