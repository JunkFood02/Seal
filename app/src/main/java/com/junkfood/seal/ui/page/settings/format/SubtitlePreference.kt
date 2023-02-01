package com.junkfood.seal.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.util.AUTO_SUBTITLE
import com.junkfood.seal.util.EMBED_SUBTITLE
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.SPONSORBLOCK
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.SUBTITLE_LANGUAGE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitlePreference(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    var downloadSubtitle by SUBTITLE.booleanState
    val sponsorBlock by SPONSORBLOCK.booleanState
//    var keepSubtitleFile by KEEP_SUBTITLE_FILES.booleanState
    var embedSubtitle by EMBED_SUBTITLE.booleanState
    var autoSubtitle by AUTO_SUBTITLE.booleanState
    var showLanguageDialog by remember { mutableStateOf(false) }


    val subtitleLang by remember(showLanguageDialog) { mutableStateOf(SUBTITLE_LANGUAGE.getString()) }
    val sponsorBlockText = stringResource(id = R.string.subtitle_sponsorblock)
    val embedSubtitleText = stringResource(R.string.embed_subtitles_mkv_msg)
    val hint by remember(sponsorBlock, embedSubtitle) {
        derivedStateOf {
            StringBuilder().apply {
                if (sponsorBlock) append(sponsorBlockText)
                if (isNotEmpty()) append("\n\n")
                if (embedSubtitle) append(embedSubtitleText)
            }.toString()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.subtitle),
                    )
                }, navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {

                item {
                    PreferenceSwitchWithContainer(
                        title = stringResource(id = R.string.download_subtitles),
                        isChecked = downloadSubtitle,
                        onClick = {
                            downloadSubtitle = !downloadSubtitle
                            SUBTITLE.updateBoolean(downloadSubtitle)
                        }, icon = null
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.subtitle_language),
                        icon = Icons.Outlined.Language,
                        description = subtitleLang,
                        onClick = { showLanguageDialog = true }
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.auto_subtitle),
                        icon = Icons.Outlined.ClosedCaption,
                        description = stringResource(
                            id = R.string.auto_subtitle_desc
                        ), isChecked = autoSubtitle, onClick = {
                            autoSubtitle = !autoSubtitle
                            AUTO_SUBTITLE.updateBoolean(autoSubtitle)
                        }
                    )
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.embed_subtitles),
                        description = stringResource(
                            id = R.string.embed_subtitles_desc
                        ),
                        isChecked = embedSubtitle,
                        onClick = {
                            embedSubtitle = !embedSubtitle
                            EMBED_SUBTITLE.updateBoolean(embedSubtitle)
                        }, icon = Icons.Outlined.Subtitles
                    )
                }


                item {
                    if (hint.isNotEmpty())
                        PreferenceInfo(text = hint)
                }

            }
        })
    if (showLanguageDialog)
        SubtitleLanguageDialog {
            showLanguageDialog = false
        }
}

