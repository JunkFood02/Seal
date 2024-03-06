package com.junkfood.seal.ui.page.videolist


import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DialogSubtitle
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SingleSelectChip
import com.junkfood.seal.ui.theme.SealTheme

@Composable
fun ExportDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit = {}) {
    SealDialog(
        onDismissRequest = onDismissRequest, confirmButton = {
            Button(onClick = { /*TODO*/ }) {
                Text(text = stringResource(id = R.string.export_backup))
            }
        }, dismissButton = {
            OutlinedButton(onClick = { /*TODO*/ }) {
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
                    text = stringResource(R.string.export_download_history_msg)
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
                            selected = true, onClick = { /*TODO*/ }, label = {
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
                            selected = false, onClick = { /*TODO*/ }, label = {
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
                            selected = true, onClick = { /*TODO*/ }, label = {
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
                            selected = false, onClick = { /*TODO*/ }, label = {
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
fun ImportDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit = {}) {
    SealDialog(
        onDismissRequest = onDismissRequest, confirmButton = {
            Button(onClick = { /*TODO*/ }) {
                Text(text = stringResource(id = R.string.import_backup))
            }
        }, dismissButton = {
            OutlinedButton(onClick = { /*TODO*/ }) {
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
                            selected = true, onClick = { /*TODO*/ }, label = {
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
                            selected = true, onClick = { /*TODO*/ }, label = {
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
                            selected = false, onClick = { /*TODO*/ }, label = {
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
        ExportDialog()
    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun PreviewImport() {
    SealTheme {
        ImportDialog()
    }

}