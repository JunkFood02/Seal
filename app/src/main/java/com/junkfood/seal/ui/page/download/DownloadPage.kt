package com.junkfood.seal.ui.page.download

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.ui.common.HapticFeedback.longPressHapticFeedback
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.NavigationBarSpacer
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.VideoCard
import com.junkfood.seal.ui.page.downloadv2.Config
import com.junkfood.seal.ui.page.downloadv2.ConfigureDialog
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.FormatPage
import com.junkfood.seal.ui.theme.PreviewThemeLight
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.CELLULAR_DOWNLOAD
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DEBUG
import com.junkfood.seal.util.DISABLE_PREVIEW
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.NOTIFICATION
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.matchUrlFromClipboard
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadPage(
    navigateToSettings: () -> Unit = {},
    navigateToDownloads: () -> Unit = {},
    navigateToPlaylistPage: () -> Unit = {},
    navigateToFormatPage: () -> Unit = {},
    onNavigateToTaskList: () -> Unit = {},
    onNavigateToCookieGeneratorPage: (String) -> Unit = {},
    downloadViewModel: DownloadViewModel = viewModel(),
    dialogViewModel: DownloadDialogViewModel = viewModel(),
) {

    val scope = rememberCoroutineScope()
    val downloaderState by Downloader.downloaderState.collectAsStateWithLifecycle()
    val taskState by Downloader.taskState.collectAsStateWithLifecycle()
    val viewState by downloadViewModel.viewStateFlow.collectAsStateWithLifecycle()
    val playlistInfo by Downloader.playlistResult.collectAsStateWithLifecycle()
    val videoInfo by downloadViewModel.videoInfoFlow.collectAsStateWithLifecycle()
    val errorState by Downloader.errorState.collectAsStateWithLifecycle()
    val processCount by Downloader.processCount.collectAsStateWithLifecycle()

    var showNotificationDialog by remember { mutableStateOf(false) }
    val notificationPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) {
                isGranted: Boolean ->
                showNotificationDialog = false
                if (!isGranted) {
                    ToastUtil.makeToast(R.string.permission_denied)
                }
            }
        } else null

    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val useDialog = LocalWindowWidthState.current != WindowWidthSizeClass.Compact
    val view = LocalView.current
    var showDownloadDialog by rememberSaveable { mutableStateOf(false) }
    var showMeteredNetworkDialog by remember { mutableStateOf(false) }

    val checkNetworkOrDownload = {
        if (!PreferenceUtil.isNetworkAvailableForDownload()) {
            showMeteredNetworkDialog = true
        } else {
            dialogViewModel.postAction(Action.ShowSheet)
            //            downloadViewModel.startDownloadVideo()
        }
    }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            b: Boolean ->
            if (b) {
                checkNetworkOrDownload()
            } else {
                ToastUtil.makeToast(R.string.permission_denied)
            }
        }

    val checkPermissionOrDownload = {
        if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted) {
            checkNetworkOrDownload()
        } else {
            storagePermission.launchPermissionRequest()
        }
    }

    val downloadCallback: () -> Unit = {
        view.slightHapticFeedback()
        keyboardController?.hide()
        if (NOTIFICATION.getBoolean() && notificationPermission?.status?.isGranted == false) {
            showNotificationDialog = true
        }
        if (CONFIGURE.getBoolean()) {
            showDownloadDialog = true
        } else {
            checkPermissionOrDownload()
        }
    }

    if (showNotificationDialog) {
        NotificationPermissionDialog(
            onDismissRequest = {
                showNotificationDialog = false
                NOTIFICATION.updateBoolean(false)
            },
            onPermissionGranted = { notificationPermission?.launchPermissionRequest() },
        )
    }

    if (showMeteredNetworkDialog) {
        MeteredNetworkDialog(
            onDismissRequest = { showMeteredNetworkDialog = false },
            onAllowOnceConfirm = {
                downloadViewModel.startDownloadVideo()
                showMeteredNetworkDialog = false
            },
            onAllowAlwaysConfirm = {
                downloadViewModel.startDownloadVideo()
                CELLULAR_DOWNLOAD.updateBoolean(true)
                showMeteredNetworkDialog = false
            },
        )
    }

    DisposableEffect(viewState.showPlaylistSelectionDialog) {
        if (!playlistInfo.entries.isNullOrEmpty() && viewState.showPlaylistSelectionDialog)
            navigateToPlaylistPage()
        onDispose { downloadViewModel.hidePlaylistDialog() }
    }

    DisposableEffect(viewState.showFormatSelectionPage) {
        if (viewState.showFormatSelectionPage) {
            if (!videoInfo.formats.isNullOrEmpty()) navigateToFormatPage()
        }
        onDispose { downloadViewModel.hideFormatPage() }
    }
    var showOutput by remember { mutableStateOf(DEBUG.getBoolean()) }
    LaunchedEffect(downloaderState) {
        showOutput = DEBUG.getBoolean() && downloaderState !is Downloader.State.Idle
    }
    if (viewState.isUrlSharingTriggered) {
        downloadViewModel.onShareIntentConsumed()
        downloadCallback()
    }

    val showVideoCard by remember(downloaderState) { mutableStateOf(!DISABLE_PREVIEW.getBoolean()) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        DownloadPageImpl(
            downloaderState = downloaderState,
            taskState = taskState,
            viewState = viewState,
            errorState = errorState,
            downloadCallback = { dialogViewModel.postAction(Action.ShowSheet) },
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
                        isMatchingMultiLink = CUSTOM_COMMAND.getBoolean(),
                    )
                    .let { downloadViewModel.updateUrl(it) }
            },
            cancelCallback = { Downloader.cancelDownload() },
            onVideoCardClicked = { Downloader.openDownloadResult() },
            onUrlChanged = { url -> downloadViewModel.updateUrl(url) },
        ) {
            Column {
                DownloaderV2.taskStateMap.forEach { (task, state) ->
                    Text(task.viewState.toString(), maxLines = 2)
                    Text(state.toString())
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        var preferences by remember {
            mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
        }
        val sheetValue = dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle().value
        val state = dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle().value

        val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value

        var showDialog by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        LaunchedEffect(sheetValue) {
            if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
                showDialog = true
            } else {
                launch { sheetState.hide() }.invokeOnCompletion { showDialog = false }
            }
        }

        if (showDialog) {

            ConfigureDialog(
                url = viewState.url,
                state = state,
                sheetState = sheetState,
                config = Config(),
                preferences = preferences,
                onPreferencesUpdate = { preferences = it },
                onActionPost = { dialogViewModel.postAction(it) },
            )
        }
        when (selectionState) {
            is DownloadDialogViewModel.SelectionState.FormatSelection ->
                FormatPage(
                    state = selectionState,
                    onDismissRequest = { dialogViewModel.postAction(Action.Reset) })
            else -> {}
        }
        DownloadSettingDialog(
            useDialog = useDialog,
            showDialog = showDownloadDialog,
            onNavigateToCookieGeneratorPage = onNavigateToCookieGeneratorPage,
            onDownloadConfirm = { checkPermissionOrDownload() },
            onDismissRequest = { showDownloadDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current

    val showCancelButton =
        downloaderState is Downloader.State.DownloadingPlaylist ||
            downloaderState is Downloader.State.DownloadingVideo
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                modifier = Modifier.padding(horizontal = 8.dp),
                navigationIcon = {
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text(text = stringResource(id = R.string.settings)) }
                        },
                    ) {
                        IconButton(
                            onClick = {
                                view.slightHapticFeedback()
                                navigateToSettings()
                            },
                            modifier = Modifier,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(id = R.string.settings),
                            )
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (processCount > 0)
                                Badge(modifier = Modifier.offset(x = (-16).dp, y = (8).dp)) {
                                    Text("$processCount")
                                }
                        }) {
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider =
                                    TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = {
                                    PlainTooltip {
                                        Text(text = stringResource(id = R.string.running_tasks))
                                    }
                                },
                            ) {
                                IconButton(
                                    onClick = {
                                        view.slightHapticFeedback()
                                        onNavigateToTaskList()
                                    },
                                    modifier = Modifier,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Terminal,
                                        contentDescription =
                                            stringResource(id = R.string.running_tasks),
                                    )
                                }
                            }
                        }
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text(text = stringResource(id = R.string.downloads_history))
                            }
                        },
                    ) {
                        IconButton(
                            onClick = {
                                view.slightHapticFeedback()
                                navigateToDownloads()
                            },
                            modifier = Modifier,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Subscriptions,
                                contentDescription =
                                    stringResource(id = R.string.downloads_history),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FABs(
                modifier =
                    with(receiver = Modifier) {
                        if (showDownloadProgress) this else this.imePadding()
                    },
                downloadCallback = downloadCallback,
                pasteCallback = pasteCallback,
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize().verticalScroll(rememberScrollState())) {
                TitleWithProgressIndicator(
                    showProgressIndicator = downloaderState is Downloader.State.FetchingInfo,
                    isDownloadingPlaylist = downloaderState is Downloader.State.DownloadingPlaylist,
                    showDownloadText = showCancelButton,
                    currentIndex =
                        downloaderState.run {
                            if (this is Downloader.State.DownloadingPlaylist) currentItem else 0
                        },
                    downloadItemCount =
                        downloaderState.run {
                            if (this is Downloader.State.DownloadingPlaylist) itemCount else 0
                        },
                )

                Column(Modifier.padding(horizontal = 24.dp).padding(top = 24.dp)) {
                    with(taskState) {
                        AnimatedVisibility(visible = showDownloadProgress && showVideoCard) {
                            Box() {
                                VideoCard(
                                    modifier = Modifier,
                                    title = title,
                                    author = uploader,
                                    thumbnailUrl = thumbnailUrl,
                                    progress = progress,
                                    showCancelButton =
                                        downloaderState is Downloader.State.DownloadingPlaylist ||
                                            downloaderState is Downloader.State.DownloadingVideo,
                                    onCancel = cancelCallback,
                                    fileSizeApprox = fileSizeApprox,
                                    duration = duration,
                                    onClick = onVideoCardClicked,
                                    isPreview = isPreview,
                                )
                            }
                        }
                        InputUrl(
                            url = viewState.url,
                            progress = progress,
                            showDownloadProgress = showDownloadProgress && !showVideoCard,
                            error = errorState != Downloader.ErrorState.None,
                            showCancelButton = showCancelButton && !showVideoCard,
                            onCancel = cancelCallback,
                            onDone = downloadCallback,
                        ) { url ->
                            onUrlChanged(url)
                        }
                        AnimatedVisibility(
                            modifier = Modifier.fillMaxWidth(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                            visible = progressText.isNotEmpty() && showOutput,
                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = 12.dp),
                                text = progressText,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    AnimatedVisibility(visible = errorState != Downloader.ErrorState.None) {
                        ErrorMessage(title = errorState.title, errorReport = errorState.report) {
                            view.longPressHapticFeedback()
                            clipboardManager.setText(
                                AnnotatedString(
                                    App.getVersionReport() +
                                        "\nURL: ${errorState.url}\n${errorState.report}"))
                            ToastUtil.makeToast(R.string.error_copied)
                        }
                    }
                    content()
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
    onValueChange: (String) -> Unit,
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = url,
        isError = error,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.video_url)) },
        modifier = Modifier.padding(0f.dp, 16f.dp).fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        maxLines = 3,
        trailingIcon = {
            if (url.isNotEmpty()) ClearButton { onValueChange("") }
            //            else PasteUrlButton { onPaste() }
        },
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    )
    AnimatedVisibility(visible = showDownloadProgress) {
        Row(Modifier.padding(0.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val progressAnimationValue by
                animateFloatAsState(
                    targetValue = progress / 100f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                )
            if (progressAnimationValue < 0)
                LinearProgressIndicator(
                    modifier = Modifier.weight(0.75f).clip(MaterialTheme.shapes.large))
            else
                LinearProgressIndicator(
                    progress = { progressAnimationValue },
                    modifier = Modifier.weight(0.75f).clip(MaterialTheme.shapes.large),
                )
            Text(
                text = if (progress < 0) "0%" else "$progress%",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.25f),
            )
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(visible = showCancelButton) {
            OutlinedButtonWithIcon(
                onClick = onCancel,
                icon = Icons.Outlined.Cancel,
                text = stringResource(id = R.string.cancel),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            modifier =
                Modifier.clip(MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 3.dp)) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                )
                AnimatedVisibility(visible = showProgressIndicator) {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp), strokeWidth = 3.dp)
                    }
                }
            }
        AnimatedVisibility(visible = showDownloadText) {
            Text(
                if (isDownloadingPlaylist)
                    stringResource(R.string.playlist_indicator_text)
                        .format(currentIndex, downloadItemCount)
                else stringResource(R.string.downloading_indicator_text),
                modifier = Modifier.padding(start = 12.dp, top = 3.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ErrorMessage(
    modifier: Modifier = Modifier,
    title: String,
    errorReport: String,
    onButtonClicked: () -> Unit = {},
) {
    val view = LocalView.current
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.padding(vertical = 16.dp),
    ) {
        Column(
            modifier =
                Modifier.animateContentSize().padding(horizontal = 12.dp, vertical = 16.dp)) {
                Row(
                    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier,
                            text = title,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var isExpanded by remember { mutableStateOf(false) }

                Text(
                    text = errorReport,
                    style =
                        MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 8,
                    modifier =
                        Modifier.clip(MaterialTheme.shapes.small)
                            .clickable(
                                enabled = !isExpanded,
                                onClickLabel = stringResource(id = R.string.expand),
                                onClick = {
                                    view.slightHapticFeedback()
                                    isExpanded = true
                                },
                            )
                            .padding(4.dp),
                    onTextLayout = { isExpanded = !it.hasVisualOverflow },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(
                        onClick = onButtonClicked,
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text(text = stringResource(id = R.string.copy_error_report))
                    }
                }
            }
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    SealTheme {
        Surface {
            LazyColumn {
                item {
                    ErrorMessage(
                        title = stringResource(id = R.string.download_error_msg),
                        errorReport = ERROR_REPORT_SAMPLE,
                    ) {}
                }
            }
        }
    }
}

@Composable
fun FABs(
    modifier: Modifier = Modifier,
    downloadCallback: () -> Unit = {},
    pasteCallback: () -> Unit = {},
) {
    Column(modifier = modifier.padding(6.dp), horizontalAlignment = Alignment.End) {
        FloatingActionButton(
            onClick = pasteCallback,
            content = {
                Icon(
                    Icons.Outlined.ContentPaste,
                    contentDescription = stringResource(R.string.paste))
            },
            modifier = Modifier.padding(vertical = 12.dp),
        )
        FloatingActionButton(
            onClick = downloadCallback,
            content = {
                Icon(
                    Icons.Outlined.FileDownload,
                    contentDescription = stringResource(R.string.download))
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
                errorState =
                    Downloader.ErrorState.DownloadError(url = "", report = ERROR_REPORT_SAMPLE),
                processCount = 99,
                isPreview = true,
                showDownloadProgress = true,
                showVideoCard = false,
            ) {}
        }
    }
}

private const val ERROR_REPORT_SAMPLE =
    """[sample] Extracting URL: https://www.example.com
[sample] sample: Downloading webpage
[sample] sample: Downloading android player API JSON
[info] Available automatic captions for sample:
[info] Available automatic captions for sample:
[sample] sample: Downloading android player API JSON
[info] Available automatic captions for sample:
[info] Available automatic captions for sample:"""
