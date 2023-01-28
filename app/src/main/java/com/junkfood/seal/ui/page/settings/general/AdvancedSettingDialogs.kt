package com.junkfood.seal.ui.page.settings.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.SPONSORBLOCK_CATEGORIES

const val ytdlpReference = "https://github.com/yt-dlp/yt-dlp#usage-and-options"
const val sponsorBlockReference = "https://github.com/yt-dlp/yt-dlp#sponsorblock-options"


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SponsorBlockDialog(onDismissRequest: () -> Unit) {
    var categories by remember {
        mutableStateOf(PreferenceUtil.getSponsorBlockCategories())
    }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.MoneyOff, null)
    }, title = { Text(stringResource(R.string.sponsorblock)) }, text = {
        Column {
            Text(
                stringResource(R.string.sponsorblock_categories_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                value = categories,
                label = { Text(stringResource(R.string.sponsorblock_categories)) },
                onValueChange = { categories = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    softwareKeyboardController?.hide()
                    focusManager.moveFocus(FocusDirection.Down)
                })
            )
            LinkButton(link = sponsorBlockReference)
        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            onDismissRequest()
            PreferenceUtil.encodeString(SPONSORBLOCK_CATEGORIES, categories)
        }
    })
}

