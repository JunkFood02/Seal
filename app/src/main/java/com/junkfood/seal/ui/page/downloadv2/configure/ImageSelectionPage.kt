package com.junkfood.seal.ui.page.downloadv2.configure

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.ui.component.SealModalBottomSheetM2Variant
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState
import com.junkfood.seal.util.PlaylistResult
import com.junkfood.seal.util.VideoInfo
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionPage(
    state: SelectionState.ImageSelection,
    downloader: DownloaderV2 = koinInject(),
    viewModel: DownloadDialogViewModel = koinInject(),
    onDismissRequest: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
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

    SealModalBottomSheetM2Variant(sheetState = sheetState, sheetGesturesEnabled = false) {
        ImageSelectionPageImpl(
            videoInfo = state.info,
            playlistInfo = state.playlist,
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            onDismissRequest = onBack,
            onConfirmDownload = { urls, quality ->
                scope.launch {
                    if (urls.isEmpty()) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.no_images_available)
                        )
                    } else {
                        val count = downloader.downloadImages(urls, quality)
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.queued_images, count)
                        )
                        onBack()
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionPageImpl(
    videoInfo: VideoInfo?,
    playlistInfo: PlaylistResult?,
    viewModel: DownloadDialogViewModel,
    snackbarHostState: SnackbarHostState,
    onDismissRequest: () -> Unit,
    onConfirmDownload: (List<String>, ImageQuality) -> Unit,
) {
    var downloadThumbnail by remember { mutableStateOf(false) }
    var downloadPlaylistCover by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf(ImageQuality.ORIGINAL) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gather available image URLs
    val thumbnailUrl = videoInfo?.thumbnail
    val playlistCoverUrl = playlistInfo?.entries?.firstOrNull()?.thumbnails?.lastOrNull()?.url

    val hasAnyImages = thumbnailUrl != null || playlistCoverUrl != null

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.select_images_to_download),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Outlined.Close, stringResource(R.string.close))
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {
                            if (!hasAnyImages) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.no_images_available)
                                    )
                                }
                            } else if (downloadThumbnail || downloadPlaylistCover) {
                                showConfirmDialog = true
                            }
                        },
                        enabled = hasAnyImages && (downloadThumbnail || downloadPlaylistCover),
                    ) {
                        Text(text = stringResource(R.string.download))
                    }
                }
            }
        },
    ) { paddings ->
        Column(
            modifier =
                Modifier.padding(paddings)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Header(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                icon = Icons.Outlined.Image,
                title = stringResource(R.string.download_images_title),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Video thumbnail option
            if (thumbnailUrl != null) {
                ImageCheckboxItem(
                    label = stringResource(R.string.video_thumbnail),
                    checked = downloadThumbnail,
                    onCheckedChange = { downloadThumbnail = it },
                )
            }

            // Playlist cover option
            if (playlistCoverUrl != null) {
                ImageCheckboxItem(
                    label = stringResource(R.string.playlist_cover),
                    checked = downloadPlaylistCover,
                    onCheckedChange = { downloadPlaylistCover = it },
                )
            }

            if (hasAnyImages) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Image Quality",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ImageQuality.entries.forEach { quality ->
                    QualityRadioItem(
                        quality = quality,
                        selected = selectedQuality == quality,
                        onSelected = { 
                            selectedQuality = it
                            viewModel.setImageQuality(it)
                        },
                    )
                }
            }

            if (!hasAnyImages) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.no_images_available),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = stringResource(R.string.download_images_title)) },
            text = { Text(text = stringResource(R.string.download_images_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        val selectedUrls = mutableListOf<String>()
                        if (downloadThumbnail && thumbnailUrl != null) selectedUrls.add(thumbnailUrl)
                        if (downloadPlaylistCover && playlistCoverUrl != null)
                            selectedUrls.add(playlistCoverUrl)
                        onConfirmDownload(selectedUrls, selectedQuality)
                    }
                ) {
                    Text(text = stringResource(R.string.download))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ImageCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun QualityRadioItem(
    quality: ImageQuality,
    selected: Boolean,
    onSelected: (ImageQuality) -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelected(quality) }
        )
        Text(
            text = quality.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
