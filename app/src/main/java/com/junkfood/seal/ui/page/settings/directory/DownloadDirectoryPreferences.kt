@file:OptIn(ExperimentalPermissionsApi::class)

package com.junkfood.seal.ui.page.settings.directory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.SdCard
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.App
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.ui.component.PreferenceInfo
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.ui.component.PreferencesHintCard
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.CUSTOM_PATH
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getConfigDirectory
import com.junkfood.seal.util.FileUtil.getSdcardTempDir
import com.junkfood.seal.util.FileUtil.getTempDir
import com.junkfood.seal.util.OUTPUT_PATH_TEMPLATE
import com.junkfood.seal.util.PRIVATE_DIRECTORY
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.SDCARD_DOWNLOAD
import com.junkfood.seal.util.SDCARD_URI
import com.junkfood.seal.util.SUBDIRECTORY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ytdlpOutputTemplateReference = "https://github.com/yt-dlp/yt-dlp#output-template"
private const val validDirectoryRegex = "/storage/emulated/0/(Download|Documents)"

private fun String.isValidDirectory(): Boolean {
    return this.contains(Regex(validDirectoryRegex))
}

private enum class Directory {
    AUDIO, VIDEO, SDCARD
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalComposeUiApi::class
)
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
            by remember { mutableStateOf(PreferenceUtil.getValue(SUBDIRECTORY)) }
    var isCustomPathEnabled
            by remember { mutableStateOf(PreferenceUtil.getValue(CUSTOM_PATH)) }

    var isPrivateDirectoryEnabled by remember {
        mutableStateOf(PreferenceUtil.getValue(PRIVATE_DIRECTORY))
    }

    var videoDirectoryText by remember(isPrivateDirectoryEnabled) {
        mutableStateOf(if (!isPrivateDirectoryEnabled) App.videoDownloadDir else App.getPrivateDownloadDirectory())
    }
    var audioDirectoryText by remember(isPrivateDirectoryEnabled) {
        mutableStateOf(if (!isPrivateDirectoryEnabled) App.audioDownloadDir else App.getPrivateDownloadDirectory())
    }
    var sdcardUri by remember {
        mutableStateOf(SDCARD_URI.getString())
    }
    var sdcardDownload by remember {
        mutableStateOf(PreferenceUtil.getValue(SDCARD_DOWNLOAD))
    }

    var pathTemplateText by remember { mutableStateOf(PreferenceUtil.getOutputPathTemplate()) }


    var showEditDialog by remember { mutableStateOf(false) }
    var showClearTempDialog by remember { mutableStateOf(false) }

    var editingDirectory by remember { mutableStateOf(Directory.VIDEO) }

    val isCustomCommandEnabled by remember {
        mutableStateOf(
            PreferenceUtil.getValue(CUSTOM_COMMAND)
        )
    }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val showDirectoryAlert =
        Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()
                && (!audioDirectoryText.isValidDirectory() || !videoDirectoryText.isValidDirectory())

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
                if (editingDirectory == Directory.SDCARD) {
                    sdcardUri = it.toString()
                    PreferenceUtil.encodeString(SDCARD_URI, it.toString())
                    context.contentResolver?.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    return@let
                }
                val path = FileUtil.getRealPath(it)
                App.updateDownloadDir(path, isAudio = editingDirectory == Directory.AUDIO)
                if (editingDirectory == Directory.AUDIO)
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
                        modifier = Modifier,
                        text = stringResource(id = R.string.download_directory),
                    )
                }, navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {

                if (isCustomCommandEnabled)
                    item {
                        PreferenceInfo(text = stringResource(id = R.string.custom_command_enabled_hint))
                    }

                if (showDirectoryAlert)
                    item {
                        PreferencesHintCard(
                            title = stringResource(R.string.permission_issue),
                            description = stringResource(R.string.permission_issue_desc),
                            icon = Icons.Filled.SdCardAlert,
                            isDarkTheme = LocalDarkTheme.current.isDarkTheme()
                        ) {
                            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    data = Uri.parse("package:" + context.packageName)
                                    if (resolveActivity(context.packageManager) != null)
                                        context.startActivity(this)
                                }
                            }
                        }
                    }
                item {
                    PreferenceSubtitle(text = stringResource(R.string.general_settings))
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_directory),
                        description = videoDirectoryText,
                        enabled = !isPrivateDirectoryEnabled && !sdcardDownload,
                        icon = Icons.Outlined.VideoLibrary
                    ) {
                        editingDirectory = Directory.VIDEO
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_directory),
                        description = audioDirectoryText,
                        enabled = !isPrivateDirectoryEnabled && !isCustomCommandEnabled && !sdcardDownload,
                        icon = Icons.Outlined.LibraryMusic
                    ) {
                        editingDirectory = Directory.AUDIO
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(id = R.string.sdcard_directory),
                        description = sdcardUri,
                        isChecked = sdcardDownload,
                        enabled = !isCustomCommandEnabled,
                        isSwitchEnabled = !isCustomCommandEnabled && sdcardUri.isNotBlank(),
                        onChecked = {
                            sdcardDownload = !sdcardDownload
                            PreferenceUtil.updateValue(SDCARD_DOWNLOAD, sdcardDownload)
                        },
                        icon = Icons.Outlined.SdCard,
                        onClick = {
                            editingDirectory = Directory.SDCARD
                            openDirectoryChooser()
                        }
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.subdirectory),
                        description = stringResource(id = R.string.subdirectory_desc),
                        icon = Icons.Outlined.SnippetFolder,
                        isChecked = isSubdirectoryEnabled,
                        enabled = !isCustomCommandEnabled && !sdcardDownload,
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
                        enabled = !showDirectoryAlert && !sdcardDownload,
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
                        enabled = !isCustomCommandEnabled && !sdcardDownload,
                        isChecked = isCustomPathEnabled,
                        onChecked = {
                            isCustomPathEnabled = !isCustomPathEnabled
                            PreferenceUtil.updateValue(CUSTOM_PATH, isCustomPathEnabled)
                        },
                        onClick = { showEditDialog = true }
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
                            FileUtil.clearTempFiles(context.getTempDir()) +
                                    FileUtil.clearTempFiles(context.getSdcardTempDir(null))
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
            title = { Text(stringResource(R.string.edit)) },
            dismissButton = {
                DismissButton { showEditDialog = false }
            },
            confirmButton = {
                ConfirmButton {
                    showEditDialog = false
                    PreferenceUtil.encodeString(OUTPUT_PATH_TEMPLATE, pathTemplateText)
                }
            }, icon = { Icon(Icons.Outlined.Edit, null) },
            text = {
                val focusManager = LocalFocusManager.current
                val softwareKeyboardController = LocalSoftwareKeyboardController.current
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
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    LinkButton()
                }
            }
        )
    }
}