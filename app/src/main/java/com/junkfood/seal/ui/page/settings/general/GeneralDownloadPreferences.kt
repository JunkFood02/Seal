package com.junkfood.seal.ui.page.settings.general

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DEBUG
import com.junkfood.seal.util.DISABLE_PREVIEW
import com.junkfood.seal.util.NOTIFICATION
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PLAYLIST
import com.junkfood.seal.util.PRIVATE_MODE
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.SPONSORBLOCK
import com.junkfood.seal.util.SUBTITLE
import com.junkfood.seal.util.THUMBNAIL
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.launch

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
    var showSponsorBlockDialog by remember { mutableStateOf(false) }

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


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar =
        {
            com.junkfood.seal.ui.component.LargeTopAppBar(
                title={ Text(text = stringResource(id = R.string.general_settings))}, navigationIcon = {
                    BackButton { onBackPressed() }
                }, scrollBehavior = scrollBehavior
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
                        title = stringResource(id = R.string.ytdlp_version),
                        description = ytdlpVersion,
                        icon = Icons.Outlined.Update
                    ) {
                        scope.launch {
                            kotlin.runCatching {
                                UpdateUtil.updateYtDlp()
                                ytdlpVersion = YT_DLP.getString()
                            }.onFailure {
                                ToastUtil.makeToastSuspend(App.context.getString(R.string.yt_dlp_update_fail))
                            }.onSuccess {
                                ToastUtil.makeToastSuspend(context.getString(R.string.yt_dlp_up_to_date))
                            }
                        }
                    }
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

//                item {
//                    PreferenceItem(
//                        title = stringResource(R.string.custom_command_template),
//                        icon = Icons.Outlined.Code,
//                        description = stringResource(R.string.custom_command_template_desc),
//                    ) {
//                        navigateToTemplate()
//                    }
//                }
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

}


