package com.junkfood.seal.ui.page.download

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.junkfood.seal.ui.component.FormatItem
import com.junkfood.seal.ui.component.FormatSubtitle
import com.junkfood.seal.ui.component.FormatVideoPreview
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.VideoInfo
import com.junkfood.seal.util.connectWithBlank
import com.junkfood.seal.util.toHttpsUrl
import kotlin.math.roundToInt

private const val TAG = "FormatPage"

@Composable
fun FormatPage(downloadViewModel: DownloadViewModel, onBackPressed: () -> Unit = {}) {
    val videoInfo by downloadViewModel.videoInfoFlow.collectAsStateWithLifecycle()
    if (videoInfo.formats.isNullOrEmpty()) return
    FormatPageImpl(videoInfo = videoInfo, onBackPressed = onBackPressed) { formatList ->
        Log.d(TAG, formatList.toString())
        Downloader.downloadVideoWithFormatId(videoInfo, formatList)
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
    onDownloadPressed: (List<Format>) -> Unit = { _ -> }
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
                    onDownloadPressed(formatList)
                }, enabled = isSuggestedFormatSelected || formatList.isNotEmpty()) {
                    Text(text = stringResource(R.string.download))
                }
            })
        }) { paddingValues ->

        LazyVerticalGrid(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(150.dp)
        ) {
            videoInfo.run {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatVideoPreview(
                        title = title,
                        author = uploader ?: channel.toString(),
                        thumbnailUrl = thumbnail.toHttpsUrl(),
                        duration = (duration ?: .0).roundToInt()
                    )
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
                        ),
                        ext = ext,
                        bitRate = tbr?.toFloat() ?: 0f,
                        fileSize = fileSize ?: fileSizeApprox ?: 0,
                        selected = isSuggestedFormatSelected,
                        onLongClick = {}
                    ) {
                        isSuggestedFormatSelected = true
                        selectedAudioOnlyFormat = NOT_SELECTED
                        selectedVideoAudioFormat = NOT_SELECTED
                        selectedAudioOnlyFormat = NOT_SELECTED
                    }
                }
            }

            if (videoAudioFormats.isNotEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                FormatSubtitle(text = stringResource(R.string.video))
            }
            itemsIndexed(videoAudioFormats) { index, formatInfo ->
                FormatItem(
                    formatInfo = formatInfo,
                    selected = selectedVideoAudioFormat == index,
                    onLongClick = { formatInfo.url.share() }
                ) {
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
                FormatItem(
                    formatInfo = formatInfo,
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
                FormatItem(
                    formatInfo = formatInfo,
                    selected = selectedVideoOnlyFormat == index,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    outlineColor = MaterialTheme.colorScheme.tertiary,
                    onLongClick = {
                        formatInfo.url.share()
                    }
                ) {
                    selectedVideoOnlyFormat =
                        if (selectedVideoOnlyFormat == index) NOT_SELECTED else {
                            selectedVideoAudioFormat = NOT_SELECTED
                            isSuggestedFormatSelected = false
                            index
                        }
                }
            }
            if (audioOnlyFormats.isNotEmpty() && videoOnlyFormats.isNotEmpty())
                item(span = { GridItemSpan(maxLineSpan) }) {
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
}
