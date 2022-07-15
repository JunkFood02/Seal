package com.junkfood.seal.ui.page

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.junkfood.seal.ui.common.*
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.download.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.download.DownloadPreferences
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.seal.ui.theme.SealTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(
    downloadViewModel: DownloadViewModel,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    SettingsProvider(windowWidthSizeClass) {
        SealTheme(
            darkTheme = LocalDarkTheme.current.isDarkTheme(),
            seedColor = LocalSeedColor.current
        ) {
            val navController = rememberAnimatedNavController()
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
                            onBackPressed = { onBackPressed() }
                        ) { navController.navigate(Route.DOWNLOAD_DIRECTORY) }
                    }
                    animatedComposable(Route.DOWNLOADS) { VideoListPage { onBackPressed() } }
                    animatedComposable(Route.ABOUT) { AboutPage { onBackPressed() } }
                    animatedComposable(Route.APPEARANCE) { AppearancePreferences { onBackPressed() } }
                    animatedComposable(Route.DOWNLOAD_DIRECTORY) {
                        DownloadDirectoryPreferences { onBackPressed() }
                    }
                }
            }
            WelcomeDialog {
                navController.navigate(Route.SETTINGS)
            }
        }
    }
}