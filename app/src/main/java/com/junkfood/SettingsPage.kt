package com.junkfood

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.ui.SettingItem
import com.junkfood.ui.theme.SealTheme

@Composable
fun SettingsPage(navController: NavController) {

    SealTheme() {


        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LargeTopAppBar(title = {
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

            }, modifier = Modifier.padding(9f.dp))
            SettingItem(
                title = stringResource(id = R.string.download), description = stringResource(
                    id = R.string.download_settings
                ), icon = Icons.Outlined.FileDownload
            ) {

            }
        }
    }
}