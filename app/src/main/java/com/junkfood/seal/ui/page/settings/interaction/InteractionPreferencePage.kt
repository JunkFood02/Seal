package com.junkfood.seal.ui.page.settings.interaction

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import androidx.compose.material3.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.util.DOWNLOAD_TYPE_INITIALIZATION
import com.junkfood.seal.util.PreferenceUtil.getInt
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.USE_PREVIOUS_SELECTION

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionPreferencePage(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showDownloadTypeDialog by remember { mutableStateOf(false) }
    val initialType by remember(showDownloadTypeDialog) {
        mutableIntStateOf(
            DOWNLOAD_TYPE_INITIALIZATION.getInt()
        )
    }


    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        LargeTopAppBar(
            title = {
                Text(
                    text = stringResource(
                        id = R.string.interface_and_interaction
                    )
                )
            }, scrollBehavior = scrollBehavior, navigationIcon = {
                BackButton(onClick = onBack)
            }
        )
    }) {
        LazyColumn(modifier = Modifier,contentPadding = it) {
            item {
                PreferenceSubtitle(text = stringResource(id = R.string.settings_before_download))
            }

            item {
                PreferenceItem(
                    title = stringResource(id = R.string.download_type),
                    description = when (initialType) {
                        USE_PREVIOUS_SELECTION -> stringResource(id = R.string.use_previous_selection)
                        else -> stringResource(id = R.string.none)
                    }
                ) {
                    showDownloadTypeDialog = true
                }
            }
        }
    }

    if (showDownloadTypeDialog) {
        DownloadTypeCustomizationDialog(
            onDismissRequest = { showDownloadTypeDialog = false },
            selectedItem = initialType
        ) {
            DOWNLOAD_TYPE_INITIALIZATION.updateInt(it)
            showDownloadTypeDialog = false
        }
    }

}