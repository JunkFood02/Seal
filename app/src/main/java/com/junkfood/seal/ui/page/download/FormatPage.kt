package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.FormatItem
import com.junkfood.seal.ui.component.FormatSubtitle
import com.junkfood.seal.ui.component.FormatVideoPreview
import com.junkfood.seal.ui.component.connectWithBlank
import com.junkfood.seal.util.VideoInfo

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun FormatPage(downloadViewModel: DownloadViewModel, onBackPressed: () -> Unit = {}) {
    val videoInfo by downloadViewModel.videoInfoFlow.collectAsStateWithLifecycle()
    FormatPageImpl(videoInfo, onBackPressed)
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FormatPageImpl(videoInfo: VideoInfo = VideoInfo(), onBackPressed: () -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    if (videoInfo.formats.isNullOrEmpty()) return
    val videoOnlyFormats = videoInfo.formats.filter { it.vcodec != "none" && it.acodec == "none" }
    val audioOnlyFormats = videoInfo.formats.filter { it.acodec != "none" && it.vcodec == "none" }
    val videoAudioFormats = videoInfo.formats.filter { it.acodec != "none" && it.vcodec != "none" }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = "Format selection",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
            }, scrollBehavior = scrollBehavior, navigationIcon = {
                IconButton(
                    onClick = { onBackPressed() }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
            })
        }) { paddingValues ->
        var selected by remember { mutableStateOf(-1) }

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
                    FormatVideoPreview(
                        title = title,
                        author = uploader ?: channel.toString(),
                        thumbnailUrl = thumbnail.toString()
                    )
                }


                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatSubtitle(text = stringResource(R.string.suggested))
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatItem(
                        formatDesc = format.toString(),
                        resolution = resolution.toString(),
                        codec = connectWithBlank(vcodec.toString(), acodec.toString()),
                        ext = ext,
                        bitRate = tbr?.toFloat() ?: 0f,
                        fileSize = fileSize ?: fileSizeApprox ?: 0,
                        selected = selected == 1
                    ) { selected = 1 }
                }
            }

            if (videoAudioFormats.isNotEmpty())
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatSubtitle(text = stringResource(R.string.video))
                }
            itemsIndexed(videoAudioFormats) { index, formatInfo ->
                FormatItem(
                    formatInfo = formatInfo,
                    selected = selected == index
                ) { selected = index }
            }

            if (audioOnlyFormats.isNotEmpty())
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatSubtitle(
                        text = stringResource(R.string.audio),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            itemsIndexed(audioOnlyFormats) { index, formatInfo ->
                FormatItem(
                    formatInfo = formatInfo,
                    selected = selected == index,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    outlineColor = MaterialTheme.colorScheme.secondary
                ) { selected = index }

            }

            if (videoOnlyFormats.isNotEmpty())
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FormatSubtitle(
                        text = stringResource(R.string.video_only),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            itemsIndexed(videoOnlyFormats) { index, formatInfo ->
                FormatItem(
                    formatInfo = formatInfo,
                    selected = selected == index,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    outlineColor = MaterialTheme.colorScheme.tertiary
                ) { selected = index }
            }


        }
    }
}
