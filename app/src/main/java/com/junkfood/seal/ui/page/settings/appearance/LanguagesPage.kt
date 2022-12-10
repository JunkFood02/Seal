package com.junkfood.seal.ui.page.settings.appearance

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceSingleChoiceItem
import com.junkfood.seal.ui.component.PreferencesHint
import com.junkfood.seal.ui.page.settings.about.weblate
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.LANGUAGE
import com.junkfood.seal.util.PreferenceUtil.SYSTEM_DEFAULT
import com.junkfood.seal.util.PreferenceUtil.getLanguageConfiguration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePage(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    var language by remember { mutableStateOf(PreferenceUtil.getLanguageNumber()) }
    val uriHandler = LocalUriHandler.current
    fun setLanguage(selectedLanguage: Int) {
        language = selectedLanguage
        PreferenceUtil.updateInt(LANGUAGE, language)
        MainActivity.setLanguage(getLanguageConfiguration())
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.language),
                    )
                }, navigationIcon = {
                    BackButton(modifier = Modifier.padding(start = 8.dp)) {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier
                .padding(it)
                .selectableGroup()) {
                item {
                    PreferencesHint(
                        title = stringResource(R.string.translate),
                        description = stringResource(R.string.translate_desc),
                        icon = Icons.Outlined.Translate,
                    ) { uriHandler.openUri(weblate) }
                }
                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(R.string.follow_system),
                        selected = language == SYSTEM_DEFAULT
                    ) { setLanguage(SYSTEM_DEFAULT) }
                }
                for (languageData in PreferenceUtil.languageMap) {
                    item {
                        PreferenceSingleChoiceItem(
                            text = PreferenceUtil.getLanguageDesc(languageData.key),
                            selected = language == languageData.key
                        ) { setLanguage(languageData.key) }
                    }
                }
            }
        })
}