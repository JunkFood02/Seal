package com.junkfood.seal.ui.page.download

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.component.NavigationBarSpacer
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.WELCOME_DIALOG
import com.junkfood.seal.util.TextUtil


@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class
)
@Composable
fun DownloadPage(
    navController: NavController,
    downloadViewModel: DownloadViewModel = hiltViewModel(),
) {
    val storagePermission =
        rememberPermissionState(
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) { b: Boolean ->
            if (b) {
                downloadViewModel.startDownloadVideo()
            } else {
                TextUtil.makeToast(R.string.permission_denied)
            }
        }
    val scope = rememberCoroutineScope()
    val viewState = downloadViewModel.stateFlow.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val useDialog = LocalWindowWidthState.current != WindowWidthSizeClass.Compact

    val checkPermissionOrDownload = {
        if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted)
            downloadViewModel.startDownloadVideo()
        else {
            storagePermission.launchPermissionRequest()
        }
    }
    PlaylistSelectionDialog(downloadViewModel = downloadViewModel)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        with(viewState.value) {
            BackHandler(drawerState.isVisible) {
                downloadViewModel.hideDialog(scope, useDialog)
            }
            Scaffold(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                topBar = {
                    SmallTopAppBar(modifier = Modifier.padding(horizontal = 8.dp),
                        title = {},
                        navigationIcon =
                        {
                            IconButton(
                                onClick = { navController.navigate(Route.SETTINGS) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = stringResource(id = R.string.settings)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Route.DOWNLOADS) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Subscriptions,
                                    contentDescription = stringResource(id = R.string.downloads_history)
                                )
                            }

                        })
                },
                floatingActionButton = {
                    FABs(
                        with(Modifier.systemBarsPadding()) { if (showVideoCard) this else this.imePadding() },
                        downloadCallback = {
                            if (PreferenceUtil.getValue(PreferenceUtil.CONFIGURE, true))
                                downloadViewModel.showDialog(scope, useDialog)
                            else checkPermissionOrDownload()
                            keyboardController?.hide()
                        }, pasteCallback = {
                            TextUtil.matchUrlFromClipboard(clipboardManager.getText().toString())
                                ?.let { downloadViewModel.updateUrl(it) }
                        }
                    )
                }) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    TitleWithProgressIndicator(
                        showProgressIndicator = isProcessRunning || isFetchingInfo,
                        showCancelOperation = isProcessRunning,
                        isDownloadingPlaylist = isDownloadingPlaylist,
                        currentIndex = currentIndex,
                        downloadItemCount = downloadItemCount,
                        onClick = {
                            downloadViewModel.cancelDownload()
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onLongClick = {
                            PreferenceUtil.updateInt(WELCOME_DIALOG, 1)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    Column(Modifier.padding(24.dp)) {
                        AnimatedVisibility(visible = showVideoCard) {
                            VideoCard(
                                Modifier, videoTitle,
                                videoAuthor,
                                videoThumbnailUrl,
                                progress = progress,
                                onClick = { downloadViewModel.openVideoFile() },
                            )
                        }

                        InputUrl(
                            url = url,
                            hint = stringResource(R.string.video_url),
                            progress = progress,
                            showVideoCard = showVideoCard,
                            isInCustomMode = isInCustomCommandMode,
                            error = isDownloadError,
                        ) { url -> downloadViewModel.updateUrl(url) }
                        AnimatedVisibility(visible = debugMode && progressText.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(bottom = 12.dp),
                                text = progressText,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AnimatedVisibility(visible = isDownloadError) {
                            ErrorMessage(
                                error = isDownloadError,
                                copyToClipboard = debugMode || isInCustomCommandMode && url.isNotEmpty(),
                                errorMessage = errorMessage
                            )
                        }
                    }
                    NavigationBarSpacer()
                }
            }
            DownloadSettingDialog(
                useDialog = useDialog,
                dialogState = showDownloadSettingDialog,
                drawerState = drawerState,
                confirm = { checkPermissionOrDownload() }) {
                downloadViewModel.hideDialog(scope, useDialog)
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputUrl(
    url: String,
    hint: String,
    error: Boolean,
    isInCustomMode: Boolean = false,
    showVideoCard: Boolean = false,
    progress: Float,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = url,
        isError = error,
        onValueChange = onValueChange,
        label = { Text(hint) },
        modifier = Modifier
            .padding(0f.dp, 16f.dp)
            .fillMaxWidth(), textStyle = MaterialTheme.typography.bodyLarge, maxLines = 3
    )
    AnimatedVisibility(visible = isInCustomMode and !showVideoCard) {
        Row(
            Modifier.padding(0.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val progressAnimationValue by animateFloatAsState(
                targetValue = progress / 100f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            LinearProgressIndicator(
                progress = progressAnimationValue,
                modifier = Modifier.fillMaxWidth(0.75f),
            )
            Text(
                text = "$progress%",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun TitleWithProgressIndicator(
    showProgressIndicator: Boolean = true,
    showCancelOperation: Boolean = true,
    isDownloadingPlaylist: Boolean = true,
    currentIndex: Int = 1,
    downloadItemCount: Int = 4,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier = with(Modifier.padding(start = 12.dp, top = 24.dp)) {
            if (showCancelOperation)
                this.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() } else this.combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongClick
            )
        }
    ) {
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
                    modifier = Modifier
                        .padding(start = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), strokeWidth = 3.dp
                    )
                }
            }
        }
        AnimatedVisibility(visible = showCancelOperation) {
            Text(
                if (isDownloadingPlaylist)
                    stringResource(R.string.playlist_indicator_text)
                        .format(currentIndex, downloadItemCount)
                else
                    stringResource(R.string.downloading_indicator_text),
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
    copyToClipboard: Boolean = false,
    error: Boolean = false,
    errorMessage: String = "",
) {
    val clipboardManager = LocalClipboardManager.current
    if (error && copyToClipboard && clipboardManager.getText() != AnnotatedString(errorMessage))
        clipboardManager.setText(AnnotatedString(errorMessage))
    Row {
        Icon(
            Icons.Outlined.Error, contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
            text = errorMessage,
            color = MaterialTheme.colorScheme.error
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(
    modifier: Modifier = Modifier,
    title: String = "Video title sample text",
    author: String = "Video creator sample text",
    thumbnailUrl: Any = R.drawable.sample,
    onClick: () -> Unit = {},
    progress: Float = 100f,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onClick() }, shape = MaterialTheme.shapes.small
    ) {
        Column {
            AsyncImage(
                modifier = Modifier
                    .padding()
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null, contentScale = ContentScale.FillWidth
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val progressAnimationValue by animateFloatAsState(
                targetValue = progress / 100f,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            if (progress < 0f)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            else
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = progressAnimationValue,
                )
        }
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
                    Icons.Outlined.ContentPaste,
                    contentDescription = stringResource(R.string.paste)
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
            }, modifier = Modifier
                .padding(vertical = 12.dp)
        )
    }

}
