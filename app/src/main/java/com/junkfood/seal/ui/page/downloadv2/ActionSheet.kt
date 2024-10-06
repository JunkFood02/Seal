package com.junkfood.seal.ui.page.downloadv2

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.download.FakeDownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.download.Task.*
import com.junkfood.seal.download.Task.DownloadState.Canceled
import com.junkfood.seal.download.Task.DownloadState.Completed
import com.junkfood.seal.download.Task.DownloadState.Error
import com.junkfood.seal.download.Task.DownloadState.FetchingInfo
import com.junkfood.seal.download.Task.DownloadState.Idle
import com.junkfood.seal.download.Task.DownloadState.ReadyWithInfo
import com.junkfood.seal.download.Task.DownloadState.Running
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.common.HapticFeedback.longPressHapticFeedback
import com.junkfood.seal.ui.common.LocalFixedColorRoles
import com.junkfood.seal.ui.component.ActionSheetItem
import com.junkfood.seal.ui.component.ActionSheetPrimaryButton
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.theme.ErrorTonalPalettes
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.makeToast
import com.junkfood.seal.util.toBitrateText
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toFileSizeText
import com.junkfood.seal.util.toLocalizedString
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
private fun ShareButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.secondaryFixed,
        contentColor = LocalFixedColorRoles.current.onSecondaryFixedVariant,
        imageVector = Icons.Rounded.Share,
        text = stringResource(R.string.share),
        onClick = onClick,
    )
}

@Composable
private fun PlayButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.primaryFixed,
        contentColor = LocalFixedColorRoles.current.onPrimaryFixedVariant,
        imageVector = Icons.Rounded.PlayArrow,
        text = stringResource(R.string.open_file),
        onClick = onClick,
    )
}

@Composable
private fun ResumeButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.tertiaryFixed,
        contentColor = LocalFixedColorRoles.current.onTertiaryFixedVariant,
        imageVector = Icons.Outlined.RestartAlt,
        text = stringResource(R.string.resume),
        onClick = onClick,
    )
}

@Composable
private fun ErrorReportButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = ErrorTonalPalettes.accent1(80.0),
        contentColor = ErrorTonalPalettes.accent1(10.0),
        imageVector = Icons.Outlined.ErrorOutline,
        text = stringResource(R.string.copy_error_report),
        onClick = onClick,
    )
}

@Composable
private fun DeleteButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        imageVector = Icons.Outlined.Delete,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        text = stringResource(R.string.delete),
        onClick = onClick,
    )
}

@Composable
private fun CancelButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        imageVector = Icons.Outlined.Cancel,
        text = stringResource(R.string.cancel),
        onClick = onClick,
    )
}

@Composable
private fun DownloadLogButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = LocalFixedColorRoles.current.secondaryFixed,
        contentColor = LocalFixedColorRoles.current.onSecondaryFixedVariant,
        imageVector = Icons.AutoMirrored.Outlined.TextSnippet,
        text = stringResource(R.string.show_logs),
        onClick = onClick,
    )
}

@Composable
private fun CopyURLButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        imageVector = Icons.Outlined.ContentCopy,
        text = stringResource(R.string.copy_link),
        onClick = onClick,
    )
}

@Composable
private fun OpenVideoURLButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
        text = stringResource(R.string.open_url),
        onClick = onClick,
    )
}

@Composable
private fun OpenThumbnailURLButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    ActionSheetPrimaryButton(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
        imageVector = Icons.Outlined.Image,
        text = stringResource(R.string.thumbnail),
        onClick = onClick,
    )
}

@Composable
fun Title(imageModel: Any?, title: String, author: String, downloadState: DownloadState) {

    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImageImpl(
            model = imageModel,
            modifier =
                Modifier.height(64.dp).aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))

        Column {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = author, style = MaterialTheme.typography.bodySmall)
            }
            val text =
                when (downloadState) {
                    is Canceled -> stringResource(R.string.status_canceled)
                    is Completed -> stringResource(R.string.status_downloaded)
                    is Error -> stringResource(R.string.status_error)
                    is FetchingInfo -> stringResource(R.string.status_fetching_video_info)
                    Idle -> stringResource(R.string.status_enqueued)
                    ReadyWithInfo -> stringResource(R.string.status_enqueued)
                    is Running -> {
                        val progress = downloadState.progress
                        if (progress >= 0) {
                            "%.1f %%".format(downloadState.progress * 100)
                        } else {
                            stringResource(R.string.status_downloading)
                        }
                    }
                }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (downloadState is Error) {
                    Icon(
                        imageVector = Icons.Rounded.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp).size(12.dp),
                    )
                }
                Text(text = text, modifier = Modifier, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SheetContent(
    task: Task,
    viewState: ViewState,
    downloadState: DownloadState,
    onDismissRequest: () -> Unit,
    onActionPost: (Task, DownloadState) -> Unit,
) {

    Column {
        Title(
            imageModel = viewState.thumbnailUrl,
            title = viewState.title,
            author = viewState.uploader,
            downloadState = downloadState,
        )
        LazyRow(
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            ActionButtons(
                task = task,
                downloadState = downloadState,
                viewState = viewState,
                onDismissRequest = onDismissRequest,
                onActionPost = onActionPost,
            )
        }

        ActionSheetInfo(task = task, viewState = viewState)
    }
}

fun LazyListScope.ActionButtons(
    task: Task,
    downloadState: DownloadState,
    viewState: ViewState,
    onDismissRequest: () -> Unit,
    onActionPost: (Task, DownloadState) -> Unit,
) {
    when (downloadState) {
        is Canceled -> {
            item(key = "ResumeButton") {
                ResumeButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, downloadState)
                    onDismissRequest()
                }
            }
        }
        is Completed -> {
            item(key = "PlayButton") {
                PlayButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, downloadState)
                    onDismissRequest()
                }
            }
            item(key = "ShareButton") {
                val context = LocalContext.current
                val shareTitle = stringResource(R.string.share)

                ShareButton(modifier = Modifier.animateItem()) {
                    FileUtil.createIntentForSharingFile(downloadState.filePath)?.runCatching {
                        context.startActivity(Intent.createChooser(this, shareTitle))
                    }
                }
            }
        }
        is Error -> {
            item(key = "ResumeButton") {
                ResumeButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, downloadState)
                    onDismissRequest()
                }
            }
            item(key = "ErrorReportButton") {
                val view = LocalView.current
                val context = LocalContext.current
                val clipboardManager = LocalClipboardManager.current
                ErrorReportButton(modifier = Modifier.animateItem()) {
                    view.longPressHapticFeedback()
                    clipboardManager.setText(
                        AnnotatedString(
                            App.getVersionReport() +
                                "\nURL: ${task.url}\n${downloadState.throwable.message}"
                        )
                    )
                    context.makeToast(R.string.error_copied)
                }
            }
        }
        is FetchingInfo,
        ReadyWithInfo,
        Idle,
        is Running -> {
            /*            if (state is Running) {
                item(key = "DownloadLogButton") {
                    DownloadLogButton(modifier = Modifier.animateItem()) {}
                }
            }*/
            item(key = "CancelButton") {
                CancelButton(modifier = Modifier.animateItem()) {
                    onActionPost(task, downloadState)
                    onDismissRequest()
                }
            }
        }
    }
    if (downloadState is DownloadState.Restartable) {
        item(key = "DeleteButton") { DeleteButton(modifier = Modifier.animateItem()) {} }
    }
    item(key = "CopyURLButton") {
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        CopyURLButton(modifier = Modifier.animateItem()) {
            clipboardManager.setText(AnnotatedString(task.url))
            context.makeToast(R.string.link_copied)
        }
    }
    item(key = "OpenVideoURLButton") {
        val uriHandler = LocalUriHandler.current
        OpenVideoURLButton(modifier = Modifier.animateItem()) { uriHandler.openUri(task.url) }
    }
    if (!viewState.thumbnailUrl.isNullOrEmpty()) {
        item(key = "OpenThumbnailURLButton") {
            val uriHandler = LocalUriHandler.current
            OpenThumbnailURLButton(modifier = Modifier.animateItem()) {
                uriHandler.openUri(viewState.thumbnailUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SheetPreview() {
    val sheetState =
        SheetState(
            density = LocalDensity.current,
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Expanded,
        )

    var downloadState: DownloadState by remember { mutableStateOf(Running(Job(), "", 0.58f)) }

    val fakeStateList =
        listOf(
            Running(Job(), "", 0.58f),
            Error(throwable = Throwable(), RestartableAction.Download),
            FetchingInfo(Job(), ""),
            Canceled(RestartableAction.Download),
            ReadyWithInfo,
            Idle,
            Completed(null),
        )
    LaunchedEffect(Unit) {
        while (true) {
            fakeStateList.forEach {
                downloadState = it
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    val context = LocalContext.current
    val downloader = FakeDownloaderV2
    val scope = rememberCoroutineScope()

    val viewState =
        ViewState(
            title = "https://www.example.com",
            videoFormats =
                listOf(
                    Format(
                        vcodec = "vp9",
                        resolution = "1280x720",
                        vbr = 129400.0,
                        fileSize = 11451400.0,
                    )
                ),
            audioOnlyFormats = listOf(Format(acodec = "mp4a", abr = 129.0, fileSize = 114514.0)),
        )

    SealTheme {
        Surface() {
            SealModalBottomSheet(
                contentPadding = PaddingValues(),
                onDismissRequest = {},
                sheetState = sheetState,
            ) {
                SheetContent(
                    task = Task(url = "https://www.example.com", preferences = PreferencesMock),
                    viewState = viewState,
                    downloadState = downloadState,
                    onDismissRequest = { scope.launch { sheetState.hide() } },
                ) { task, state ->
                    when (state) {
                        is Canceled,
                        is Error -> {
                            downloader.restart(task)
                        }
                        is Completed -> {
                            state.filePath?.let {
                                FileUtil.openFile(it) {
                                    context.makeToast(R.string.file_unavailable)
                                }
                            }
                        }
                        Idle,
                        ReadyWithInfo,
                        is FetchingInfo,
                        is Running -> {
                            downloader.cancel(task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionSheetInfo(modifier: Modifier = Modifier, task: Task, viewState: ViewState) {
    with(viewState) {
        Column(modifier = modifier) {
            HorizontalDivider()
            ActionSheetItem(
                text = {
                    Text(
                        task.timeCreated.toLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "${duration.toDurationText()} · ${fileSizeApprox.toFileSizeText()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null)
                },
            )

            videoFormats?.forEachIndexed { _index, fmt ->
                val index = _index + 1
                val fileSizeText = (fmt.fileSize ?: fmt.fileSizeApprox).toFileSizeText()
                val bitRateText = fmt.vbr.toBitrateText()
                val codecText = fmt.vcodec?.substringBefore(delimiter = ".") ?: ""

                val title = "${stringResource(R.string.video)} #$index: ${fmt.formatNote}"
                val details =
                    listOf(codecText, fmt.resolution, bitRateText, fileSizeText)
                        .joinToString(separator = " · ")

                ActionSheetItem(
                    text = {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        Text(details, style = MaterialTheme.typography.bodySmall)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.VideoFile, contentDescription = null)
                    },
                )
            }

            val audioFormats: List<Format> = buildList {
                videoFormats?.filter { it.containsAudio() }?.let { addAll(it) }
                audioOnlyFormats?.let { addAll(it) }
            }

            audioFormats.forEachIndexed { _index, fmt ->
                val index = _index + 1
                val fileSizeText = (fmt.fileSize ?: fmt.fileSizeApprox).toFileSizeText()
                val bitRateText = fmt.abr.toBitrateText()
                val codecText = fmt.acodec?.substringBefore(delimiter = ".") ?: ""

                val title = "${stringResource(R.string.audio)} #$index: ${fmt.formatNote}"
                val details =
                    listOf(codecText, bitRateText, fileSizeText).joinToString(separator = " · ")

                ActionSheetItem(
                    text = {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        Text(details, style = MaterialTheme.typography.bodySmall)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.AudioFile, contentDescription = null)
                    },
                )
            }

            ActionSheetItem(
                text = {
                    Text(text = extractorKey, style = MaterialTheme.typography.titleSmall)
                    Text(text = url, style = MaterialTheme.typography.bodySmall)
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Link, contentDescription = null) },
            )
        }
    }
}
