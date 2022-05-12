package com.junkfood.seal.ui.page.videolist

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.ui.component.BottomDrawer

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoDetailDrawer(videoListViewModel: VideoListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val viewState = videoListViewModel.detailViewState.collectAsState().value
    with(viewState) {
        BottomDrawer(drawerState = drawerState, sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),

                    text = author,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                TextButton(onClick = {}) {
                    Icon(Icons.Outlined.Link, "")
                    Text(
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        text = url, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp), horizontalArrangement = Arrangement.End
                ) {

                    OutlinedButton(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        onClick = {
                            context.startActivity(Intent().apply {
                                action = Intent.ACTION_VIEW
                                data = Uri.parse(url)
                            })
                        }) {
                        Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = "")
                        Text(modifier = Modifier.padding(start = 8.dp), text = "Remove")
                    }

                    FilledTonalButton(
                        onClick = {
                            context.startActivity(Intent().apply {
                                action = Intent.ACTION_VIEW
                                data = Uri.parse(url)
                            })
                        }) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = "")
                        Text(modifier = Modifier.padding(start = 8.dp), text = "Open Url")
                    }

                }

            }
        })
    }

}