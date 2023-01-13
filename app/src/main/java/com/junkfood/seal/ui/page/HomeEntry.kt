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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.common.slideInVerticallyComposable
import com.junkfood.seal.ui.common.toId
import com.junkfood.seal.ui.common.withArg
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
import com.junkfood.seal.ui.page.settings.command.TemplateEditPage
import com.junkfood.seal.ui.page.settings.command.TemplateListPage
import com.junkfood.seal.ui.page.settings.directory.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.format.DownloadFormatPreferences
import com.junkfood.seal.ui.page.settings.format.SubtitlePreference
import com.junkfood.seal.ui.page.settings.general.GeneralDownloadPreferences
import com.junkfood.seal.ui.page.settings.network.CookieProfilePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.settings.network.NetworkPreferences
import com.junkfood.seal.ui.page.settings.network.WebViewPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP
import com.yausername.youtubedl_android.YoutubeDL
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
            animatedComposable(Route.HOME) {
                DownloadPage(
                    navigateToDownloads = { navController.navigate(Route.DOWNLOADS) },
                    navigateToSettings = { navController.navigate(Route.SETTINGS) },
                    navigateToPlaylistPage = { navController.navigate(Route.PLAYLIST) },
                    navigateToFormatPage = { navController.navigate(Route.FORMAT_SELECTION) },
                    downloadViewModel = downloadViewModel
                )
            }
            animatedComposable(Route.DOWNLOADS) { VideoListPage { onBackPressed() } }
//            animatedComposable(Route.DOWNLOAD_QUEUE) { DownloadQueuePage { onBackPressed() } }
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
                    val res = UpdateUtil.updateYtDlp()
                    if (res == YoutubeDL.UpdateStatus.DONE) {
                        TextUtil.makeToastSuspend(context.getString(R.string.yt_dlp_up_to_date) + " (${YT_DLP.getString()})")
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
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences { onBackPressed() }
        }
        animatedComposable(Route.SETTINGS_PAGE) { SettingsPage(navController) }
        animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
            GeneralDownloadPreferences(
                onBackPressed = { onBackPressed() },
            ) { navController.navigate(Route.TEMPLATE) }
        }
        animatedComposable(Route.DOWNLOAD_FORMAT) {
            DownloadFormatPreferences(onBackPressed = onBackPressed) {
                navController.navigate(Route.SUBTITLE_PREFERENCES)
            }
        }
        animatedComposable(Route.SUBTITLE_PREFERENCES) { SubtitlePreference { onBackPressed() } }
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
        animatedComposable(Route.TEMPLATE) {
            TemplateListPage(onBackPressed = onBackPressed) {
                navController.navigate(Route.TEMPLATE_EDIT.toId(it))
            }
        }
        animatedComposable(
            Route.TEMPLATE_EDIT.withArg("templateId"),
            arguments = listOf(navArgument("templateId") { type = NavType.IntType })
        ) {
            TemplateEditPage(onBackPressed, it.arguments?.getInt("templateId") ?: -1)
        }
        animatedComposable(Route.DARK_THEME) { DarkThemePreferences { onBackPressed() } }
        animatedComposable(Route.NETWORK_PREFERENCES) {
            NetworkPreferences(navigateToCookieProfilePage = {
                navController.navigate(Route.COOKIE_PROFILE)
            }) { onBackPressed() }
        }
        animatedComposable(Route.COOKIE_PROFILE) {
            CookieProfilePage(
                cookiesViewModel = cookiesViewModel,
                navigateToCookieGeneratorPage = { navController.navigate(Route.COOKIE_GENERATOR_WEBVIEW) },
            ) { onBackPressed() }
        }
        animatedComposable(
            Route.COOKIE_GENERATOR_WEBVIEW
        ) {
            WebViewPage(cookiesViewModel) { onBackPressed() }
        }
    }
}

