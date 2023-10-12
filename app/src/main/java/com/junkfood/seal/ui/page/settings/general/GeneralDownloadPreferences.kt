package com.junkfood.seal.ui.page.settings.general

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.PrintDisabled
import androidx.compose.material.icons.outlined.RemoveDone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.theme.generateLabelColor
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DEBUG
import com.junkfood.seal.util.DISABLE_PREVIEW
import com.junkfood.seal.util.DOWNLOAD_ARCHIVE
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.NOTIFICATION
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PRIVATE_MODE
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.SPONSORBLOCK
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.THUMBNAIL
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP
import com.junkfood.seal.util.YT_DLP_NIGHTLY
import com.junkfood.seal.util.YT_DLP_UPDATE
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun GeneralDownloadPreferences(
    onBackPressed: () -> Unit,
    navigateToTemplate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var showSponsorBlockDialog by remember { mutableStateOf(false) }
    var showYtdlpDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    val downloadSubtitle by SUBTITLE.booleanState

    var displayErrorReport by DEBUG.booleanState
    var downloadPlaylist by remember { mutableStateOf(PreferenceUtil.getValue(PLAYLIST)) }
    var isSponsorBlockEnabled by remember { mutableStateOf(PreferenceUtil.getValue(SPONSORBLOCK)) }
    var downloadNotification by remember {
        mutableStateOf(PreferenceUtil.getValue(NOTIFICATION))
    }

    var isPrivateModeEnabled by remember {
        mutableStateOf(PreferenceUtil.getValue(PRIVATE_MODE))
    }

    var isPreviewDisabled by remember { mutableStateOf(PreferenceUtil.getValue(DISABLE_PREVIEW)) }
    var isNotificationPermissionGranted by remember {
        mutableStateOf(NotificationUtil.areNotificationsEnabled())
    }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= 33) rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) { status ->
            if (!status) ToastUtil.makeToast(context.getString(R.string.permission_denied))
            else isNotificationPermissionGranted = true
        } else null

    var useDownloadArchive by DOWNLOAD_ARCHIVE.booleanState
    var showClearArchiveDialog by remember { mutableStateOf(false) }
    var archiveFileContent by remember {
        mutableStateOf(listOf<String>())
    }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val isPermissionGranted = Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            com.junkfood.seal.ui.component.LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.general_settings)) },
                navigationIcon = {
                    BackButton { onBackPressed() }
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = {
            val isCustomCommandEnabled by remember {
                mutableStateOf(
                    PreferenceUtil.getValue(CUSTOM_COMMAND)
                )
            }
            LazyColumn(
                modifier = Modifier.padding(it)
            ) {
//                item {
//                    SettingTitle(text = stringResource(id = R.string.general_settings))
//                }
                if (isCustomCommandEnabled)
                    item {
                        PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
                    }
                item {
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
                            if (isUpdating) UpdateProgressIndicator() else {
                                Icon(
                                    imageVector = Icons.Outlined.Update,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(start = 8.dp, end = 16.dp)
                                        .size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }, onClick = {
                            scope.launch {
                                runCatching {
                                    isUpdating = true
                                    UpdateUtil.updateYtDlp()
                                    ytdlpVersion = YT_DLP.getString()
                                }.onFailure { th ->
                                    th.printStackTrace()
                                    ToastUtil.makeToastSuspend(App.context.getString(R.string.yt_dlp_update_fail))
                                }.onSuccess {
                                    ToastUtil.makeToastSuspend(context.getString(R.string.yt_dlp_up_to_date))
                                }
                                isUpdating = false
                            }
                        }, onClickLabel = stringResource(id = R.string.update),
                        trailingIcon = {
                            IconButton(onClick = { showYtdlpDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = stringResource(
                                        id = R.string.open_settings
                                    )
                                )
                            }
                        }
                    )
                }
                item {
                    PreferenceSwitch(title = stringResource(id = R.string.download_notification),
                        description = stringResource(
                            id = if (isNotificationPermissionGranted) R.string.download_notification_desc
                            else R.string.permission_denied
                        ),
                        icon = if (!isNotificationPermissionGranted) Icons.Outlined.NotificationsOff
                        else if (!downloadNotification) Icons.Outlined.Notifications
                        else Icons.Outlined.NotificationsActive,
                        isChecked = downloadNotification && isNotificationPermissionGranted,
                        onClick = {
                            notificationPermission?.launchPermissionRequest()
                            if (isNotificationPermissionGranted) {
                                if (downloadNotification)
                                    NotificationUtil.cancelAllNotifications()
                                downloadNotification = !downloadNotification
                                PreferenceUtil.updateValue(
                                    NOTIFICATION, downloadNotification
                                )
                            }
                        })
                }
                item {
                    var configureBeforeDownload by CONFIGURE.booleanState
                    PreferenceSwitch(title = stringResource(id = R.string.settings_before_download),
                        description = stringResource(
                            id = R.string.settings_before_download_desc
                        ),
                        icon = if (configureBeforeDownload) Icons.Outlined.DoneAll else Icons.Outlined.RemoveDone,
                        isChecked = configureBeforeDownload,
                        onClick = {
                            configureBeforeDownload = !configureBeforeDownload
                            PreferenceUtil.updateValue(
                                CONFIGURE, configureBeforeDownload
                            )
                        })
                }


                item {
                    var thumbnailSwitch by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(THUMBNAIL)
                        )
                    }
                    PreferenceSwitch(title = stringResource(id = R.string.create_thumbnail),
                        description = stringResource(
                            id = R.string.create_thumbnail_summary
                        ),
                        enabled = !isCustomCommandEnabled,
                        icon = Icons.Outlined.Image,
                        isChecked = thumbnailSwitch,
                        onClick = {
                            thumbnailSwitch = !thumbnailSwitch
                            PreferenceUtil.updateValue(THUMBNAIL, thumbnailSwitch)
                        })
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.print_details),
                        description = stringResource(R.string.print_details_desc),
                        icon = if (displayErrorReport) Icons.Outlined.Print else Icons.Outlined.PrintDisabled,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            displayErrorReport = !displayErrorReport
                            PreferenceUtil.updateValue(DEBUG, displayErrorReport)
                        },
                        isChecked = displayErrorReport
                    )
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.privacy))
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.private_mode),
                        description = stringResource(R.string.private_mode_desc),
                        icon = if (isPrivateModeEnabled) Icons.Outlined.HistoryToggleOff else Icons.Outlined.History,
                        isChecked = isPrivateModeEnabled,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            isPrivateModeEnabled = !isPrivateModeEnabled
                            PreferenceUtil.updateValue(
                                PRIVATE_MODE,
                                isPrivateModeEnabled
                            )
                        }
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.disable_preview),
                        description = stringResource(R.string.disable_preview_desc),
                        icon = if (isPreviewDisabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        isChecked = isPreviewDisabled,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            isPreviewDisabled = !isPreviewDisabled
                            PreferenceUtil.updateValue(
                                DISABLE_PREVIEW,
                                isPreviewDisabled
                            )
                        }
                    )
                }

                item {
                    PreferenceSubtitle(text = stringResource(R.string.advanced_settings))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.download_playlist),
                        onClick = {
                            downloadPlaylist = !downloadPlaylist
                            PreferenceUtil.updateValue(
                                PLAYLIST, downloadPlaylist
                            )
                        },
                        icon = Icons.Outlined.PlaylistAddCheck,
                        enabled = !isCustomCommandEnabled,
                        description = stringResource(R.string.download_playlist_desc),
                        isChecked = downloadPlaylist
                    )
                }

                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(id = R.string.download_archive),
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                archiveFileContent = FileUtil.getArchiveFile().readLines()
                                Log.d("TAG", "GeneralDownloadPreferences: $archiveFileContent")
                                withContext(Dispatchers.Main) {
                                    showClearArchiveDialog = true
                                }
                            }
                        },
                        icon = Icons.Outlined.Archive,
                        description = stringResource(R.string.download_archive_desc),
                        isChecked = useDownloadArchive,
                        onChecked = {
                            useDownloadArchive = !useDownloadArchive
                            DOWNLOAD_ARCHIVE.updateBoolean(useDownloadArchive)
                        },
                        enabled = isPermissionGranted
                    )
                }

                item {
                    PreferenceSwitchWithDivider(title = stringResource(R.string.sponsorblock),
                        description = stringResource(
                            R.string.sponsorblock_desc
                        ),
                        icon = Icons.Outlined.MoneyOff,
                        enabled = !isCustomCommandEnabled,
                        isChecked = isSponsorBlockEnabled,
                        onChecked = {
                            isSponsorBlockEnabled = !isSponsorBlockEnabled
                            PreferenceUtil.updateValue(SPONSORBLOCK, isSponsorBlockEnabled)
                        },
                        onClick = { showSponsorBlockDialog = true })
                }

                if (downloadSubtitle) item {
                    PreferenceInfo(text = stringResource(id = R.string.subtitle_sponsorblock))
                }
            }
        })
    if (showSponsorBlockDialog) {
        SponsorBlockDialog {
            showSponsorBlockDialog = false
        }
    }
    if (showYtdlpDialog) {
        var ytdlpNightly by YT_DLP_NIGHTLY.booleanState
        var ytdlpAutoUpdate by YT_DLP_UPDATE.booleanState
        SealDialog(
            onDismissRequest = { showYtdlpDialog = false },
            confirmButton = {
                ConfirmButton {
                    YT_DLP_NIGHTLY.updateBoolean(ytdlpNightly)
                    YT_DLP_UPDATE.updateBoolean(ytdlpAutoUpdate)
                    showYtdlpDialog = false
                }
            },
            dismissButton = {
                DismissButton {
                    showYtdlpDialog = false
                }
            },
            title = { Text(text = stringResource(id = R.string.update)) },
            icon = { Icon(Icons.Outlined.SyncAlt, null) },
            text = {
                LazyColumn() {
                    item {
                        Text(
                            text = stringResource(id = R.string.update_channel),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.run {
                                if (ytdlpNightly) tertiary else primary
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    item {
                        DialogSingleChoiceItem(
                            text = "yt-dlp",
                            selected = !ytdlpNightly,
                            label = "Stable"
                        ) {
                            ytdlpNightly = false
                        }
                    }
                    item {
                        DialogSingleChoiceItem(
                            text = "yt-dlp-nightly-builds",
                            selected = ytdlpNightly,
                            label = "Nightly",
                            labelContainerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            ytdlpNightly = true
                        }
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.additional_settings),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    item {
                        DialogCheckBoxItem(
                            text = stringResource(id = R.string.auto_update),
                            checked = ytdlpAutoUpdate
                        ) {
                            ytdlpAutoUpdate = !ytdlpAutoUpdate
                        }
                    }
                }
            },
        )
    }
    if (showClearArchiveDialog) {
        Log.d("Dialog", "GeneralDownloadPreferences:$archiveFileContent ")
        DownloadArchiveDialog(
            archiveFileContent = archiveFileContent,
            onDismissRequest = { showClearArchiveDialog = false }) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    FileUtil.getArchiveFile().writeText("")
                }
            }
        }
    }

}

@Composable
private fun DialogSingleChoiceItem(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    label: String,
    labelContainerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = true,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            modifier = Modifier.clearAndSetSemantics { }, selected = selected, onClick = onClick
        )

        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            modifier.padding(end = 12.dp),
            shape = CircleShape,
            color = labelContainerColor
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = label,
                color = MaterialTheme.colorScheme.contentColorFor(labelContainerColor),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun DialogCheckBoxItem(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = true,
                onValueChange = { onClick() },
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier.clearAndSetSemantics { },
            checked = checked, onCheckedChange = { onClick() },
        )
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun UpdateProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .padding(start = 8.dp, end = 16.dp)
            .size(24.dp)
            .padding(2.dp)
    )
}


@Composable
fun DownloadArchiveDialog(
    archiveFileContent: List<String>,
    onDismissRequest: () -> Unit,
    onClearArchiveCallback: () -> Unit
) {
    SealDialog(
        onDismissRequest = onDismissRequest, confirmButton = {
            ConfirmButton {
                onClearArchiveCallback()
                onDismissRequest()
            }
        }, dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Unarchive,
                contentDescription = null
            )
        },
        title = { Text(text = stringResource(id = R.string.clear_download_archive)) },
        text = {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 12.dp),
                    text = stringResource(
                        id = R.string.clear_download_archive_desc,
                        pluralStringResource(
                            id = R.plurals.item_count,
                            count = archiveFileContent.size,
                            archiveFileContent.size
                        )
                    ), style = MaterialTheme.typography.bodyLarge
                )
                val textStyle =
                    MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                if (archiveFileContent.isNotEmpty()) {
                    HorizontalDivider()
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(archiveFileContent) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(16.dp)
                                        .background(
                                            color = it
                                                .hashCode()
                                                .generateLabelColor(), shape = CircleShape
                                        )
                                        .clearAndSetSemantics { }
                                ) {}
                                Text(
                                    text = it,
                                    style = textStyle,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
            }
        }
    )
}


@Composable
@Preview
fun DownloadArchiveDialogPreview() {
    val str = buildList { repeat(20) { add("youtube IPf4AxotvNU") } }
    DownloadArchiveDialog(
        archiveFileContent = str,
        onDismissRequest = { }) {}
}