@file:OptIn(ExperimentalPermissionsApi::class)

package com.junkfood.seal.ui.page.settings.general

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.SnippetFolder
import androidx.compose.material.icons.outlined.TabUnselected
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.ui.component.PreferencesCaution
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getConfigDirectory
import com.junkfood.seal.util.FileUtil.getTempDir
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CUSTOM_PATH
import com.junkfood.seal.util.PreferenceUtil.OUTPUT_PATH_TEMPLATE
import com.junkfood.seal.util.PreferenceUtil.PRIVATE_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil.SUBDIRECTORY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ytdlpOutputTemplateReference = "https://github.com/yt-dlp/yt-dlp#output-template"
private const val validDirectoryRegex = "/storage/emulated/0/(Download|Documents)"

private fun String.isValidDirectory(): Boolean {
    return this.contains(Regex(validDirectoryRegex))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DownloadDirectoryPreferences(onBackPressed: () -> Unit) {

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isSubdirectoryEnabled
            by remember { mutableStateOf(PreferenceUtil.getValue(SUBDIRECTORY, false)) }
    var isCustomPathEnabled
            by remember { mutableStateOf(PreferenceUtil.getValue(CUSTOM_PATH, false)) }

    var isPrivateDirectoryEnabled by remember {
        mutableStateOf(PreferenceUtil.getValue(PRIVATE_DIRECTORY))
    }

    var videoDirectoryText by remember(isPrivateDirectoryEnabled) {
        mutableStateOf(if (!isPrivateDirectoryEnabled) BaseApplication.videoDownloadDir else BaseApplication.getPrivateDownloadDirectory())
    }
    var audioDirectoryText by remember(isPrivateDirectoryEnabled) {
        mutableStateOf(if (!isPrivateDirectoryEnabled) BaseApplication.audioDownloadDir else BaseApplication.getPrivateDownloadDirectory())
    }

    var pathTemplateText by remember { mutableStateOf(PreferenceUtil.getOutputPathTemplate()) }


    var showEditDialog by remember { mutableStateOf(false) }
    var showClearTempDialog by remember { mutableStateOf(false) }

    var isEditingAudioDirectory = false
    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val showDirectoryAlert =
        Build.VERSION.SDK_INT >= 30 && (!audioDirectoryText.isValidDirectory() || !videoDirectoryText.isValidDirectory())

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
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.systemBarsPadding(),
                hostState = snackbarHostState
            )
        },
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
                if (showDirectoryAlert)
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
                        description = videoDirectoryText, enabled = !isPrivateDirectoryEnabled,
                        icon = Icons.Outlined.VideoLibrary
                    ) {
                        isEditingAudioDirectory = false
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_directory),
                        description = audioDirectoryText, enabled = !isPrivateDirectoryEnabled,
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
                    PreferenceSubtitle(text = stringResource(R.string.privacy))
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.private_directory),
                        description = stringResource(
                            R.string.private_directory_desc
                        ),
                        icon = Icons.Outlined.TabUnselected,
                        enabled = !showDirectoryAlert,
                        isChecked = isPrivateDirectoryEnabled,
                        onClick = {
                            isPrivateDirectoryEnabled = !isPrivateDirectoryEnabled
                            PreferenceUtil.updateValue(PRIVATE_DIRECTORY, isPrivateDirectoryEnabled)
                        }
                    )
                }
                item {
                    PreferenceSubtitle(text = stringResource(R.string.advanced_settings))
                }
                item {
                    PreferenceSwitchWithDivider(title = stringResource(R.string.custom_output_path),
                        description = stringResource(R.string.custom_output_path_desc),
                        icon = Icons.Outlined.FolderSpecial,
                        isChecked = isCustomPathEnabled, onChecked = {
                            isCustomPathEnabled = !isCustomPathEnabled
                            PreferenceUtil.updateValue(CUSTOM_PATH, isCustomPathEnabled)
                        }, onClick = { showEditDialog = true }
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.clear_temp_files),
                        description = stringResource(
                            R.string.clear_temp_files_desc
                        ),
                        icon = Icons.Outlined.FolderDelete,
                        onClick = { showClearTempDialog = true }
                    )
                }
            }
        })


    if (showClearTempDialog) {
        AlertDialog(
            onDismissRequest = { showClearTempDialog = false },
            icon = { Icon(Icons.Outlined.FolderDelete, null) },
            title = { Text(stringResource(id = R.string.clear_temp_files)) },
            dismissButton = {
                DismissButton { showClearTempDialog = false }
            },
            text = {
                Text(
                    stringResource(R.string.clear_temp_files_info),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                ConfirmButton {
                    showClearTempDialog = false
                    scope.launch(Dispatchers.IO) {
                        FileUtil.clearTempFiles(context.getConfigDirectory())
                        val count =
                            FileUtil.clearTempFiles(context.getTempDir())
                        withContext(Dispatchers.Main) {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.clear_temp_files_count).format(count)
                            )
                        }
                    }
                }
            })
    }
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_custom_command_template)) },
            dismissButton = {
                DismissButton { showEditDialog = false }
            },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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