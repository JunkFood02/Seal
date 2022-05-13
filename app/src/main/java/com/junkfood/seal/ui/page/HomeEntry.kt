package com.junkfood.seal.ui.page

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.ui.animatedComposable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(
) {
    val useDarkTheme = isSystemInDarkTheme()

    val systemUiController = rememberSystemUiController()

    systemUiController.run {
        setStatusBarColor(Color.Transparent, !useDarkTheme)
        setSystemBarsColor(Color.Transparent, !useDarkTheme)
        setNavigationBarColor(Color.Transparent, !useDarkTheme)
    }


    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController = navController, startDestination = "home") {
        animatedComposable("home") {
            DownloadPage(navController = navController)
        }
        animatedComposable("settings") { SettingsPage(navController) }
        animatedComposable("download") { DownloadPreferences(navController) }
        animatedComposable("videolist") { VideoListPage(navController) }
    }

}