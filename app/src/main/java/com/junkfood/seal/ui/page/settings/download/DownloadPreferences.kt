package com.junkfood.seal.ui.page.settings.download

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import com.junkfood.seal.BaseApplication.Companion.updateDownloadDir
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.Subtitle
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil.DEBUG
import com.junkfood.seal.util.PreferenceUtil.EXTRACT_AUDIO
import com.junkfood.seal.util.PreferenceUtil.OPEN_IMMEDIATELY
import com.junkfood.seal.util.PreferenceUtil.PLAYLIST
import com.junkfood.seal.util.PreferenceUtil.TEMPLATE
import com.junkfood.seal.util.PreferenceUtil.THUMBNAIL
import com.junkfood.seal.util.PreferenceUtil.getAudioFormatDesc
import com.junkfood.seal.util.PreferenceUtil.getVideoFormatDesc
import com.junkfood.seal.util.PreferenceUtil.getVideoQualityDesc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun DownloadPreferences(navController: NavController) {
    val context = LocalContext.current
    val ytdlpReference = "https://github.com/yt-dlp/yt-dlp#usage-and-options"
    var downloadDirectoryText by remember { mutableStateOf(downloadDir) }

    var showTemplateEditDialog by remember { mutableStateOf(false) }
    var showAudioFormatEditDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }

    var customCommandTemplate by remember { mutableStateOf(PreferenceUtil.getTemplate()) }
    var displayErrorReport by remember { mutableStateOf(PreferenceUtil.getValue(DEBUG)) }
    var downloadPlaylist by remember { mutableStateOf(PreferenceUtil.getValue(PLAYLIST)) }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val launcher =
        rememberLauncherForActivityResult(object : ActivityResultContracts.OpenDocumentTree() {
            override fun createIntent(context: Context, input: Uri?): Intent {
                return (super.createIntent(context, input)).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                }
            }
        }) {
            it?.let {
                val path = FileUtil.getRealPath(it)
                updateDownloadDir(path)
                downloadDirectoryText = path
            }
        }

    fun openDirectoryChooser() {
        when (storagePermission.status) {
            is PermissionStatus.Granted -> {
                launcher.launch(null)
            }
            is PermissionStatus.Denied -> {
                storagePermission.launchPermissionRequest()
            }
        }
    }

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarScrollState()
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
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                    Subtitle(text = stringResource(id = R.string.general_settings))
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.download_directory),
                        description = downloadDirectoryText,
                        icon = null, enabled = true
                    ) {
                        openDirectoryChooser()
                    }
                }
                item {
                    var ytdlpVersion by remember {
                        mutableStateOf(BaseApplication.ytdlpVersion)
                    }
                    PreferenceItem(
                        title = stringResource(id = R.string.ytdlp_version),
                        description = ytdlpVersion,
                        icon = null, enabled = true
                    ) {
                        CoroutineScope(Job()).launch {
                            ytdlpVersion = DownloadUtil.updateYtDlp()
                        }
                    }
                }

                item {
                    var configureBeforeDownload by remember {
                        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.CONFIGURE, true))
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.settings_before_download),
                        description = stringResource(
                            id = R.string.settings_before_download_desc
                        ), enabled = !customCommandEnable,
                        icon = null,
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
                        icon = null,
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
                        icon = null,
                        isChecked = thumbnailSwitch,
                        onClick = {
                            thumbnailSwitch = !thumbnailSwitch
                            PreferenceUtil.updateValue(THUMBNAIL, thumbnailSwitch)
                        }
                    )
                }

                item {
                    var openSwitch by remember {
                        mutableStateOf(
                            PreferenceUtil.getValue(
                                OPEN_IMMEDIATELY
                            )
                        )
                    }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.open_when_finish),
                        description = stringResource(
                            id = R.string.open_when_finish_summary
                        ), enabled = !customCommandEnable,
                        icon = null,
                        isChecked = openSwitch,
                        onClick = {
                            openSwitch = !openSwitch
                            PreferenceUtil.updateValue(OPEN_IMMEDIATELY, openSwitch)
                        }
                    )
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
                        },
                        enabled = !customCommandEnable,
                        description = stringResource(R.string.download_playlist_desc),
                        isChecked = downloadPlaylist
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.error_report),
                        description = stringResource(R.string.error_report_desc),
                        enabled = !customCommandEnable,
                        icon = null,
                        onClick = {
                            displayErrorReport = !displayErrorReport
                            PreferenceUtil.updateValue(DEBUG, displayErrorReport)
                        },
                        isChecked = displayErrorReport
                    )
                }
                item {
                    Subtitle(text = stringResource(id = R.string.format))
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.video_format_preference),
                        description = getVideoFormatDesc(),
                        enabled = !customCommandEnable and !audioSwitch
                    ) { showVideoFormatDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.quality),
                        description = getVideoQualityDesc(),
                        icon = null,
                        enabled = !customCommandEnable and !audioSwitch
                    ) { showVideoQualityDialog = true }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.audio_format),
                        description = getAudioFormatDesc(),
                        icon = null,
                        enabled = !customCommandEnable and audioSwitch
                    ) { showAudioFormatEditDialog = true }
                }

                item {
                    Subtitle(text = stringResource(R.string.advanced_settings))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.custom_command),
                        description = stringResource(
                            id = R.string.custom_command_desc
                        ),
                        icon = null,
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
                        icon = null,
                        enabled = customCommandEnable
                    ) { showTemplateEditDialog = true }
                }
            }
        }
    )
    if (showTemplateEditDialog) {
        val temp = PreferenceUtil.getTemplate()
        CommandTemplateDialog(
            onDismissRequest = {
                customCommandTemplate = temp
                showTemplateEditDialog = false
            },
            confirmationCallback = {
                PreferenceUtil.updateString(TEMPLATE, customCommandTemplate)
                showTemplateEditDialog = false
            },
            onValueChange = { s -> customCommandTemplate = s },
            template = customCommandTemplate
        ) {
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(ytdlpReference)
            })
        }
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
}


