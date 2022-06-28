package com.junkfood.seal.ui.page.download

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.color.MaterialColors
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.PLAYLIST
import com.junkfood.seal.util.TextUtil


@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
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

    val checkPermissionOrDownload = {
        if (Build.VERSION.SDK_INT >= 29 || storagePermission.status == PermissionStatus.Granted)
            downloadViewModel.startDownloadVideo()
        else {
            storagePermission.launchPermissionRequest()
        }
    }
    PlaylistSelectionDialog()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        with(viewState.value) {
            BackHandler(drawerState.isVisible) {
                downloadViewModel.hideDrawer(scope)
            }
            Scaffold(floatingActionButton = {
                FABs(
                    with(Modifier.systemBarsPadding()) { if (showVideoCard) this else this.imePadding() },
                    downloadCallback = {
                        if (PreferenceUtil.getValue(PreferenceUtil.CONFIGURE, true))
                            downloadViewModel.showDrawer(scope)
                        else checkPermissionOrDownload()
                    }, pasteCallback = {
                        TextUtil.matchUrlFromClipboard(clipboardManager.getText().toString())
                            ?.let { downloadViewModel.updateUrl(it) }
                    }
                )
            }) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(it)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
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
                    TitleWithProgressIndicator(isProcessing) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        PreferenceUtil.updateInt(
                            PreferenceUtil.WELCOME_DIALOG, 1
                        )
                    }
                    Column(Modifier.padding(24.dp)) {
                        AnimatedVisibility(visible = showVideoCard) {
                            VideoCard(
                                Modifier, videoTitle,
                                videoAuthor,
                                videoThumbnailUrl,
                                progress = progress,
                                playlistIndex = playlistIndex,
                                playlistCount = playlistSize,
                                onClick = { downloadViewModel.openVideoFile() },
                                stopNext = stopNext,
                                onCheckedChange = {
                                    downloadViewModel.stopNext(it)
                                },
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
                                text = progressText,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AnimatedVisibility(visible = isDownloadError) {
                            OutputMessage(
                                error = isDownloadError,
                                copyToClipboard = debugMode || isInCustomCommandMode && url.isNotEmpty(),
                                errorMessage = errorMessage
                            )
                        }
                    }

                }
            }
            DownloadSettingDialog(
                drawerState = drawerState,
                confirm = { checkPermissionOrDownload() }) {
                downloadViewModel.hideDrawer(scope)
            }
        }
    }

}


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
fun TitleWithProgressIndicator(isProcessing: Boolean = true, onLongClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .padding(start = 24.dp, top = 36.dp)
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = onLongClick
            )
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall
        )
        AnimatedVisibility(visible = isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(16.dp), strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun OutputMessage(
    modifier: Modifier = Modifier,
    copyToClipboard: Boolean = false,
    error: Boolean = false,
    errorMessage: String = "",
) {
    if (error and copyToClipboard)
        LocalClipboardManager.current.setText(AnnotatedString(errorMessage))
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
    title: String = "videotitle",
    author: String = "author",
    thumbnailUrl: Any,
    onClick: () -> Unit,
    stopNext: Boolean = false,
    progress: Float = 0f,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    playlistCount: Int = 1,
    playlistIndex: Int = 1
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onClick() }, shape = MaterialTheme.shapes.small
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .padding()
                .fillMaxWidth()
                .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = null, contentScale = ContentScale.FillWidth
        ) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .requiredSize(32.dp)
                    )
                    Text(stringResource(R.string.loading_thumbnail))
                }
            } else {
                SubcomposeAsyncImageContent()
            }
        }
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            AnimatedVisibility(
                visible = PreferenceUtil.getValue(
                    PLAYLIST
                ) && playlistIndex + 1 < playlistCount
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = context.getString(R.string.stop_next) + " [" + (playlistIndex + 1) + "/" + playlistCount + "]",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        overflow = TextOverflow.Ellipsis
                    )
                    Switch(
                        checked = stopNext,
                        onCheckedChange = onCheckedChange
                    )
                }
            }

        }
        val progressAnimationValue by animateFloatAsState(
            targetValue = progress / 100f,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        )

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = progressAnimationValue,
        )

    }
}

fun harmonize(color1: Color, color2: Color): Color {
    return Color(MaterialColors.harmonize(color1.toArgb(), color2.toArgb()))
}

@Composable
fun FABs(
    modifier: Modifier,
    downloadCallback: () -> Unit,
    pasteCallback: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
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
