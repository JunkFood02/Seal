package com.junkfood.seal.ui.page.download

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BottomDrawer
import com.junkfood.seal.ui.core.Route
import com.junkfood.seal.ui.page.videolist.FilterChipWithIcon
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CONFIGURE
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.OPEN_IMMEDIATELY
import com.junkfood.seal.util.PreferenceUtil.THUMBNAIL
import com.junkfood.seal.util.TextUtil


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
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
                TextUtil.makeToast(context.resources.getString(R.string.permission_denied))
            }
        }
    val scope = rememberCoroutineScope()
    val viewState = downloadViewModel.viewState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val checkPermissionOrDownload = {
        if (storagePermission.status == PermissionStatus.Granted)
            downloadViewModel.startDownloadVideo()
        else {
            storagePermission.launchPermissionRequest()
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BackHandler(viewState.value.drawerState.isVisible) {
            downloadViewModel.hideDrawer(scope)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FABs(
                Modifier
                    .align(Alignment.BottomEnd)
                    .systemBarsPadding(),
                viewState.value.customCommandMode,
                downloadCallback = {
                    if (PreferenceUtil.getValue(CONFIGURE) and !PreferenceUtil.getValue(
                            CUSTOM_COMMAND
                        )
                    )
                        downloadViewModel.showDrawer(scope)
                    else checkPermissionOrDownload()
                },
                pasteCallback = {
                    TextUtil.matchUrlFromClipboard(clipboardManager.getText().toString())
                        ?.let { downloadViewModel.updateUrl(it) }
                }
            )
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
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
                Row(modifier = Modifier.padding(start = 24.dp, top = 36.dp)) {
                    Text(
                        text = context.getString(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall
                    )
                    AnimatedVisibility(visible = viewState.value.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier
                            .padding(start = 12.dp)
                            .size(16.dp), strokeWidth = 3.dp)
                    }

                }

                with(viewState.value) {
                    Column(
                        Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AnimatedVisibility(visible = showVideoCard) {
                            VideoCard(
                                videoTitle,
                                videoAuthor,
                                videoThumbnailUrl,
                                progress = progress, onClick = { downloadViewModel.openVideoFile() }
                            )
                        }
                        InputUrl(
                            url = url,
                            hint = hintText,
                            progress = progress,
                            showVideoCard = showVideoCard,
                            isInCustomMode = customCommandMode,
                            error = isDownloadError,
                            errorMessage = errorMessage
                        ) { downloadViewModel.updateUrl(it) }
                    }
                }
            }
        }
        DownloadSettingDialog(
            drawerState = viewState.value.drawerState,
            confirm = { checkPermissionOrDownload() }) {
            downloadViewModel.hideDrawer(scope)
        }
    }

}


@Composable
fun SimpleText(text: String) {
    with(MaterialTheme.typography.titleLarge) {
        Text(text = text, fontSize = fontSize, fontWeight = fontWeight)
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
    errorMessage: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = url,
        isError = error,
        onValueChange = onValueChange,
        label = { Text(hint) },
        modifier = Modifier
            .padding(0f.dp, 16f.dp)
            .fillMaxWidth(), textStyle = MaterialTheme.typography.bodyLarge
    )
    AnimatedVisibility(visible = error) {
        Row {
            Icon(
                Icons.Outlined.Error,
                contentDescription = "error",
                tint = MaterialTheme.colorScheme.error
            )
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(
    title: String = "videotitle",
    author: String = "author",
    thumbnailUrl: String,
    onClick: () -> Unit,
    progress: Float = 0f
) {
    ElevatedCard(modifier = Modifier
        .fillMaxWidth(), onClick = { onClick() }
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
            )
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

        }
        val progressAnimationValue by animateFloatAsState(
            targetValue = progress / 100f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            LinearProgressIndicator(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                progress = progressAnimationValue
            )
        }
    }
}


@Composable
fun FABs(
    modifier: Modifier,
    isInCustomMode: Boolean,
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
                    contentDescription = "paste"
                )
            }, modifier = Modifier
                .padding(vertical = 12f.dp)
        )

        FloatingActionButton(
            onClick = downloadCallback,
            content = {
                Icon(
                    Icons.Outlined.FileDownload,
                    contentDescription = "download"
                )
            }, modifier = Modifier
                .padding(vertical = 12f.dp)
        )
    }

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingDialog(
    drawerState: ModalBottomSheetState,
    confirm: () -> Unit,
    cancel: () -> Unit
) {
    var audio by remember { mutableStateOf(PreferenceUtil.getValue(EXTRACT_AUDIO)) }
    var thumbnail by remember { mutableStateOf(PreferenceUtil.getValue(THUMBNAIL)) }
    var open by remember { mutableStateOf(PreferenceUtil.getValue(OPEN_IMMEDIATELY)) }

    BottomDrawer(drawerState = drawerState, sheetContent = {
        Column(Modifier.fillMaxWidth()) {
            Icon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                imageVector = Icons.Outlined.DoneAll,
                contentDescription = stringResource(R.string.settings)
            )
            Text(
                text = stringResource(R.string.settings_before_download),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
            Text(
                text = stringResource(R.string.settings_before_download_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChipWithIcon(
                    select = audio,
                    onClick = { audio = !audio },
                    label = stringResource(R.string.extract_audio)
                )
                FilterChipWithIcon(
                    select = thumbnail,
                    onClick = { thumbnail = !thumbnail },
                    label = stringResource(R.string.create_thumbnail)
                )
                FilterChipWithIcon(
                    select = open,
                    onClick = { open = !open },
                    label = stringResource(R.string.open_when_finish)
                )
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp), horizontalArrangement = Arrangement.End
        ) {

            OutlinedButton(
                modifier = Modifier.padding(horizontal = 12.dp),
                onClick = cancel
            )
            {
                Icon(
                    Icons.Outlined.Cancel,
                    contentDescription = stringResource(R.string.cancel)
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.cancel)
                )
            }

            Button(onClick = {
                PreferenceUtil.updateValue(EXTRACT_AUDIO, audio)
                PreferenceUtil.updateValue(THUMBNAIL, thumbnail)
                PreferenceUtil.updateValue(OPEN_IMMEDIATELY, open)
                cancel()
                confirm()
            }) {
                Icon(
                    Icons.Outlined.DownloadDone,
                    contentDescription = stringResource(R.string.confirm)
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.confirm)
                )
            }
        }
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlCard(url: String, pasteCallback: (() -> Unit)) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { pasteCallback() }
            .clip(RoundedCornerShape(32f.dp)),
        elevation = CardDefaults.cardElevation(),
        shape = RoundedCornerShape(32f.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16f.dp)
        )
        {
            with(MaterialTheme.typography.bodyLarge) {
                Text(text = url, fontSize = fontSize, fontWeight = fontWeight)
            }
        }
    }
}