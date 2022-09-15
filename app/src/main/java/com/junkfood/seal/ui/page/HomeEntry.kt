package com.junkfood.seal.ui.page

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.about.kotlin
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.download.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.download.DownloadPreferences
import com.junkfood.seal.ui.page.settings.download.TemplateListPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.AUTO_UPDATE
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(
    downloadViewModel: DownloadViewModel,
) {
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current
    var showUpdateDialog by remember { mutableStateOf(false) }
    var currentDownloadStatus by remember { mutableStateOf(UpdateUtil.DownloadStatus.NotYet as UpdateUtil.DownloadStatus) }
    val scope = rememberCoroutineScope()
    var updateJob: Job? = null
    var latestRelease by remember { mutableStateOf(UpdateUtil.LatestRelease()) }

    val settings =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            UpdateUtil.installLatestApk()
        }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            UpdateUtil.installLatestApk()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls())
                    settings.launch(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}"),
                        )
                    )
                else
                    UpdateUtil.installLatestApk()
            }
        }
    }

    val onBackPressed = { navController.popBackStack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedNavHost(
            modifier = Modifier
                .fillMaxWidth(
                    when (LocalWindowWidthState.current) {
                        WindowWidthSizeClass.Compact -> 1f
                        WindowWidthSizeClass.Expanded -> 0.5f
                        else -> 0.8f
                    }
                )
                .align(Alignment.Center),
            navController = navController,
            startDestination = Route.HOME
        ) {
            animatedComposable(Route.HOME) {
                DownloadPage(
                    navController,
                    downloadViewModel
                )
            }
            animatedComposable(Route.SETTINGS) { SettingsPage(navController) }
            animatedComposable(Route.DOWNLOAD_PREFERENCES) {
                DownloadPreferences(
                    onBackPressed = { onBackPressed() },
                    navigateToDownloadDirectory = { navController.navigate(Route.DOWNLOAD_DIRECTORY) }
                ) { navController.navigate(Route.TEMPLATE) }
            }
            animatedComposable(Route.DOWNLOADS) { VideoListPage { onBackPressed() } }
            animatedComposable(Route.ABOUT) {
                AboutPage(onBackPressed = { onBackPressed() })
                { navController.navigate(Route.CREDITS) }
            }
            animatedComposable(Route.CREDITS) { CreditsPage { onBackPressed() } }
            animatedComposable(Route.APPEARANCE) { AppearancePreferences(navController) }
            animatedComposable(Route.LANGUAGES) { LanguagePage { onBackPressed() } }
            animatedComposable(Route.DOWNLOAD_DIRECTORY) {
                DownloadDirectoryPreferences { onBackPressed() }
            }
            animatedComposable(Route.TEMPLATE) { TemplateListPage { onBackPressed() } }
        }

        WelcomeDialog {
            navController.navigate(Route.SETTINGS)
        }
        LaunchedEffect(Unit) {
            if (!PreferenceUtil.getValue(AUTO_UPDATE))
                return@LaunchedEffect
            launch(Dispatchers.IO) {
                try {
                    val temp = UpdateUtil.checkForUpdate()
                    if (temp == null) {
                        TextUtil.makeToastSuspend(context.getString(R.string.app_up_to_date))
                    } else {
                        latestRelease = temp
                        showUpdateDialog = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    TextUtil.makeToastSuspend(context.getString(R.string.app_update_failed))
                    return@launch
                }
            }
        }

        if (showUpdateDialog) {
            UpdateDialog(
                onDismissRequest = {
                    showUpdateDialog = false
                    updateJob?.cancel()
                },
                title = latestRelease.name.toString(),
                onConfirmUpdate = {
                    updateJob = scope.launch(Dispatchers.IO) {
                        kotlin.runCatching {
                            UpdateUtil.downloadApk(latestRelease = latestRelease)
                                .collect { downloadStatus ->
                                    currentDownloadStatus = downloadStatus
                                    if (downloadStatus is UpdateUtil.DownloadStatus.Finished) {
                                        launcher.launch(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                                    }
                                }
                        }.onFailure {
                            it.printStackTrace()
                            currentDownloadStatus = UpdateUtil.DownloadStatus.NotYet
                            TextUtil.makeToastSuspend(context.getString(R.string.app_update_failed))
                        }
                    }
                },
                releaseNote = latestRelease.body.toString(),
                downloadStatus = currentDownloadStatus
            )
        }
    }
}

