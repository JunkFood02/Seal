package com.junkfood.seal.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NetworkWifi
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    windowWidth: WindowWidthSizeClass = LocalWindowWidthState.current,
    currentRoute: String? = null,
    showQuickSettings: Boolean = true,
    onNavigateToRoute: (String) -> Unit,
    onDismissRequest: suspend () -> Unit,
    gesturesEnabled: Boolean = true,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    when (windowWidth) {
        WindowWidthSizeClass.Compact,
        WindowWidthSizeClass.Medium -> {
            ModalNavigationDrawer(
                gesturesEnabled = gesturesEnabled,
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerState = drawerState, modifier = modifier.width(360.dp)) {
                        NavigationDrawerSheetContent(
                            modifier = Modifier,
                            currentRoute = currentRoute,
                            showQuickSettings = showQuickSettings,
                            onNavigateToRoute = onNavigateToRoute,
                            onDismissRequest = onDismissRequest,
                            footer = footer,
                        )
                    }
                },
                content = content,
            )
        }
        WindowWidthSizeClass.Expanded -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = modifier.width(240.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        NavigationDrawerSheetContent(
                            modifier = Modifier,
                            currentRoute = currentRoute,
                            showQuickSettings = showQuickSettings,
                            onNavigateToRoute = onNavigateToRoute,
                            onDismissRequest = onDismissRequest,
                            footer = footer,
                        )
                    }
                },
                content = content,
            )
        }
    }
}

@Composable
fun NavigationDrawerSheetContent(
    modifier: Modifier = Modifier,
    currentRoute: String? = null,
    showQuickSettings: Boolean = true,
    onNavigateToRoute: (String) -> Unit,
    onDismissRequest: suspend () -> Unit,
    footer: @Composable (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(72.dp))
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.download_queue)) },
                icon = { Icon(Icons.Filled.Download, null) },
                onClick = {
                    scope
                        .launch { onDismissRequest() }
                        .invokeOnCompletion { onNavigateToRoute(Route.HOME) }
                },
                selected = currentRoute == Route.HOME,
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.downloads_history)) },
                icon = { Icon(Icons.Outlined.Subscriptions, null) },
                onClick = {
                    scope
                        .launch { onDismissRequest() }
                        .invokeOnCompletion { onNavigateToRoute(Route.DOWNLOADS) }
                },
                selected = currentRoute == Route.DOWNLOADS,
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.custom_command)) },
                icon = { Icon(Icons.Outlined.Terminal, null) },
                onClick = {
                    scope
                        .launch { onDismissRequest() }
                        .invokeOnCompletion { onNavigateToRoute(Route.TASK_LIST) }
                },
                selected = currentRoute == Route.TASK_LIST,
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.settings)) },
                icon = { Icon(Icons.Outlined.Settings, null) },
                onClick = {
                    scope
                        .launch { onDismissRequest() }
                        .invokeOnCompletion { onNavigateToRoute(Route.SETTINGS) }
                },
                selected = currentRoute == Route.SETTINGS_PAGE,
            )

            NavigationDrawerItem(
                label = { Text(stringResource(R.string.sponsor)) },
                icon = { Icon(Icons.Outlined.VolunteerActivism, null) },
                onClick = {
                    scope
                        .launch { onDismissRequest() }
                        .invokeOnCompletion { onNavigateToRoute(Route.DONATE) }
                },
                selected = currentRoute == Route.DONATE,
            )

            if (showQuickSettings) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Column(
                    modifier = Modifier.padding(start = 16.dp).padding(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier,
                    )
                }

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.general_settings)) },
                    icon = { Icon(Icons.Rounded.SettingsApplications, null) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion {
                                onNavigateToRoute(Route.GENERAL_DOWNLOAD_PREFERENCES)
                            }
                    },
                    selected = currentRoute == Route.GENERAL_DOWNLOAD_PREFERENCES,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.download_directory)) },
                    icon = { Icon(Icons.Rounded.Folder, null) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.DOWNLOAD_DIRECTORY) }
                    },
                    selected = currentRoute == Route.DOWNLOAD_DIRECTORY,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.network)) },
                    icon = { Icon(Icons.Rounded.NetworkWifi, null) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.NETWORK_PREFERENCES) }
                    },
                    selected = currentRoute == Route.NETWORK_PREFERENCES,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.about)) },
                    icon = { Icon(Icons.Rounded.Info, null) },
                    onClick = {
                        scope
                            .launch { onDismissRequest() }
                            .invokeOnCompletion { onNavigateToRoute(Route.ABOUT) }
                    },
                    selected = currentRoute == Route.ABOUT,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        footer?.invoke()
    }
}
