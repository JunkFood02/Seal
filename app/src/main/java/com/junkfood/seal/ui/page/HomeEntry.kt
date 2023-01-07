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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.slideInHorizontallyComposable
import com.junkfood.seal.ui.common.slideInVerticallyComposable
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.page.download.FormatPage
import com.junkfood.seal.ui.page.download.PlaylistSelectionPage
import com.junkfood.seal.ui.page.queue.DownloadQueuePage
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.about.kotlin
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.DarkThemePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.command.TemplateListPage
import com.junkfood.seal.ui.page.settings.format.DownloadFormatPreferences
import com.junkfood.seal.ui.page.settings.general.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.general.GeneralDownloadPreferences
import com.junkfood.seal.ui.page.settings.network.CookieProfilePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.settings.network.NetworkPreferences
import com.junkfood.seal.ui.page.settings.network.WebViewPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.YT_DLP
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "HomeEntry"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(
    downloadViewModel: DownloadViewModel,
    isUrlShared: Boolean
) {
    val navController = rememberAnimatedNavController()
    val context = LocalContext.current
    var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
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

    if (isUrlShared) {
        if (navController.currentDestination?.route != Route.HOME) {
            navController.popBackStack(route = Route.HOME, inclusive = false, saveState = true)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val cookiesViewModel: CookiesViewModel = viewModel()
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
            slideInHorizontallyComposable(Route.HOME) {
                DownloadPage(
                    navigateToDownloads = { navController.navigate(Route.DOWNLOADS) },
                    navigateToSettings = { navController.navigate(Route.SETTINGS) },
                    navigateToPlaylistPage = { navController.navigate(Route.PLAYLIST) },
                    navigateToFormatPage = { navController.navigate(Route.FORMAT_SELECTION) },
                    downloadViewModel = downloadViewModel
                )
            }
            slideInHorizontallyComposable(Route.DOWNLOADS) { VideoListPage { onBackPressed() } }
            slideInHorizontallyComposable(Route.DOWNLOAD_QUEUE) { DownloadQueuePage { onBackPressed() } }
            slideInVerticallyComposable(Route.PLAYLIST) { PlaylistSelectionPage { onBackPressed() } }
            slideInVerticallyComposable(Route.FORMAT_SELECTION) { FormatPage(downloadViewModel) { onBackPressed() } }
            settingsGraph(navController, cookiesViewModel)

        }

        WelcomeDialog {
            navController.navigate(Route.SETTINGS)
        }
        LaunchedEffect(Unit) {
            if (!PreferenceUtil.isNetworkAvailableForDownload() || !PreferenceUtil.isAutoUpdateEnabled()
            )
                return@LaunchedEffect
            launch(Dispatchers.IO) {
                kotlin.runCatching {
                    val ytdlpVersion = PreferenceUtil.getString(YT_DLP)
                    val latestYtdlpVersion = UpdateUtil.updateYtDlp()
                    if (ytdlpVersion != latestYtdlpVersion) {
                        TextUtil.makeToastSuspend(context.getString(R.string.yt_dlp_up_to_date) + " ($latestYtdlpVersion)")
                    }
                    val temp = UpdateUtil.checkForUpdate()
                    if (temp != null) {
                        latestRelease = temp
                        showUpdateDialog = true
                    }
                }.onFailure {
                    it.printStackTrace()
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
                            return@launch
                        }
                    }
                },
                releaseNote = latestRelease.body.toString(),
                downloadStatus = currentDownloadStatus
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    cookiesViewModel: CookiesViewModel,
    onBackPressed: () -> Unit = { navController.popBackStack() }
) {
    navigation(startDestination = Route.SETTINGS_PAGE, route = Route.SETTINGS) {
        slideInHorizontallyComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences { onBackPressed() }
        }
        slideInHorizontallyComposable(Route.SETTINGS_PAGE) { SettingsPage(navController) }
        slideInHorizontallyComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
            GeneralDownloadPreferences(
                onBackPressed = { onBackPressed() },
            ) { navController.navigate(Route.TEMPLATE) }
        }
        slideInHorizontallyComposable(Route.DOWNLOAD_FORMAT) { DownloadFormatPreferences { onBackPressed() } }
        slideInHorizontallyComposable(Route.ABOUT) {
            AboutPage(onBackPressed = { onBackPressed() })
            { navController.navigate(Route.CREDITS) }
        }
        slideInHorizontallyComposable(Route.CREDITS) { CreditsPage { onBackPressed() } }
        slideInHorizontallyComposable(Route.APPEARANCE) { AppearancePreferences(navController) }
        slideInHorizontallyComposable(Route.LANGUAGES) { LanguagePage { onBackPressed() } }
        slideInHorizontallyComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences { onBackPressed() }
        }
        slideInHorizontallyComposable(Route.TEMPLATE) { TemplateListPage { onBackPressed() } }
        slideInHorizontallyComposable(Route.DARK_THEME) { DarkThemePreferences { onBackPressed() } }
        slideInHorizontallyComposable(Route.NETWORK_PREFERENCES) {
            NetworkPreferences(navigateToCookieProfilePage = {
                navController.navigate(Route.COOKIE_PROFILE)
            }) { onBackPressed() }
        }
        slideInHorizontallyComposable(Route.COOKIE_PROFILE) {
            CookieProfilePage(
                cookiesViewModel = cookiesViewModel,
                navigateToCookieGeneratorPage = { navController.navigate(Route.COOKIE_GENERATOR_WEBVIEW) },
            ) { onBackPressed() }
        }
        slideInHorizontallyComposable(
            Route.COOKIE_GENERATOR_WEBVIEW
        ) {
            WebViewPage(cookiesViewModel) { onBackPressed() }
        }
    }
}

