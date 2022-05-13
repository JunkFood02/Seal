package com.junkfood.seal.ui.page.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.SettingItem
import com.junkfood.seal.ui.core.Route
import com.junkfood.seal.ui.theme.SealTheme

@Composable
fun SettingsPage(navController: NavController) {

    SealTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        fontSize = MaterialTheme.typography.displaySmall.fontSize
                    )
                }, navigationIcon =
                {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }, modifier = Modifier.padding(start = 8.dp)
            )
            SettingItem(
                title = stringResource(id = R.string.download), description = stringResource(
                    id = R.string.download_settings
                ), icon = Icons.Default.Download
            ) {
                navController.navigate(Route.DOWNLOAD_PREFERENCES) { launchSingleTop = true }
            }
            SettingItem(
                title = stringResource(id = R.string.about), description = stringResource(
                    id = R.string.about_page
                ), icon = Icons.Default.Info
            ) {
                navController.navigate(Route.ABOUT) { launchSingleTop = true }
            }
        }
    }
}