package com.junkfood

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.home.HomeViewModel
import com.junkfood.seal.util.TextUtil
import com.junkfood.ui.theme.SealTheme


@Composable
fun DownloadPage(
    navController: NavController,
    homeViewModel: HomeViewModel,
    downloadCallback: () -> Unit
) {


    SealTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(9f.dp)
            ) {
                TitleBar(title = context.resources.getString(R.string.app_name)) {
                    navController.navigate("settings") { launchSingleTop = true
                    popUpTo("home")}
                }
                Box(
                    modifier = Modifier
                        .padding(16f.dp)
                        .fillMaxSize()
                )
                {
                    Column {
                        val progress = homeViewModel.progress.observeAsState(0f).value
                        InputUrl(
                            url = homeViewModel.url,
                            hint = context.resources.getString(R.string.video_url)
                        )
                        ProgressBar(progress = progress)
                    }
                    Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                        FABs(downloadCallback = downloadCallback)
                        {
                            TextUtil.readUrlFromClipboard()
                                ?.let { homeViewModel.url.value = it }
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

@Composable
fun ProgressBar(progress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0f.dp, 9f.dp)
    ) {
        LinearProgressIndicator(
            progress = progress / 100f, modifier = Modifier
                .fillMaxWidth(0.75f)
        )
        Text(
            text = "$progress%",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0f.dp, 12f.dp)
        )
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
            .clip(RoundedCornerShape(32f.dp))
            .clickable {
                pasteCallback()
            }, colors = CardDefaults.cardColors(),
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