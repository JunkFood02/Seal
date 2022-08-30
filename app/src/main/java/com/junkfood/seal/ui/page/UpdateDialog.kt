package com.junkfood.seal.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.MultiChoiceItem
import com.junkfood.seal.util.PreferenceUtil

@Composable
fun UpdateDialog(onClick: () -> Unit) {
    var showUpdateDialog by remember {
        mutableStateOf(PreferenceUtil.getInt(PreferenceUtil.UPDATE_DIALOG, 1))
    }
    var disableDialog by remember { mutableStateOf(false) }

    val onDismissRequest = {
        PreferenceUtil.updateInt(
            PreferenceUtil.UPDATE_DIALOG,
            if (disableDialog) 0 else showUpdateDialog + 1
        )
        showUpdateDialog = 0
    }
    if(showUpdateDialog > 0)
        AlertDialog(onDismissRequest = onDismissRequest, dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close))
            }
        }, confirmButton = {
            TextButton(onClick = {
                onClick()
                onDismissRequest()
            }) {
                Text(stringResource(R.string.download_update))
            }
        }, title = { Text(stringResource(R.string.avaliable_update))}, text =  {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(modifier = Modifier.padding(start = 12.dp), text = stringResource(R.string.description_avaliableUpdate_dialog))
            }
            if ((showUpdateDialog > 1))
                MultiChoiceItem(
                    text = stringResource(id = R.string.close_never_show_again),
                    checked = disableDialog, onClick = { disableDialog = !disableDialog })
        })
}