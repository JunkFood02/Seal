package com.junkfood

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.ui.PreferenceSwitch
import com.junkfood.ui.theme.SealTheme

@Composable
fun DownloadPreferences(navController: NavController) {

    SealTheme() {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.download),
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

                }, modifier = Modifier.padding(9f.dp)
            )
            var switch by remember { mutableStateOf(PreferenceUtil.getValue("extract_audio")) }
            PreferenceSwitch(
                title = stringResource(id = R.string.extract_audio), description = stringResource(
                    id = R.string.extract_audio_summary
                ), icon = Icons.Outlined.Audiotrack, isChecked = switch, onClick = {
                    switch=switch.not()
                }
            )
        }
    }
}