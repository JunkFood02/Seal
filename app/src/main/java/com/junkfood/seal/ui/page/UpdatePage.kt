package com.junkfood.seal.ui.page

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ChangelogTag
import com.junkfood.seal.ui.component.CustomTag
import com.junkfood.seal.ui.component.FilledTonalButtonWithIcon
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.util.TimeUtil
import com.junkfood.seal.util.UpdateUtil
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun UpdatePage(
    onDismissRequest: () -> Unit,
    onConfirmUpdate: () -> Unit,
    latestRelease: UpdateUtil.LatestRelease,
    downloadStatus: UpdateUtil.DownloadStatus,
) {
    val uriHandler = LocalUriHandler.current

    fun openUrl(url: String) {
        uriHandler.openUri(url)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = downloadStatus is UpdateUtil.DownloadStatus.Progress
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        progress = when (downloadStatus) {
                            is UpdateUtil.DownloadStatus.Progress -> downloadStatus.percent.toFloat() / 100f
                            else -> 0f
                        }
                    )
                }
                FilledTonalButtonWithIcon(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onConfirmUpdate,
                    icon = Icons.Outlined.Download,
                    text = stringResource(R.string.update)
                )
                OutlinedButtonWithIcon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    onClick = onDismissRequest,
                    icon = Icons.Outlined.Cancel,
                    text = stringResource(R.string.cancel)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    modifier = Modifier
                        .size(52.dp)
                        .padding(vertical = 6.dp),
                    imageVector = Icons.Outlined.NewReleases,
                    contentDescription = "New release icon for update page",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.update_available),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .alpha(0.8f),
                    text = latestRelease.name.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    ChangelogTag()
                }
                item {
                    CustomTag(
                        text = latestRelease.publishedAt.toString(),
                    )
                }
            }
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()) {
                MarkdownText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    markdown = latestRelease.body.toString(),
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    onLinkClicked = { url ->
                        openUrl(url)
                    }
                )
            }
        }
    }
}


//val imageLoader = ImageLoader.Builder(App.context)
//    .memoryCache {
//        MemoryCache.Builder(App.context)
//            .maxSizePercent(0.25)
//            .build()
//    }
//    .diskCache {
//        DiskCache.Builder()
//            .directory(App.context.cacheDir.resolve("image_cache"))
//            .maxSizePercent(0.02)
//            .build()
//    }
//    .build()


//Thx ChatGPT
val fakeData = UpdateUtil.LatestRelease(
    htmlUrl = "https://github.com/username/repo/releases/tag/v1.0",
    tagName = "v1.0",
    name = "Release 1.0",
    draft = false,
    preRelease = false,
    createdAt = "2023-05-28T10:15:00Z",
    publishedAt = "2023-05-28T12:30:00Z",
    assets = listOf(
        UpdateUtil.AssetsItem(
            name = "app.apk",
            contentType = "application/vnd.android.package-archive",
            size = 1024,
            downloadCount = 100,
            createdAt = "2023-05-28T12:30:00Z",
            updatedAt = "2023-05-28T12:35:00Z",
            browserDownloadUrl = "https://github.com/username/repo/releases/download/v1.0/app.apk"
        ),
        UpdateUtil.AssetsItem(
            name = "changelog.txt",
            contentType = "text/plain",
            size = 512,
            downloadCount = 50,
            createdAt = "2023-05-28T12:32:00Z",
            updatedAt = "2023-05-28T12:34:00Z",
            browserDownloadUrl = "https://github.com/username/repo/releases/download/v1.0/changelog.txt"
        )
    ),
    body = "This is the release description. Here all the text is going to appear"
)

@Preview
@Composable
fun UpdatePagePreview() {
    UpdatePage(
        onDismissRequest = {},
        onConfirmUpdate = {},
        latestRelease = fakeData,
        downloadStatus = UpdateUtil.DownloadStatus.NotYet
    )
}
