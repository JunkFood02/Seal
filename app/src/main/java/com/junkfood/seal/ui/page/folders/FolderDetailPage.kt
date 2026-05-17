package com.junkfood.seal.ui.page.folders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.database.objects.DownloadedVideoInfo
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.MediaListItem
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailPage(
    folderId: Int,
    folderName: String,
    onNavigateBack: () -> Unit,
) {
    val videosFlow: Flow<List<DownloadedVideoInfo>> = remember(folderId) {
        DatabaseUtil.getVideosByFolderFlow(folderId)
    }
    val videos by videosFlow.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderName.ifBlank { "Folder" }) },
                navigationIcon = { BackButton(onClick = onNavigateBack) },
            )
        }
    ) { paddingValues ->
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No downloads in this folder",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(videos, key = { it.id }) { video ->
                    MediaListItem(
                        title = video.videoTitle,
                        author = video.videoAuthor,
                        thumbnailUrl = video.thumbnailUrl,
                        videoPath = video.videoPath,
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
