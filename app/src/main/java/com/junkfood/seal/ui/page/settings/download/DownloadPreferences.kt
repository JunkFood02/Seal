package com.junkfood.seal.ui.page.settings.download

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.ARIA2C
import com.junkfood.seal.util.PreferenceUtil.COOKIES
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.DEBUG
import com.junkfood.seal.util.PreferenceUtil.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.NOTIFICATION
import com.junkfood.seal.util.PreferenceUtil.PLAYLIST
import com.junkfood.seal.util.PreferenceUtil.SPONSORBLOCK
import com.junkfood.seal.util.PreferenceUtil.SUBTITLE
import com.junkfood.seal.util.PreferenceUtil.THUMBNAIL
import com.junkfood.seal.util.PreferenceUtil.getAudioFormatDesc
import com.junkfood.seal.util.PreferenceUtil.getVideoFormatDesc
import com.junkfood.seal.util.PreferenceUtil.getVideoResolutionDesc
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun DownloadPreferences(
    onBackPressed: () -> Unit,
    navigateToDownloadDirectory: () -> Unit,
    navigateToTemplate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAudioFormatEditDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }
    var showConcurrentDownloadDialog by remember { mutableStateOf(false) }
    var showSponsorBlockDialog by remember { mutableStateOf(false) }
    var showCookiesDialog by remember { mutableStateOf(false) }
    var aria2c by remember { mutableStateOf(PreferenceUtil.getValue(ARIA2C)) }

    var displayErrorReport by remember { mutableStateOf(PreferenceUtil.getValue(DEBUG)) }
    var downloadPlaylist by remember { mutableStateOf(PreferenceUtil.getValue(PLAYLIST)) }
    var isSponsorBlockEnabled by remember { mutableStateOf(PreferenceUtil.getValue(SPONSORBLOCK)) }
    var downloadNotification by remember {
        mutableStateOf(PreferenceUtil.getValue(NOTIFICATION))
    }

    var videoFormat by remember { mutableStateOf(getVideoFormatDesc()) }
    var videoResolution by remember { mutableStateOf(getVideoResolutionDesc()) }
    var audioFormat by remember { mutableStateOf(getAudioFormatDesc()) }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= 33) rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) { status ->
            if (!status) TextUtil.makeToast(context.getString(R.string.permission_denied))
        } else null

    fun checkNotificationPermission(): Boolean =
        notificationPermission == null || (notificationPermission.status == PermissionStatus.Granted)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            com.junkfood.seal.ui.component.LargeTopAppBar(title = {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.download),
                )
            }, navigationIcon = {
                BackButton(modifier = Modifier.padding(start = 8.dp)) {
                    onBackPressed()
                }
            }, scrollBehavior = scrollBehavior
            )
        },
        content = {
            var customCommandEnable by remember {
                mutableStateOf(
                    PreferenceUtil.getValue(CUSTOM_COMMAND)
                )
            }
            var audioSwitch by remember {
                mutableStateOf(
                    PreferenceUtil.getValue(EXTRACT_AUDIO)
                )
            }
            LazyColumn(
                modifier = Modifier.padding(it)
            ) {
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.general_settings))
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.download_directory),
                        description = stringResource(R.string.download_directory_desc),
                        icon = Icons.Outlined.FolderOpen
                    ) { navigateToDownloadDirectory() }
                }
                item {
                    var ytdlpVersion by remember {
                        mutableStateOf(BaseApplication.ytdlpVersion)
                    }
                    PreferenceItem(
                        title = stringResource(id = R.string.ytdlp_version),
                        description = ytdlpVersion,
                        icon = Icons.Outlined.Update
                    ) {
                        scope.launch {
                            ytdlpVersion = UpdateUtil.updateYtDlp()
                        }
                    }
                }
                item {
                    PreferenceSwitch(title = stringResource(id = R.string.download_notification),
                        description = stringResource(
                            id = if (notificationPermission == null || notificationPermission.status == PermissionStatus.Granted) R.string.download_notification_desc
                            else R.string.permission_denied
                        ),
                        icon = if (!checkNotificationPermission()) Icons.Outlined.NotificationsOff
                        else if (!downloadNotification) Icons.Outlined.Notifications
                        else Icons.Outlined.NotificationsActive,
                        isChecked = downloadNotification && checkNotificationPermission(),
                        onClick = {
                            notificationPermission?.launchPermissionRequest()
                            if (checkNotificationPermission()) {
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
                    var configureBeforeDownload by remember {
                        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.CONFIGURE, true))
                    }
                    PreferenceSwitch(title = stringResource(id = R.string.settings_before_download),
                        description = stringResource(
                            id = R.string.settings_before_download_desc
                        ),
                        icon = if (configureBeforeDownload) Icons.Outlined.DoneAll else Icons.Outlined.RemoveDone,
                        isChecked = configureBeforeDownload,
                        onClick = {
                            configureBeforeDownload = !configureBeforeDownload
                            PreferenceUtil.updateValue(
                                PreferenceUtil.CONFIGURE, configureBeforeDownload
                            )
                        })
                }

                item {

                    PreferenceSwitch(title = stringResource(id = R.string.extract_audio),
                        description = stringResource(
                            id = R.string.extract_audio_summary
                        ),
                        icon = Icons.Outlined.MusicNote,
                        enabled = !customCommandEnable,
                        isChecked = audioSwitch,
                        onClick = {
                            audioSwitch = !audioSwitch
                            PreferenceUtil.updateValue(EXTRACT_AUDIO, audioSwitch)
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
                        enabled = !customCommandEnable,
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
                        enabled = !customCommandEnable,
                        onClick = {
                            displayErrorReport = !displayErrorReport
                            PreferenceUtil.updateValue(DEBUG, displayErrorReport)
                        },
                        isChecked = displayErrorReport
                    )
                }
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.format))
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.video_format_preference),
                        description = videoFormat,
                        icon = Icons.Outlined.VideoFile,
                        enabled = !customCommandEnable and !audioSwitch
                    ) { showVideoFormatDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_quality),
                        description = videoResolution,
                        icon = Icons.Outlined.HighQuality,
                        enabled = !customCommandEnable and !audioSwitch
                    ) { showVideoQualityDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.audio_format),
                        description = audioFormat,
                        icon = Icons.Outlined.AudioFile,
                        enabled = !customCommandEnable and audioSwitch
                    ) { showAudioFormatEditDialog = true }
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
                        enabled = !customCommandEnable,
                        description = stringResource(R.string.download_playlist_desc),
                        isChecked = downloadPlaylist
                    )
                }
                item {
                    var embedSubtitle by remember { mutableStateOf(PreferenceUtil.getValue(SUBTITLE)) }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.embed_subtitles),
                        icon = Icons.Outlined.Subtitles,
                        enabled = !customCommandEnable && !audioSwitch,
                        description = stringResource(id = R.string.embed_subtitles_desc),
                        isChecked = embedSubtitle
                    ) {
                        embedSubtitle = !embedSubtitle
                        PreferenceUtil.updateValue(SUBTITLE, embedSubtitle)
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.aria2),
                        icon = Icons.Outlined.Bolt,
                        description = stringResource(
                            R.string.aria2_desc
                        ),
                        isChecked = aria2c,
                        onClick = {
                            aria2c = !aria2c
                            PreferenceUtil.updateValue(ARIA2C, aria2c)
                        },
                        enabled = !customCommandEnable
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.concurrent_download),
                        description = stringResource(R.string.concurrent_download_desc),
                        icon = Icons.Outlined.Speed,
                        enabled = !customCommandEnable && !audioSwitch && !aria2c,
                    ) { showConcurrentDownloadDialog = true }
                }
                item {
                    PreferenceSwitchWithDivider(title = stringResource(R.string.sponsorblock),
                        description = stringResource(
                            R.string.sponsorblock_desc
                        ),
                        icon = Icons.Outlined.MoneyOff,
                        enabled = !customCommandEnable && !audioSwitch,
                        isChecked = isSponsorBlockEnabled,
                        onChecked = {
                            isSponsorBlockEnabled = !isSponsorBlockEnabled
                            PreferenceUtil.updateValue(SPONSORBLOCK, isSponsorBlockEnabled)
                        },
                        onClick = { showSponsorBlockDialog = true })
                }
                item {
                    var isCookiesEnabled by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(COOKIES)
                        )
                    }
                    PreferenceSwitchWithDivider(title = stringResource(R.string.cookies),
                        description = stringResource(R.string.cookies_desc),
                        isChecked = isCookiesEnabled,
                        icon = Icons.Outlined.Cookie,
                        onChecked = {
                            isCookiesEnabled = !isCookiesEnabled
                            PreferenceUtil.updateValue(COOKIES, isCookiesEnabled)
                        }, onClick = { showCookiesDialog = true })
                }
                item {
                    PreferenceSwitch(title = stringResource(id = R.string.custom_command),
                        description = stringResource(
                            id = R.string.custom_command_desc
                        ),
                        icon = Icons.Outlined.Terminal,
                        isChecked = customCommandEnable,
                        onClick = {
                            customCommandEnable = !customCommandEnable
                            PreferenceUtil.updateValue(CUSTOM_COMMAND, customCommandEnable)
                        })
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.custom_command_template),
                        icon = Icons.Outlined.Code,
                        description = stringResource(R.string.custom_command_template_desc),
                    ) {
                        navigateToTemplate()
                    }
                }
            }
        })
    if (showAudioFormatEditDialog) {
        AudioFormatDialog(onDismissRequest = { showAudioFormatEditDialog = false }) {
            audioFormat = getAudioFormatDesc()
        }
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(onDismissRequest = { showVideoQualityDialog = false }) {
            videoResolution = getVideoResolutionDesc()
        }
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(onDismissRequest = {
            showVideoFormatDialog = false
        }) { videoFormat = getVideoFormatDesc() }
    }
    if (showConcurrentDownloadDialog) {
        ConcurrentDownloadDialog {
            showConcurrentDownloadDialog = false
        }
    }
    if (showSponsorBlockDialog) {
        SponsorBlockDialog {
            showSponsorBlockDialog = false
        }
    }
    if (showCookiesDialog) {
        CookiesDialog {
            showCookiesDialog = false
        }
    }
}


