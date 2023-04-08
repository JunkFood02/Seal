package com.junkfood.seal.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.junkfood.seal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadTypeItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.NavigateNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

const val verticalPadding = 0

@Composable
@Preview
fun DownloadTypeItemPreview() {
    Column {
        DrawerPreview {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                text = "Download type",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center
            )
            Text(
                text = "This is a description placeholder",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
            )
            DownloadTypeItem(
                modifier = Modifier.padding(bottom = verticalPadding.dp),
                label = stringResource(id = R.string.audio),
                icon = Icons.Outlined.AudioFile,
                description = stringResource(
                    id = R.string.extract_audio_summary
                )
            ) {}
            DownloadTypeItem(
                modifier = Modifier.padding(bottom = verticalPadding.dp),
                label = stringResource(id = R.string.video),
                icon = Icons.Outlined.VideoFile,
                description = stringResource(
                    id = R.string.extract_audio_summary
                )
            ) {}
            DownloadTypeItem(
                modifier = Modifier,
                label = stringResource(id = R.string.custom_command),
                icon = Icons.Outlined.Terminal,
                description = stringResource(
                    id = R.string.custom_command_desc
                )
            ) {}
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            DownloadTypeItem(
                modifier = Modifier,
                label = "Default",
                icon = Icons.Outlined.DownloadDone,
                description = "Download with the current settings"
            ) {}
        }
    }
}

@Composable
fun DrawerPreview(
    modifier: Modifier = Modifier,
    sheetContent: @Composable () -> Unit = {}
) {
    Column {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(
                topStart = 28.0.dp,
                topEnd = 28.0.dp,
                bottomEnd = 0.0.dp,
                bottomStart = 0.0.dp
            )
        ) {
            Box(modifier = Modifier.padding()) {
                Row(
                    modifier = modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = modifier
                            .size(32.dp, 4.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.4f
                                )
                            )
                            .zIndex(1f)
                    ) {}
                }
                Column(modifier = Modifier) {
                    Spacer(modifier = Modifier.height(40.dp))
                    sheetContent()
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
        NavigationBarSpacer(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                .fillMaxWidth()
        )
    }
}