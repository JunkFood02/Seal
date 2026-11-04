package com.junkfood.seal.ui.page

import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.common.animatedComposableVariant
import com.junkfood.seal.ui.common.arg
import com.junkfood.seal.ui.common.id
import com.junkfood.seal.ui.common.slideInVerticallyComposable
import com.junkfood.seal.ui.page.command.TaskListPage
import com.junkfood.seal.ui.page.command.TaskLogPage
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.DownloadPageV2
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.about.SponsorsPage
import com.junkfood.seal.ui.page.settings.about.UpdatePage
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.DarkThemePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.command.TemplateEditPage
import com.junkfood.seal.ui.page.settings.command.TemplateListPage
import com.junkfood.seal.ui.page.settings.directory.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.format.DownloadFormatPreferences
import com.junkfood.seal.ui.page.settings.format.SubtitlePreference
import com.junkfood.seal.ui.page.settings.general.GeneralDownloadPreferences
import com.junkfood.seal.ui.page.settings.interaction.InteractionPreferencePage
import com.junkfood.seal.ui.page.settings.network.CookieProfilePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.settings.network.NetworkPreferences
import com.junkfood.seal.ui.page.settings.network.WebViewPage
import com.junkfood.seal.ui.page.settings.troubleshooting.TroubleShootingPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val TAG = "HomeEntry"

private val TopDestinations =
    listOf(Route.HOME, Route.TASK_LIST, Route.SETTINGS_PAGE, Route.DOWNLOADS)

@Composable
fun AppEntry(dialogViewModel: DownloadDialogViewModel) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val view = LocalView.current
    val windowWidth = LocalWindowWidthState.current
    val sheetState by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()
    val cookiesViewModel: CookiesViewModel = koinViewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val versionReport = App.packageInfo.versionName.toString()
    val appName = stringResource(R.string.app_name)
    val scope = rememberCoroutineScope()

    val onNavigateBack: () -> Unit = {
        with(navController) {
            if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                popBackStack()
            }
        }
    }

    if (sheetState is DownloadDialogViewModel.SheetState.Configure) {
        if (navController.currentDestination?.route != Route.HOME) {
            navController.popBackStack(route = Route.HOME, inclusive = false, saveState = true)
        }
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var currentTopDestination by rememberSaveable { mutableStateOf(currentRoute) }

    LaunchedEffect(currentRoute) {
        if (currentRoute in TopDestinations) {
            currentTopDestination = currentRoute
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavigationDrawer(
            windowWidth = windowWidth,
            drawerState = drawerState,
            currentRoute = currentRoute,
            currentTopDestination = currentTopDestination,
            showQuickSettings = true,
            gesturesEnabled = currentRoute == Route.HOME,
            onDismissRequest = { drawerState.close() },
            onNavigateToRoute = {
                if (currentRoute != it) {
                    navController.navigate(it) {
                        launchSingleTop = true
                        popUpTo(route = Route.HOME)
                    }
                }
            },
            footer = {
                Text(
                    appName + "\n" + versionReport + "\n" + currentRoute,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp),
                )
            },
        ) {
            NavHost(
                modifier = Modifier.align(Alignment.Center),
                navController = navController,
                startDestination = Route.HOME,
            ) {
                animatedComposable(Route.HOME) {
                    DownloadPageV2(
                        dialogViewModel = dialogViewModel,
                        onMenuOpen = {
                            view.slightHapticFeedback()
                            scope.launch { drawerState.open() }
                        },
                    )
                }
                animatedComposable(Route.DOWNLOADS) { VideoListPage { onNavigateBack() } }
                // [SIMPLIFIED] disabled (commented out, not deleted): custom command task list route
                // animatedComposableVariant(Route.TASK_LIST) {
                    // TaskListPage(
                        // onNavigateBack = onNavigateBack,
                        // onNavigateToDetail = { navController.navigate(Route.TASK_LOG id it) },
                    // )
                // }
                // [SIMPLIFIED] disabled (commented out, not deleted): custom command task log route
                // slideInVerticallyComposable(
                    // Route.TASK_LOG arg Route.TASK_HASHCODE,
                    // arguments = listOf(navArgument(Route.TASK_HASHCODE) { type = NavType.IntType }),
                // ) {
                    // TaskLogPage(
                        // onNavigateBack = onNavigateBack,
                        // taskHashCode = it.arguments?.getInt(Route.TASK_HASHCODE) ?: -1,
                    // )
                // }

                settingsGraph(
                    onNavigateBack = onNavigateBack,
                    onNavigateTo = { route ->
                        navController.navigate(route = route) { launchSingleTop = true }
                    },
                    cookiesViewModel = cookiesViewModel,
                )
            }

            // [SIMPLIFIED] disabled (commented out, not deleted): self-updater of the app (points at upstream releases)
            // AppUpdater()
            YtdlpUpdater()
        }
    }
}

fun NavGraphBuilder.settingsGraph(
    onNavigateBack: () -> Unit,
    onNavigateTo: (route: String) -> Unit,
    cookiesViewModel: CookiesViewModel,
) {
    navigation(startDestination = Route.SETTINGS_PAGE, route = Route.SETTINGS) {
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences(onNavigateBack)
        }
        animatedComposable(Route.SETTINGS_PAGE) {
            SettingsPage(onNavigateBack = onNavigateBack, onNavigateTo = onNavigateTo)
        }
        animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
            GeneralDownloadPreferences(onNavigateBack = { onNavigateBack() }) {
                onNavigateTo(Route.TEMPLATE)
            }
        }
        animatedComposable(Route.DOWNLOAD_FORMAT) {
            DownloadFormatPreferences(onNavigateBack = onNavigateBack) {
                onNavigateTo(Route.SUBTITLE_PREFERENCES)
            }
        }
        animatedComposable(Route.SUBTITLE_PREFERENCES) { SubtitlePreference { onNavigateBack() } }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for ABOUT (its entry points are disabled)
        // animatedComposable(Route.ABOUT) {
            // AboutPage(
                // onNavigateBack = onNavigateBack,
                // onNavigateToCreditsPage = { onNavigateTo(Route.CREDITS) },
                // onNavigateToUpdatePage = { onNavigateTo(Route.AUTO_UPDATE) },
                // onNavigateToDonatePage = { onNavigateTo(Route.DONATE) },
            // )
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for DONATE (its entry points are disabled)
        // animatedComposable(Route.DONATE) { SponsorsPage(onNavigateBack) }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for CREDITS (its entry points are disabled)
        // animatedComposable(Route.CREDITS) { CreditsPage(onNavigateBack) }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for AUTO_UPDATE (its entry points are disabled)
        // animatedComposable(Route.AUTO_UPDATE) { UpdatePage(onNavigateBack) }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for APPEARANCE (its entry points are disabled)
        // animatedComposable(Route.APPEARANCE) {
            // AppearancePreferences(onNavigateBack = onNavigateBack, onNavigateTo = onNavigateTo)
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for INTERACTION (its entry points are disabled)
        // animatedComposable(Route.INTERACTION) { InteractionPreferencePage(onBack = onNavigateBack) }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for LANGUAGES (its entry points are disabled)
        // animatedComposable(Route.LANGUAGES) { LanguagePage { onNavigateBack() } }
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences { onNavigateBack() }
        }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for TEMPLATE (its entry points are disabled)
        // animatedComposable(Route.TEMPLATE) {
            // TemplateListPage(onNavigateBack = onNavigateBack) {
                // onNavigateTo(Route.TEMPLATE_EDIT id it)
            // }
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for TEMPLATE_EDIT (its entry points are disabled)
        // animatedComposable(
            // Route.TEMPLATE_EDIT arg Route.TEMPLATE_ID,
            // arguments = listOf(navArgument(Route.TEMPLATE_ID) { type = NavType.IntType }),
        // ) {
            // TemplateEditPage(onNavigateBack, it.arguments?.getInt(Route.TEMPLATE_ID) ?: -1)
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for DARK_THEME (its entry points are disabled)
        // animatedComposable(Route.DARK_THEME) { DarkThemePreferences { onNavigateBack() } }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for NETWORK_PREFERENCES (its entry points are disabled)
        // animatedComposable(Route.NETWORK_PREFERENCES) {
            // NetworkPreferences(
                // navigateToCookieProfilePage = { onNavigateTo(Route.COOKIE_PROFILE) }
            // ) {
                // onNavigateBack()
            // }
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for COOKIE_PROFILE (its entry points are disabled)
        // animatedComposable(Route.COOKIE_PROFILE) {
            // CookieProfilePage(
                // cookiesViewModel = cookiesViewModel,
                // navigateToCookieGeneratorPage = { onNavigateTo(Route.COOKIE_GENERATOR_WEBVIEW) },
            // ) {
                // onNavigateBack()
            // }
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for COOKIE_GENERATOR_WEBVIEW (its entry points are disabled)
        // animatedComposable(Route.COOKIE_GENERATOR_WEBVIEW) {
            // WebViewPage(cookiesViewModel = cookiesViewModel) {
                // onNavigateBack()
                // CookieManager.getInstance().flush()
            // }
        // }
        // [SIMPLIFIED] disabled (commented out, not deleted): route for TROUBLESHOOTING (its entry points are disabled)
        // animatedComposable(Route.TROUBLESHOOTING) {
            // TroubleShootingPage(onNavigateTo = onNavigateTo, onBack = onNavigateBack)
        // }
    }
}
