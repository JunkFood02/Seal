@file:OptIn(ExperimentalPermissionsApi::class)

package com.junkfood.seal.ui.page.settings.download

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_PATH
import com.junkfood.seal.util.PreferenceUtil.OUTPUT_PATH_TEMPLATE
import com.junkfood.seal.util.PreferenceUtil.SUBDIRECTORY


const val ytdlpOutputTemplateReference = "https://github.com/yt-dlp/yt-dlp#output-template"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DownloadDirectoryPreferences(onBackPressed: () -> Unit) {

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    var videoDirectoryText by remember { mutableStateOf(BaseApplication.videoDownloadDir) }
    var audioDirectoryText by remember { mutableStateOf(BaseApplication.audioDownloadDir) }
    var pathTemplateText by remember { mutableStateOf(PreferenceUtil.getOutputPathTemplate()) }
    var isSubdirectoryEnabled
            by remember { mutableStateOf(PreferenceUtil.getValue(SUBDIRECTORY, false)) }
    var isCustomPathEnabled
            by remember { mutableStateOf(PreferenceUtil.getValue(CUSTOM_PATH, false)) }
    var showEditDialog by remember { mutableStateOf(false) }

    var isEditingAudioDirectory = false
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
                BaseApplication.updateDownloadDir(path, isAudio = isEditingAudioDirectory)
                if (isEditingAudioDirectory)
                    audioDirectoryText = path
                else videoDirectoryText = path
            }
        }

    fun openDirectoryChooser() {
        if (Build.VERSION.SDK_INT > 29 || storagePermission.status == PermissionStatus.Granted)
            launcher.launch(null)
        else storagePermission.launchPermissionRequest()
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
                        text = stringResource(id = R.string.download_directory),
                    )
                }, navigationIcon = {
                    BackButton(modifier = Modifier.padding(start = 8.dp)) {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                if (Build.VERSION.SDK_INT > 29)
                    item {
                        PreferencesCaution(
                            title = stringResource(R.string.permission_issue),
                            description = stringResource(R.string.permission_issue_desc),
                            icon = Icons.Filled.SdCardAlert
                        ) { uriHandler.openUri("https://github.com/JunkFood02/Seal/issues/34") }
                    }
                item {
                    PreferenceSubtitle(text = stringResource(R.string.general_settings))
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_directory),
                        description = videoDirectoryText,
                        icon = Icons.Outlined.VideoLibrary
                    ) {
                        isEditingAudioDirectory = false
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_directory),
                        description = audioDirectoryText,
                        icon = Icons.Outlined.LibraryMusic
                    ) {
                        isEditingAudioDirectory = true
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.subdirectory),
                        description = stringResource(id = R.string.subdirectory_desc),
                        icon = Icons.Outlined.SnippetFolder, isChecked = isSubdirectoryEnabled,
                    ) {
                        isSubdirectoryEnabled = !isSubdirectoryEnabled
                        PreferenceUtil.updateValue(SUBDIRECTORY, isSubdirectoryEnabled)
                    }
                }
                item {
                    PreferenceSubtitle(text = stringResource(R.string.advanced_settings))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.custom_output_path),
                        description = stringResource(R.string.custom_output_path_desc),
                        icon = Icons.Outlined.FolderSpecial,
                        isChecked = isCustomPathEnabled
                    ) {
                        isCustomPathEnabled = !isCustomPathEnabled
                        PreferenceUtil.updateValue(CUSTOM_PATH, isCustomPathEnabled)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.download_path_template),
                        description = pathTemplateText,
                        icon = Icons.Outlined.Code,
                        enabled = isCustomPathEnabled
                    ) { showEditDialog = true }
                }
            }
        })

    val onDismissRequest = {
        showEditDialog = false
        pathTemplateText = PreferenceUtil.getOutputPathTemplate()
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            title = { Text(stringResource(R.string.edit_custom_command_template)) },
            dismissButton = { DismissButton { onDismissRequest() } },
            confirmButton = {
                ConfirmButton {
                    showEditDialog = false
                    PreferenceUtil.updateString(OUTPUT_PATH_TEMPLATE, pathTemplateText)
                }
            }, icon = { Icon(Icons.Outlined.Edit, null) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.edit_custom_path_template_desc),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        modifier = Modifier.padding(vertical = 12.dp),
                        value = pathTemplateText,
                        onValueChange = { pathTemplateText = it },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.getText()?.let { pathTemplateText = it.text }
                            }) { Icon(Icons.Outlined.ContentPaste, stringResource(R.string.paste)) }
                        },
                        label = { Text(stringResource(R.string.download_path_template)) },
                        maxLines = 1
                    )
                    TextButton(
                        onClick = { uriHandler.openUri(ytdlpOutputTemplateReference) },
                    ) {
                        Row {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = stringResource(R.string.yt_dlp_docs)
                            )
                        }
                    }
                }
            }
        )
    }
}