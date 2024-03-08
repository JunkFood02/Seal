package com.junkfood.seal.ui.page.videolist


import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DialogSubtitle
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SingleSelectChip
import com.junkfood.seal.database.backup.BackupUtil.BackupDestination
import com.junkfood.seal.database.backup.BackupUtil.BackupType
import com.junkfood.seal.ui.theme.SealTheme

@Composable
fun ExportDialog(
    modifier: Modifier = Modifier,
    itemCount: Int = 0,
    onDismissRequest: () -> Unit = {},
    onExport: (BackupType, BackupDestination) -> Unit
) {
    var type by remember { mutableStateOf(BackupType.DownloadHistory) }
    var destination by remember { mutableStateOf(BackupDestination.File) }
    SealDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest, confirmButton = {
            Button(onClick = { onExport(type, destination) }) {
                Text(text = stringResource(id = R.string.export_backup))
            }
        }, dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.export_download_history))
        },
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
                contentDescription = null
            )
        },
        text = {
            Column {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.export_download_history_msg).format(
                        pluralStringResource(id = R.plurals.item_count, count = itemCount).format(
                            itemCount
                        )
                    )
                )
                DialogSubtitle(
                    modifier = Modifier,
                    text = stringResource(id = R.string.backup_type)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SingleSelectChip(
                            selected = type == BackupType.DownloadHistory,
                            onClick = { type = BackupType.DownloadHistory },
                            label = {
                                Text(
                                    stringResource(
                                        id =
                                        R.string.full_backup
                                    )
                                )
                            }
                        )
                    }
                    item {
                        SingleSelectChip(
                            selected = type == BackupType.URLList,
                            onClick = { type = BackupType.URLList }, label = {
                                Text(
                                    text = stringResource(
                                        id = R.string.video_url
                                    )
                                )
                            }
                        )
                    }
                }
                DialogSubtitle(
                    modifier = Modifier,
                    text = stringResource(id = R.string.export_to)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SingleSelectChip(
                            selected = destination == BackupDestination.File,
                            onClick = { destination = BackupDestination.File },
                            label = {
                                Text(
                                    stringResource(
                                        id =
                                        R.string.file
                                    )
                                )
                            }
                        )
                    }
                    item {
                        SingleSelectChip(
                            selected = destination == BackupDestination.Clipboard,
                            onClick = { destination = BackupDestination.Clipboard }, label = {
                                Text(
                                    text = stringResource(
                                        id = R.string.clipboard
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ImportDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onImport: (BackupDestination) -> Unit
) {
    var destination by remember { mutableStateOf(BackupDestination.File) }

    SealDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest, confirmButton = {
            Button(onClick = { onImport(destination) }) {
                Text(text = stringResource(id = R.string.import_backup))
            }
        }, dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.import_download_history))
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Restore,
                contentDescription = null
            )
        },
        text = {
            Column {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(R.string.import_download_history_msg)
                )
                DialogSubtitle(
                    modifier = Modifier,
                    text = stringResource(id = R.string.backup_type)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SingleSelectChip(
                            selected = true, onClick = {}, label = {
                                Text(
                                    stringResource(
                                        id =
                                        R.string.full_backup
                                    )
                                )
                            }
                        )
                    }
                }
                DialogSubtitle(
                    modifier = Modifier,
                    text = stringResource(id = R.string.import_from)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SingleSelectChip(
                            selected = destination == BackupDestination.File,
                            onClick = { destination = BackupDestination.File },
                            label = {
                                Text(
                                    stringResource(
                                        id =
                                        R.string.file
                                    )
                                )
                            }
                        )
                    }
                    item {
                        SingleSelectChip(
                            selected = destination == BackupDestination.Clipboard,
                            onClick = { destination = BackupDestination.Clipboard },
                            label = {
                                Text(
                                    text = stringResource(
                                        id = R.string.clipboard
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    )

}

@Preview
@Composable
private fun PreviewExport() {
    SealTheme {
        ExportDialog() { _, _ -> }
    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun PreviewImport() {
    SealTheme {
        ImportDialog() { _ -> }
    }

}