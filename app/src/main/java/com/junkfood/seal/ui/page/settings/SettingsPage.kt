package com.junkfood.seal.ui.page.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Aod
import androidx.compose.material.icons.sharp.Download
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.SettingItem
import com.junkfood.seal.ui.common.Route

@Composable
fun SettingsPage(navController: NavController) {
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
            Column(modifier = Modifier.padding(top = 24.dp)) {
                SettingItem(
                    title = stringResource(id = R.string.download), description = stringResource(
                        id = R.string.download_settings_desc
                    ), icon = Icons.Sharp.Download
                ) {
                    navController.navigate(Route.DOWNLOAD_PREFERENCES) { launchSingleTop = true }
                }
                SettingItem(
                    title = stringResource(id = R.string.display), description = stringResource(
                        id = R.string.display_settings
                    ), icon = Icons.Sharp.Aod
                ) {
                    navController.navigate(Route.APPEARANCE) { launchSingleTop = true }
                }
                SettingItem(
                    title = stringResource(id = R.string.about), description = stringResource(
                        id = R.string.about_page
                    ), icon = Icons.Sharp.Info
                ) {
                    navController.navigate(Route.ABOUT) { launchSingleTop = true }
                }
            }
        }
    }
}