package com.junkfood.seal.ui.page

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.viewmodel.DownloadViewModel
import com.junkfood.seal.util.TextUtil


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DownloadPage(
    navController: NavController,
    downloadViewModel: DownloadViewModel,
) {

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val progress = downloadViewModel.progress.observeAsState(0f).value
    val expanded = downloadViewModel.isDownloading.observeAsState(false).value
    val videoTitle = downloadViewModel.videoTitle.observeAsState().value
    val videoThumbnailUrl = downloadViewModel.videoThumbnailUrl.observeAsState().value
    val clipboardManager = LocalClipboardManager.current


    val checkPermission = {
        if (storagePermission.status == PermissionStatus.Granted)
            downloadViewModel.startDownloadVideo()
        else {
            storagePermission.launchPermissionRequest()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(9f.dp)
        ) {
            TitleBar(title = context.getString(R.string.app_name)) {
                navController.navigate("settings") {
                    launchSingleTop = true
                    popUpTo("home")
                }
            }
            Box(
                modifier = Modifier
                    .padding(16f.dp)
                    .fillMaxSize()
            )
            {
                Column {
                    AnimatedVisibility(visible = expanded) {
                        ProgressBar(
                            videoTitle.toString(),
                            videoThumbnailUrl.toString(),
                            progress = progress
                        )
                    }

                    InputUrl(
                        url = downloadViewModel.url,
                        hint = context.getString(R.string.video_url)
                    )

                }
                Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                    FABs(downloadCallback = checkPermission) {
                        TextUtil.matchUrlFromClipboard(clipboardManager.getText().toString())?.let {
                            downloadViewModel.url.value = it
                        }
                    }
                }
            }
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
fun InputUrl(url: MutableLiveData<String>, hint: String) {
    val urlState = url.observeAsState("").value
    OutlinedTextField(
        value = urlState,
        onValueChange = { url.value = it },
        label = { Text(hint) },
        modifier = Modifier
            .padding(0f.dp, 16f.dp)
            .fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressBar(title: String = "videotitle", thumbnailUrl: String, progress: Float = 0f) {
    ElevatedCard(modifier = Modifier
        .fillMaxWidth(), onClick = {}
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .padding()
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailUrl)
                .scale(Scale.FIT)
                .crossfade(true)
                .build(),
            loading = {
                CircularProgressIndicator()
            },
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .padding(12.dp, 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium,
            )
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
}

@Composable
fun TitleBar(title: String, onClick: () -> Unit) {
    LargeTopAppBar(title = {
        Text(
            text = title,
            fontSize = MaterialTheme.typography.displaySmall.fontSize
        )
    }, navigationIcon =
    {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Localized description"
            )
        }

    }
    )
}

@Composable
fun FABs(downloadCallback: () -> Unit, pasteCallback: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlCard(url: String, pasteCallback: (() -> Unit)) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { pasteCallback() }
            .clip(RoundedCornerShape(32f.dp)), colors = CardDefaults.cardColors(),
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