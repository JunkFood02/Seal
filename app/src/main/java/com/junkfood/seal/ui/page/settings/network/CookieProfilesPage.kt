package com.junkfood.seal.ui.page.settings.network

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.database.CookieProfile
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PasteButton
import com.junkfood.seal.ui.component.PreferenceItemVariant
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.ui.component.TemplateItem
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.COOKIES
import com.junkfood.seal.util.TextUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieProfilePage(onBackPressed: () -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val cookies = DatabaseUtil.getCookiesFlow().collectAsState(emptyList()).value
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isCookieEnabled by remember { mutableStateOf(PreferenceUtil.getValue(COOKIES)) }
    var editingCookieProfile by remember { mutableStateOf(-1) }
    var selectedCookieProfile by remember {
        mutableStateOf(PreferenceUtil.getInt(PreferenceUtil.COOKIES_PROFILE_INDEX, -1))
    }
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.cookies),
                )
            }, navigationIcon = {
                BackButton(modifier = Modifier.padding(start = 8.dp)) {
                    onBackPressed()
                }
            })
        })
    { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                PreferenceSwitchWithContainer(
                    title = stringResource(R.string.use_cookies),
                    icon = null,
                    isChecked = isCookieEnabled,
                    onClick = {
                        isCookieEnabled = !isCookieEnabled
                        PreferenceUtil.updateValue(COOKIES, isCookieEnabled)
                    })
            }
            itemsIndexed(cookies) { index, item ->
                TemplateItem(
                    label = item.url,
                    template = null,
                    selected = selectedCookieProfile == index,
                    onClick = {
                        editingCookieProfile = index
                        showEditDialog = true
                    }, onSelect = {
                        selectedCookieProfile = index
                        PreferenceUtil.updateInt(
                            PreferenceUtil.COOKIES_PROFILE_INDEX,
                            selectedCookieProfile
                        )
                    }, onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        editingCookieProfile = index
                        showDeleteDialog = true
                    })
            }
            item {
                PreferenceItemVariant(
                    title = stringResource(id = R.string.generate_new_cookies),
                    icon = Icons.Outlined.Add
                ) {
                    editingCookieProfile = -1
                    showEditDialog = true
                }
            }
        }

    }
    if (showEditDialog) {
        if (editingCookieProfile == --1) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieGeneratorDialog(
    scope: CoroutineScope = rememberCoroutineScope(),
    newProfile: Boolean = false,
    cookieProfile: CookieProfile = CookieProfile(id = 0, url = "", content = ""),
    navigateToCookieGeneratorPage: (Int) -> Unit = {},
    onDismissRequest: () -> Unit
) {
    var url by remember { mutableStateOf(cookieProfile.url) }
    var cookies by remember { mutableStateOf(cookieProfile.content) }
    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Cookie, null)
    }, title = { Text(stringResource(R.string.cookies)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(
                stringResource(R.string.cookies_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = url, label = { Text("URL") },
                onValueChange = { url = it }, trailingIcon = {
                    PasteButton { url = TextUtil.matchUrlFromClipboard(it) }
                }, maxLines = 1
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                value = cookies,
                label = { Text(stringResource(R.string.cookies_file_name)) },
                onValueChange = { cookies = it }, minLines = 8, maxLines = 8
            )
            TextButtonWithIcon(
                onClick = {
                    scope.launch {
                        DatabaseUtil.updateCookieProfile(
                            cookieProfile.copy(url = url)
                        )
                        navigateToCookieGeneratorPage(cookieProfile.id)
                    }
                },
                icon = Icons.Outlined.GeneratingTokens,
                text = stringResource(id = R.string.generate_new_cookies)
            )

        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            scope.launch {
                DatabaseUtil.updateCookieProfile(cookieProfile.copy(url = url, content = cookies))
                onDismissRequest()
            }
        }
    })

}
