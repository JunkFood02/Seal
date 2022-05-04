package com.junkfood.seal.ui.page

import android.Manifest
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.junkfood.SettingsPage
import com.junkfood.seal.ui.home.DownloadViewModel
import com.junkfood.ui.animatedComposable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(activityResultLauncher:ActivityResultLauncher<Array<String>> ,downloadViewModel:DownloadViewModel)
{
    Log.d("ComposeInit", "HomeEntry: ")
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController = navController, startDestination = "home") {
        animatedComposable("home") {
            DownloadPage(navController = navController, downloadViewModel = downloadViewModel) {
                activityResultLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }
        animatedComposable("settings") { SettingsPage(navController) }
        animatedComposable("download") { DownloadPreferences(navController) }
    }
    Log.d("ComposeInit", "HomeEntry: Finish")

}