package com.junkfood.seal.ui.page.settings.download

import android.Manifest
import android.os.Build
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.*
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
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.DEBUG
import com.junkfood.seal.util.PreferenceUtil.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.NOTIFICATION
import com.junkfood.seal.util.PreferenceUtil.PLAYLIST
import com.junkfood.seal.util.PreferenceUtil.THUMBNAIL
import com.junkfood.seal.util.PreferenceUtil.getAudioFormatDesc
import com.junkfood.seal.util.PreferenceUtil.getVideoFormatDesc
import com.junkfood.seal.util.PreferenceUtil.getVideoQualityDesc
import com.junkfood.seal.util.TextUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun DownloadPreferences(onBackPressed: () -> Unit, navigateToDownloadDirectory: () -> Unit) {
    val context = LocalContext.current

    var showTemplateEditDialog by remember { mutableStateOf(false) }
    var showAudioFormatEditDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }
    var showConcurrentDownloadDialog by remember { mutableStateOf(false) }

    var customCommandTemplate by remember { mutableStateOf(PreferenceUtil.getTemplate()) }
    var displayErrorReport by remember { mutableStateOf(PreferenceUtil.getValue(DEBUG)) }
    var downloadPlaylist by remember { mutableStateOf(PreferenceUtil.getValue(PLAYLIST)) }

    var downloadNotification by remember {
        mutableStateOf(PreferenceUtil.getValue(NOTIFICATION))
    }

    val notificationPermission =
        if (Build.VERSION.SDK_INT >= 33)
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) { status ->
                if (!status) TextUtil.makeToast(context.getString(R.string.permission_denied))
            } else null

    fun checkNotificationPermission(): Boolean =
        notificationPermission == null || (notificationPermission.status == PermissionStatus.Granted)

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState(),
        canScroll = { true }
    )
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            com.junkfood.seal.ui.component.LargeTopAppBar(
                title = {
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
        }, content = {
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
                modifier = Modifier
                    .padding(it)
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
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
                        CoroutineScope(Job()).launch {
                            ytdlpVersion = DownloadUtil.updateYtDlp()
                        }
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.download_notification),
                        description = stringResource(
                            id = if (notificationPermission == null || notificationPermission.status == PermissionStatus.Granted) R.string.download_notification_desc
                            else R.string.permission_denied
                        ),
                        icon = if (!checkNotificationPermission())
                            Icons.Outlined.NotificationsOff
                        else if (!downloadNotification) Icons.Outlined.Notifications
                        else Icons.Outlined.NotificationsActive,
                        isChecked = downloadNotification && checkNotificationPermission(),
                        onClick = {
                            notificationPermission?.launchPermissionRequest()
                            if (checkNotificationPermission()) {
                                downloadNotification = !downloadNotification
                                PreferenceUtil.updateValue(
                                    NOTIFICATION,
                                    downloadNotification
                                )
                            }
                        }
                    )
                }

                item {
                    var configureBeforeDownload by remember {
                        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.CONFIGURE, true))
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.settings_before_download),
                        description = stringResource(
                            id = R.string.settings_before_download_desc
                        ),
                        icon = Icons.Outlined.DoneAll,
                        isChecked = configureBeforeDownload,
                        onClick = {
                            configureBeforeDownload = !configureBeforeDownload
                            PreferenceUtil.updateValue(
                                PreferenceUtil.CONFIGURE,
                                configureBeforeDownload
                            )
                        }
                    )
                }

                item {

                    PreferenceSwitch(
                        title = stringResource(id = R.string.extract_audio),
                        description = stringResource(
                            id = R.string.extract_audio_summary
                        ),
                        icon = Icons.Outlined.MusicNote,
                        enabled = !customCommandEnable,
                        isChecked = audioSwitch,
                        onClick = {
                            audioSwitch = !audioSwitch
                            PreferenceUtil.updateValue(EXTRACT_AUDIO, audioSwitch)
                        }
                    )
                }

                item {
                    var thumbnailSwitch by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(THUMBNAIL)
                        )
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.create_thumbnail),
                        description = stringResource(
                            id = R.string.create_thumbnail_summary
                        ),
                        enabled = !customCommandEnable,
                        icon = Icons.Outlined.Image,
                        isChecked = thumbnailSwitch,
                        onClick = {
                            thumbnailSwitch = !thumbnailSwitch
                            PreferenceUtil.updateValue(THUMBNAIL, thumbnailSwitch)
                        }
                    )
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.print_details),
                        description = stringResource(R.string.print_details_desc),
                        enabled = !customCommandEnable,
                        icon = if (displayErrorReport) Icons.Outlined.Print else Icons.Outlined.PrintDisabled,
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
                        description = getVideoFormatDesc(),
                        icon = Icons.Outlined.VideoFile,
                        enabled = !customCommandEnable and !audioSwitch
                    ) { showVideoFormatDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_quality),
                        description = getVideoQualityDesc(),
                        icon = Icons.Outlined._4k,
                        enabled = !customCommandEnable and !audioSwitch
                    ) { showVideoQualityDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.audio_format),
                        description = getAudioFormatDesc(),
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
                                PLAYLIST,
                                downloadPlaylist
                            )
                        }, icon = Icons.Outlined.PlaylistAddCheck,
                        enabled = !customCommandEnable,
                        description = stringResource(R.string.download_playlist_desc),
                        isChecked = downloadPlaylist
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.concurrent_download),
                        description = stringResource(R.string.concurrent_download_desc),
                        icon = Icons.Outlined.Speed,
                        enabled = !customCommandEnable
                    ) { showConcurrentDownloadDialog = true }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.custom_command),
                        description = stringResource(
                            id = R.string.custom_command_desc
                        ),
                        icon = Icons.Outlined.Code,
                        isChecked = customCommandEnable,
                        onClick = {
                            customCommandEnable = !customCommandEnable
                            PreferenceUtil.updateValue(CUSTOM_COMMAND, customCommandEnable)
                        }
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.custom_command_template),
                        description = customCommandTemplate,
                        enabled = customCommandEnable
                    ) { showTemplateEditDialog = true }
                }
            }
        }
    )
    if (showTemplateEditDialog) {
        CommandTemplateDialog(
            onDismissRequest = { showTemplateEditDialog = false },
            confirmationCallback = {
                customCommandTemplate = PreferenceUtil.getTemplate()
            },
        )
    }
    if (showAudioFormatEditDialog) {
        AudioFormatDialog(onDismissRequest = { showAudioFormatEditDialog = false }) {
        }
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(onDismissRequest = { showVideoQualityDialog = false }) {
        }
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(onDismissRequest = { showVideoFormatDialog = false }) {
        }
    }
    if (showConcurrentDownloadDialog) {
        ConcurrentDownloadDialog {
            showConcurrentDownloadDialog = false
        }
    }
}


