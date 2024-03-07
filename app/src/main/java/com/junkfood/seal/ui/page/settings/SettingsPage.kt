package com.junkfood.seal.ui.page.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Aod
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferencesHintCard
import com.junkfood.seal.ui.component.SettingItem
import com.junkfood.seal.ui.component.SettingTitle
import com.junkfood.seal.ui.component.SmallTopAppBar
import com.junkfood.seal.util.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SHOW_SPONSOR_MSG

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavController, onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var showBatteryHint by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                !pm.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                false
            }
        )
    }
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    } else {
        Intent()
    }
    val isActivityAvailable: Boolean = if (Build.VERSION.SDK_INT < 23) false
    else if (Build.VERSION.SDK_INT < 33) context.packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_ALL
    ).isNotEmpty()
    else context.packageManager.queryIntentActivities(
        intent,
        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_SYSTEM_ONLY.toLong())
    ).isNotEmpty()


    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showBatteryHint = !pm.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val showSponsorMessage by SHOW_SPONSOR_MSG.intState

    LaunchedEffect(Unit) {
        SHOW_SPONSOR_MSG.updateInt(showSponsorMessage + 1)
    }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmallTopAppBar(
                titleText = stringResource(id = R.string.settings),
                navigationIcon = { BackButton(onBackPressed) },
                scrollBehavior = scrollBehavior
            )
        }) {
        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            item {
                SettingTitle(text = stringResource(id = R.string.settings))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            ) {
                item {
                    AnimatedVisibility(
                        visible = showBatteryHint && isActivityAvailable,
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        PreferencesHintCard(
                            title = stringResource(R.string.battery_configuration),
                            icon = Icons.Rounded.EnergySavingsLeaf,
                            description = stringResource(R.string.battery_configuration_desc),
                        ) {
                            launcher.launch(intent)
                            showBatteryHint =
                                !pm.isIgnoringBatteryOptimizations(context.packageName)
                        }
                    }
                }
            }
            if (!showBatteryHint && showSponsorMessage > 30)
                item {
                    PreferencesHintCard(
                        title = stringResource(id = R.string.sponsor),
                        icon = Icons.Rounded.VolunteerActivism,
                        description = stringResource(id = R.string.sponsor_desc)
                    ) {
                        navController.navigate(Route.DONATE)
                    }
                }
            item {
                SettingItem(
                    title = stringResource(id = R.string.general_settings),
                    description = stringResource(
                        id = R.string.general_settings_desc
                    ),
                    icon = Icons.Filled.SettingsApplications
                ) {
                    navController.navigate(Route.GENERAL_DOWNLOAD_PREFERENCES) {
                        launchSingleTop = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.download_directory),
                    description = stringResource(
                        id = R.string.download_directory_desc
                    ),
                    icon = Icons.Filled.Folder
                ) {
                    navController.navigate(Route.DOWNLOAD_DIRECTORY) {
                        launchSingleTop = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.format),
                    description = stringResource(id = R.string.format_settings_desc),
                    icon = if (EXTRACT_AUDIO.getBoolean()) Icons.Filled.AudioFile else Icons.Filled.VideoFile
                ) {
                    navController.navigate(Route.DOWNLOAD_FORMAT) {
                        launchSingleTop = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.network),
                    description = stringResource(id = R.string.network_settings_desc),
                    icon = if (App.connectivityManager.isActiveNetworkMetered) Icons.Filled.SignalCellular4Bar else Icons.Filled.SignalWifi4Bar
                ) {
                    navController.navigate(Route.NETWORK_PREFERENCES) {
                        launchSingleTop = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.custom_command),
                    description = stringResource(id = R.string.custom_command_desc),
                    icon = Icons.Outlined.Terminal
                ) {
                    navController.navigate(Route.TEMPLATE) {
                        launchSingleTop = true
                    }
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.look_and_feel),
                    description = stringResource(
                        id = R.string.display_settings
                    ),
                    icon = Icons.Filled.Palette
                ) {
                    navController.navigate(Route.APPEARANCE) { launchSingleTop = true }
                }
            }
            item {

                SettingItem(
                    title = stringResource(id = R.string.about), description = stringResource(
                        id = R.string.about_page
                    ), icon = Icons.Filled.Info
                ) {
                    navController.navigate(Route.ABOUT) { launchSingleTop = true }
                }
            }
        }
    }
}