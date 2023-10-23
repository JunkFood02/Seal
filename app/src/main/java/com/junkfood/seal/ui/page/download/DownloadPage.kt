package com.junkfood.seal.ui.page.download

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.NavigationBarSpacer
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.VideoCard
import com.junkfood.seal.ui.theme.PreviewThemeLight
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DEBUG
import com.junkfood.seal.util.DISABLE_PREVIEW
import com.junkfood.seal.util.NOTIFICATION
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.matchUrlFromClipboard


@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun DownloadPage(
    navigateToSettings: () -> Unit = {},
    navigateToDownloads: () -> Unit = {},
    navigateToPlaylistPage: () -> Unit = {},
    navigateToFormatPage: () -> Unit = {},
    onNavigateToTaskList: () -> Unit = {},
    onNavigateToCookieGeneratorPage: (String) -> Unit = {},
    downloadViewModel: DownloadViewModel = hiltViewModel(),
) {
    val storagePermission = rememberPermissionState(
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) { b: Boolean ->
        if (b) {
            downloadViewModel.startDownloadVideo()
        } else {
            ToastUtil.makeToast(R.string.permission_denied)
        }
    }

    val scope = rememberCoroutineScope()
    val downloaderState by Downloader.downloaderState.collectAsStateWithLifecycle()
    val taskState by Downloader.taskState.collectAsStateWithLifecycle()
    val viewState by downloadViewModel.viewStateFlow.collectAsStateWithLifecycle()
    val playlistInfo by Downloader.playlistResult.collectAsStateWithLifecycle()
    val videoInfo by downloadViewModel.videoInfoFlow.collectAsStateWithLifecycle()
    val errorState by Downloader.errorState.collectAsStateWithLifecycle()
    val processCount by Downloader.processCount.collectAsStateWithLifecycle()

    var showNotificationDialog by remember { mutableStateOf(false) }
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) { isGranted: Boolean ->
            showNotificationDialog = false
            if (!isGranted) {
                ToastUtil.makeToast(R.string.permission_denied)
            }
        }
    } else null

    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val useDialog = LocalWindowWidthState.current != WindowWidthSizeClass.Compact

    val checkPermissionOrDownload = {
        if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted) downloadViewModel.startDownloadVideo()
        else {
            storagePermission.launchPermissionRequest()
        }
    }
    val downloadCallback: () -> Unit = {
        if (NOTIFICATION.getBoolean() && notificationPermission?.status?.isGranted == false) {
            showNotificationDialog = true
        }
        if (CONFIGURE.getBoolean()) downloadViewModel.showDialog(
            scope,
            useDialog
        )
        else checkPermissionOrDownload()
        keyboardController?.hide()
    }
    if (showNotificationDialog) {
        NotificationPermissionDialog(onDismissRequest = {
            showNotificationDialog = false
            NOTIFICATION.updateBoolean(false)
        }, onPermissionGranted = {
            notificationPermission?.launchPermissionRequest()
        })
    }

    DisposableEffect(viewState.showPlaylistSelectionDialog) {
        if (!playlistInfo.entries.isNullOrEmpty() && viewState.showPlaylistSelectionDialog) navigateToPlaylistPage()
        onDispose { downloadViewModel.hidePlaylistDialog() }
    }

    DisposableEffect(viewState.showFormatSelectionPage) {
        if (viewState.showFormatSelectionPage) {
            if (!videoInfo.formats.isNullOrEmpty()) navigateToFormatPage()
        }
        onDispose { downloadViewModel.hideFormatPage() }
    }
    var showOutput by remember {
        mutableStateOf(DEBUG.getBoolean())
    }
    LaunchedEffect(downloaderState) {
        showOutput = PreferenceUtil.getValue(DEBUG) && downloaderState !is Downloader.State.Idle
    }
    if (viewState.isUrlSharingTriggered) {
        downloadViewModel.onShareIntentConsumed()
        downloadCallback()
    }

    BackHandler(viewState.drawerState.targetValue == ModalBottomSheetValue.Expanded) {
        downloadViewModel.hideDialog(scope, useDialog)
    }

    val showVideoCard by remember(downloaderState) {
        mutableStateOf(
            !PreferenceUtil.getValue(DISABLE_PREVIEW)
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DownloadPageImpl(downloaderState = downloaderState,
            taskState = taskState,
            viewState = viewState,
            errorState = errorState,
            downloadCallback = downloadCallback,
            navigateToSettings = navigateToSettings,
            navigateToDownloads = navigateToDownloads,
            onNavigateToTaskList = onNavigateToTaskList,
            processCount = processCount,
            showVideoCard = showVideoCard,
            showOutput = showOutput,
            showDownloadProgress = taskState.taskId.isNotEmpty(),
            pasteCallback = {
                matchUrlFromClipboard(
                    string = clipboardManager.getText().toString(),
                    isMatchingMultiLink = CUSTOM_COMMAND.getBoolean()
                )
                    .let { downloadViewModel.updateUrl(it) }
            },
            cancelCallback = {
                Downloader.cancelDownload()
            },
            onVideoCardClicked = { Downloader.openDownloadResult() },
            onUrlChanged = { url -> downloadViewModel.updateUrl(url) }) {}


        with(viewState) {
            DownloadSettingDialog(useDialog = useDialog,
                dialogState = showDownloadSettingDialog,
                drawerState = drawerState,
                onNavigateToCookieGeneratorPage = onNavigateToCookieGeneratorPage,
                confirm = { checkPermissionOrDownload() },
                hide = { downloadViewModel.hideDialog(scope, useDialog) }
            )
        }
    }

}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun DownloadPageImpl(
    downloaderState: Downloader.State,
    taskState: Downloader.DownloadTaskItem,
    viewState: DownloadViewModel.ViewState,
    errorState: Downloader.ErrorState,
    showVideoCard: Boolean = false,
    showOutput: Boolean = false,
    showDownloadProgress: Boolean = false,
    processCount: Int = 0,
    downloadCallback: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToDownloads: () -> Unit = {},
    onNavigateToTaskList: () -> Unit = {},
    pasteCallback: () -> Unit = {},
    cancelCallback: () -> Unit = {},
    onVideoCardClicked: () -> Unit = {},
    onUrlChanged: (String) -> Unit = {},
    isPreview: Boolean = false,
    content: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val showCancelButton =
        downloaderState is Downloader.State.DownloadingPlaylist || downloaderState is Downloader.State.DownloadingVideo
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {}, modifier = Modifier.padding(horizontal = 8.dp), navigationIcon = {
            TooltipBox(
                state = rememberTooltipState(),
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(text = stringResource(id = R.string.settings))
                    }
                }) {
                IconButton(
                    onClick = { navigateToSettings() },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                }
            }

        }, actions = {
            BadgedBox(badge = {
                if (processCount > 0)
                    Badge(
                        modifier = Modifier.offset(
                            x = (-16).dp,
                            y = (8).dp
                        )
                    ) { Text("$processCount") }
            }) {
                TooltipBox(state = rememberTooltipState(),
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(text = stringResource(id = R.string.running_tasks))
                        }
                    }) {
                    IconButton(
                        onClick = { onNavigateToTaskList() },
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Terminal,
                            contentDescription = stringResource(id = R.string.running_tasks)
                        )
                    }
                }
            }
            TooltipBox(state = rememberTooltipState(),
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(text = stringResource(id = R.string.downloads_history))
                    }
                }) {
                IconButton(
                    onClick = { navigateToDownloads() },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Subscriptions,
                        contentDescription = stringResource(id = R.string.downloads_history)
                    )
                }
            }
        })
    }, floatingActionButton = {
        FABs(
            modifier = with(receiver = Modifier) { if (showDownloadProgress) this else this.imePadding() },
            downloadCallback = downloadCallback,
            pasteCallback = pasteCallback
        )
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TitleWithProgressIndicator(
                showProgressIndicator = downloaderState is Downloader.State.FetchingInfo,
                isDownloadingPlaylist = downloaderState is Downloader.State.DownloadingPlaylist,
                showDownloadText = showCancelButton,
                currentIndex = downloaderState.run { if (this is Downloader.State.DownloadingPlaylist) currentItem else 0 },
                downloadItemCount = downloaderState.run { if (this is Downloader.State.DownloadingPlaylist) itemCount else 0 },
            )


            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
            ) {
                with(taskState) {
                    AnimatedVisibility(
                        visible = showDownloadProgress && showVideoCard
                    ) {
                        Box() {
                            VideoCard(
                                modifier = Modifier,
                                title = title,
                                author = uploader,
                                thumbnailUrl = thumbnailUrl,
                                progress = progress,
                                showCancelButton = downloaderState is Downloader.State.DownloadingPlaylist || downloaderState is Downloader.State.DownloadingVideo,
                                onCancel = cancelCallback,
                                fileSizeApprox = fileSizeApprox,
                                duration = duration,
                                onClick = onVideoCardClicked,
                                isPreview = isPreview
                            )

                        }
                    }
                    InputUrl(
                        url = viewState.url,
                        progress = progress,
                        showDownloadProgress = showDownloadProgress && !showVideoCard,
                        error = errorState.isErrorOccurred(),
                        showCancelButton = showCancelButton && !showVideoCard,
                        onCancel = cancelCallback,
                        onDone = downloadCallback,
                    ) { url -> onUrlChanged(url) }
                    AnimatedVisibility(
                        modifier = Modifier.fillMaxWidth(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                        visible = progressText.isNotEmpty() && showOutput
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 12.dp),
                            text = progressText,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                AnimatedVisibility(visible = errorState.isErrorOccurred()) {
                    ErrorMessage(
                        url = viewState.url,
                        errorMessageResId = errorState.errorMessageResId,
                        errorReport = errorState.errorReport
                    )
                }
                content()
//                val output = Downloader.mutableProcessOutput
//                LazyRow() {
//                    items(output.toList()) { entry ->
//                        TextField(
//                            value = entry.second,
//                            label = { Text(entry.first) },
//                            onValueChange = {},
//                            readOnly = true,
//                            minLines = 10,
//                            maxLines = 10,
//                        )
//                    }
//                }
//                    PreviewFormat()
                NavigationBarSpacer()
                Spacer(modifier = Modifier.height(160.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InputUrl(
    url: String,
    error: Boolean,
    showDownloadProgress: Boolean = false,
    progress: Float,
    onDone: () -> Unit,
    showCancelButton: Boolean,
    onCancel: () -> Unit,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = url,
        isError = error,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.video_url)) },
        modifier = Modifier
            .padding(0f.dp, 16f.dp)
            .fillMaxWidth()
            .focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodyLarge,
        maxLines = 3,
        trailingIcon = {
            if (url.isNotEmpty()) ClearButton { onValueChange("") }
//            else PasteUrlButton { onPaste() }
        }, keyboardActions = KeyboardActions(onDone = {
            softwareKeyboardController?.hide()
            focusManager.moveFocus(FocusDirection.Down)
            onDone()
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
    AnimatedVisibility(visible = showDownloadProgress) {
        Row(
            Modifier.padding(0.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val progressAnimationValue by animateFloatAsState(
                targetValue = progress / 100f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            if (progressAnimationValue < 0) LinearProgressIndicator(
                modifier = Modifier
                    .weight(0.75f)
                    .clip(MaterialTheme.shapes.large),
            )
            else LinearProgressIndicator(
                progress = { progressAnimationValue },
                modifier = Modifier
                    .weight(0.75f)
                    .clip(MaterialTheme.shapes.large),
            )
            Text(
                text = if (progress < 0) "0%" else "$progress%",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.25f)
            )
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = showCancelButton) {
            OutlinedButtonWithIcon(
                onClick = onCancel,
                icon = Icons.Outlined.Cancel,
                text = stringResource(id = R.string.cancel),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleWithProgressIndicator(
    showProgressIndicator: Boolean = true,
    showDownloadText: Boolean = true,
    isDownloadingPlaylist: Boolean = true,
    currentIndex: Int = 1,
    downloadItemCount: Int = 4,
) {
    Column(modifier = Modifier.padding(start = 12.dp, top = 24.dp)) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 3.dp)
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall
            )
            AnimatedVisibility(visible = showProgressIndicator) {
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), strokeWidth = 3.dp
                    )
                }
            }
        }
        AnimatedVisibility(visible = showDownloadText) {
            Text(
                if (isDownloadingPlaylist) stringResource(R.string.playlist_indicator_text).format(
                    currentIndex,
                    downloadItemCount
                )
                else stringResource(R.string.downloading_indicator_text),
                modifier = Modifier.padding(start = 12.dp, top = 3.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorMessage(
    modifier: Modifier = Modifier,
    url: String,
    errorReport: String = "",
    errorMessageResId: Int,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(modifier = modifier
        .fillMaxWidth()
        .run {
            if (errorReport.isNotEmpty()) {
                clip(MaterialTheme.shapes.large).clickable {
                    clipboardManager.setText(AnnotatedString(App.getVersionReport() + "\nURL: $url\n$errorReport"))
                    ToastUtil.makeToastSuspend(context.getString(R.string.error_copied))
                }
            } else this
        }
        .padding(horizontal = 8.dp, vertical = 8.dp)) {
        Icon(
            Icons.Outlined.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error
        )
        Text(
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
            text = errorReport.ifEmpty { stringResource(id = errorMessageResId) },
            color = MaterialTheme.colorScheme.error
        )
    }
}


@Composable
fun FABs(
    modifier: Modifier = Modifier,
    downloadCallback: () -> Unit = {},
    pasteCallback: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(6.dp), horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            onClick = pasteCallback,
            content = {
                Icon(
                    Icons.Outlined.ContentPaste, contentDescription = stringResource(R.string.paste)
                )
            },
            modifier = Modifier.padding(vertical = 12.dp),
        )
        FloatingActionButton(
            onClick = downloadCallback,
            content = {
                Icon(
                    Icons.Outlined.FileDownload,
                    contentDescription = stringResource(R.string.download)
                )
            },
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }

}

@Composable
@Preview
fun DownloadPagePreview() {
    PreviewThemeLight {
        Column() {
            DownloadPageImpl(
                downloaderState = Downloader.State.DownloadingVideo,
                taskState = Downloader.DownloadTaskItem(),
                viewState = DownloadViewModel.ViewState(),
                errorState = Downloader.ErrorState(),
                processCount = 99,
                isPreview = true,
                showDownloadProgress = true,
                showVideoCard = false
            ) {}
        }
    }
}
