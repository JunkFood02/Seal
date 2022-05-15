package com.junkfood.seal.ui.page.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(navController: NavController) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    val context = LocalContext.current
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = info.versionName
    val releaseURL = "https://github.com/JunkFood02/Seal/releases/latest"
    val repoUrl = "https://github.com/JunkFood02/Seal"
    val youtubedlAndroidUrl = "https://github.com/yausername/youtubedl-android"
    val ytdlpUrl = "https://github.com/yt-dlp/yt-dlp"
    val readYou = "https://github.com/Ashinch/ReadYou"
    val muscYou = "https://github.com/Kyant0/MusicYou"
    val dvd = "https://github.com/yausername/dvd"
    val icons8 = "https://icons8.com/"
    val materialIcon = "https://fonts.google.com/icons"
    val creditsDialog = remember { mutableStateOf(false) }
    fun openUrl(url: String) {
        context.startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        })
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.about),
                    )
                }, navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    PreferenceItem(
                        title = stringResource(R.string.readme),
                        description = stringResource(R.string.readme_desc),
                        icon = null,
                        enable = true
                    ) { openUrl(repoUrl) }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.version),
                        description = versionName,
                        icon = null,
                        enable = false
                    ) {}
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.release),
                        description = stringResource(R.string.release_desc),
                        icon = null,
                        enable = true
                    ) { openUrl(releaseURL) }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.credits),
                        description = stringResource(id = R.string.credits_desc),
                        icon = null,
                        enable = true
                    ) {
                        creditsDialog.value = true
                    }
                }
            }
        })
    if (creditsDialog.value)
        AlertDialog(
            onDismissRequest = { creditsDialog.value = false },
            title = { Text(stringResource(id = R.string.credits)) },
            text = {
                Column() {
                    TextButton(onClick = { openUrl(youtubedlAndroidUrl) }) { Text(text = "youtubedl-android") }
                    TextButton(onClick = { openUrl(ytdlpUrl) }) { Text(text = "yt-dlp") }
                    TextButton(onClick = { openUrl(readYou) }) { Text(text = "Read You") }
                    TextButton(onClick = { openUrl(muscYou) }) { Text(text = "Music You") }
                    TextButton(onClick = { openUrl(dvd) }) { Text(text = "dvd") }
                    TextButton(onClick = { openUrl(materialIcon) }) { Text("Material Icons") }
                    TextButton(onClick = { openUrl(icons8) }) { Text(text = "App Icon by Icons8.com") }
                }
            }, confirmButton = {
                TextButton(onClick = { creditsDialog.value = false }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            })
}