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
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.download.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.download.DownloadPreferences
import com.junkfood.seal.ui.page.settings.download.TemplateListPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.seal.util.UpdateUtil

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(
    downloadViewModel: DownloadViewModel,
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
        UpdateDialog {
            //Todo
        }
    }
}
