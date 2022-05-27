package com.junkfood.seal.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.MultiChoiceItem
import com.junkfood.seal.ui.core.Route
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.WELCOME_DIALOG

@Composable
fun WelcomeDialog(navController: NavController) {
    var showWelcomeDialog by remember {
        mutableStateOf(PreferenceUtil.getInt(WELCOME_DIALOG, 1))
    }
    var disableDialog by remember { mutableStateOf(false) }
    val onDismissRequest = {
        PreferenceUtil.updateInt(
            WELCOME_DIALOG,
            if (disableDialog) 0 else showWelcomeDialog + 1
        )
        showWelcomeDialog = 0
    }
    if (showWelcomeDialog > 0)
        AlertDialog(onDismissRequest = onDismissRequest, dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close))
            }
        }, confirmButton = {
            TextButton(onClick = {
                navController.navigate(Route.DOWNLOAD_PREFERENCES)
                onDismissRequest()
            }) {
                Text(stringResource(R.string.go_to_download_preferences))
            }
        }, title = { Text(stringResource(R.string.user_guide)) }, text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                IconDescription(
                    icon = Icons.Outlined.ContentPaste,
                    description = stringResource(R.string.paste_desc)
                )
                IconDescription(
                    icon = Icons.Outlined.FileDownload,
                    description = stringResource(R.string.download_desc)
                )
                IconDescription(
                    icon = Icons.Outlined.Subscriptions,
                    description = stringResource(R.string.download_history_desc)
                )
                IconDescription(
                    icon = Icons.Outlined.SettingsSuggest,
                    description = stringResource(R.string.check_download_settings_desc)
                )
                AnimatedVisibility(visible = (showWelcomeDialog > 1)) {
                    MultiChoiceItem(
                        text = stringResource(id = R.string.close_never_show_again),
                        checked = disableDialog, onClick = { disableDialog = !disableDialog })
                }
            }
        })
}

@Composable
fun IconDescription(modifier: Modifier = Modifier, icon: ImageVector, description: String) {
    Row(
        modifier = modifier.padding(top = 12.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = description,
        )
    }
}

