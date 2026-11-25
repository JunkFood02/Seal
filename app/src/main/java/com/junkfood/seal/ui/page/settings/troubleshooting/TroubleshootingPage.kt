package com.junkfood.seal.ui.page.settings.troubleshooting

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.page.settings.BasePreferencePage
import com.junkfood.seal.ui.page.settings.general.YtdlpUpdateChannelDialog
import com.junkfood.seal.util.DEBUG_LOG_TO_FILE
import com.junkfood.seal.util.DebugLogger
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.RESTRICT_FILENAMES
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_VERSION
import com.junkfood.seal.util.makeToast
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TroubleShootingPage(
    modifier: Modifier = Modifier,
    onNavigateTo: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var logSizeText by remember { mutableStateOf(DebugLogger.getLogFileSizeString()) }

    BasePreferencePage(
        modifier = modifier,
        title = stringResource(R.string.trouble_shooting),
        onBack = onBack,
    ) {
        LazyColumn(contentPadding = it) {
            item {
                OutlinedCard(modifier = Modifier.padding(16.dp)) {
                    PreferenceInfo(
                        modifier = Modifier,
                        text = stringResource(R.string.issue_tracker_hint),
                    )
                    val knownIssueUrlSeal = "https://github.com/JunkFood02/Seal/issues/1399"
                    PreferenceItem(
                        title = "Seal Issue Tracker",
                        description = null,
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { uriHandler.openUri(knownIssueUrlSeal) },
                    )

                    val knownIssueUrlYtdlp = "https://github.com/yt-dlp/yt-dlp/issues/3766"
                    PreferenceItem(
                        title = "yt-dlp Issue Tracker",
                        description = null,
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { uriHandler.openUri(knownIssueUrlYtdlp) },
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
            item { PreferenceSubtitle(text = stringResource(R.string.update)) }
            item {
                var isUpdating by remember { mutableStateOf(false) }
                var showYtdlpDialog by remember { mutableStateOf(false) }

                var ytdlpVersion by remember {
                    mutableStateOf(
                        YoutubeDL.getInstance().version(context.applicationContext)
                            ?: context.getString(R.string.ytdlp_update)
                    )
                }
                PreferenceItem(
                    title = stringResource(id = R.string.ytdlp_update_action),
                    description = ytdlpVersion,
                    leadingIcon = {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.padding(start = 8.dp, end = 16.dp)
                                        .size(24.dp)
                                        .padding(2.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Update,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp, end = 16.dp).size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            runCatching {
                                    isUpdating = true
                                    UpdateUtil.updateYtDlp()
                                    ytdlpVersion = YT_DLP_VERSION.getString()
                                }
                                .onFailure { th ->
                                    th.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        context.makeToast(
                                            App.context.getString(R.string.yt_dlp_update_fail)
                                        )
                                    }
                                }
                                .onSuccess {
                                    withContext(Dispatchers.Main) {
                                        context.makeToast(
                                            context.getString(R.string.yt_dlp_up_to_date) +
                                                " (${YT_DLP_VERSION.getString()})"
                                        )
                                    }
                                }
                            isUpdating = false
                        }
                    },
                    onClickLabel = stringResource(id = R.string.update),
                    trailingIcon = {
                        IconButton(onClick = { showYtdlpDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(id = R.string.open_settings),
                            )
                        }
                    },
                )
                if (showYtdlpDialog) {
                    YtdlpUpdateChannelDialog(onDismissRequest = { showYtdlpDialog = false })
                }
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.js_runtime),
                    description = App.getJsRuntimeInfo(),
                    icon = Icons.Outlined.Code,
                    onClick = {},
                )
            }

            item { PreferenceSubtitle(text = stringResource(R.string.network)) }
            item {
                PreferenceItem(
                    title = stringResource(R.string.cookies),
                    description = stringResource(R.string.cookies_desc),
                    icon = Icons.Outlined.Cookie,
                    onClick = { onNavigateTo(Route.COOKIE_PROFILE) },
                )
            }
            item { PreferenceSubtitle(text = stringResource(R.string.download_directory)) }
            item {
                var restrictFilenames by RESTRICT_FILENAMES.booleanState
                PreferenceSwitch(
                    title = stringResource(id = R.string.restrict_filenames),
                    icon = Icons.Outlined.Spellcheck,
                    description = stringResource(id = R.string.restrict_filenames_desc),
                    isChecked = restrictFilenames,
                ) {
                    restrictFilenames = !restrictFilenames
                    RESTRICT_FILENAMES.updateBoolean(restrictFilenames)
                }
            }
            item { PreferenceSubtitle(text = stringResource(R.string.trouble_shooting)) }
            item {
                var keepLogs by DEBUG_LOG_TO_FILE.booleanState
                PreferenceSwitch(
                    title = stringResource(id = R.string.keep_debug_logs),
                    icon = Icons.Outlined.BugReport,
                    description = stringResource(id = R.string.keep_debug_logs_desc),
                    isChecked = keepLogs,
                ) {
                    keepLogs = !keepLogs
                    DEBUG_LOG_TO_FILE.updateBoolean(keepLogs)
                    logSizeText = DebugLogger.getLogFileSizeString()
                }
            }
            item {
                val logFile = DebugLogger.getLogFile()
                if (logFile.exists() && logFile.length() > 0) {
                    PreferenceItem(
                        title = stringResource(R.string.share_logs),
                        description = "${logFile.absolutePath} ($logSizeText)",
                        icon = Icons.Outlined.Share,
                        onClick = {
                            FileUtil.createIntentForSharingFile(logFile.absolutePath)?.let {
                                context.startActivity(
                                    android.content.Intent.createChooser(
                                        it,
                                        context.getString(R.string.share_logs),
                                    )
                                )
                            } ?: context.makeToast(R.string.no_logs_found)
                        },
                    )
                    PreferenceItem(
                        title = stringResource(R.string.clear_debug_logs),
                        description = null,
                        icon = Icons.Outlined.DeleteOutline,
                        onClick = {
                            scope.launch {
                                DebugLogger.clearLog()
                                logSizeText = DebugLogger.getLogFileSizeString()
                                withContext(Dispatchers.Main) {
                                    context.makeToast(R.string.logs_cleared)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
