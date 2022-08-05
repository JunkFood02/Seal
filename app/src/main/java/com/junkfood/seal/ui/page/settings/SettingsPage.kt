package com.junkfood.seal.ui.page.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material.icons.sharp.Aod
import androidx.compose.material.icons.sharp.Download
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferencesHint
import com.junkfood.seal.ui.component.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(navController: NavController) {
    val context = LocalContext.current
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var showBatteryHint by remember { mutableStateOf(!pm.isIgnoringBatteryOptimizations(context.packageName)) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            SmallTopAppBar(
                title = {},
                navigationIcon = { BackButton { navController.popBackStack() } },
                modifier = Modifier.padding(start = 8.dp)
            )
            Text(
                modifier = Modifier.padding(start = 24.dp, top = 48.dp),
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.headlineLarge
            )
            LazyColumn(modifier = Modifier.padding(top = 24.dp)) {
                item {
                    AnimatedVisibility(visible = showBatteryHint) {
                        PreferencesHint(
                            title = stringResource(R.string.battery_configuration),
                            icon = Icons.Rounded.EnergySavingsLeaf,
                            description = stringResource(R.string.battery_configuration_desc)
                        ) {
                            context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            })
                            showBatteryHint =
                                !pm.isIgnoringBatteryOptimizations(context.packageName)
                        }
                    }
                }
                item {
                    SettingItem(
                        title = stringResource(id = R.string.download),
                        description = stringResource(
                            id = R.string.download_settings_desc
                        ),
                        icon = Icons.Sharp.Download
                    ) {
                        navController.navigate(Route.DOWNLOAD_PREFERENCES) {
                            launchSingleTop = true
                        }
                    }
                }
                item {

                    SettingItem(
                        title = stringResource(id = R.string.display),
                        description = stringResource(
                            id = R.string.display_settings
                        ),
                        icon = Icons.Sharp.Aod
                    ) {
                        navController.navigate(Route.APPEARANCE) { launchSingleTop = true }
                    }
                }
                item {

                    SettingItem(
                        title = stringResource(id = R.string.about),
                        description = stringResource(
                            id = R.string.about_page
                        ),
                        icon = Icons.Sharp.Info
                    ) {
                        navController.navigate(Route.ABOUT) { launchSingleTop = true }
                    }
                }
            }
        }
    }
}