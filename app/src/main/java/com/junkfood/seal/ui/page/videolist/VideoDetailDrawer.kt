package com.junkfood.seal.ui.page.videolist

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BottomDrawer
import com.junkfood.seal.ui.component.FilledTonalButtonWithIcon
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.util.ToastUtil

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoDetailDrawer(videoListViewModel: VideoListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val detailViewState = videoListViewModel.detailViewState.collectAsState().value
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    BackHandler(detailViewState.drawerState.targetValue == ModalBottomSheetValue.Expanded) {
        videoListViewModel.hideDrawer(scope)
    }

    with(detailViewState) {
        BottomDrawer(drawerState = drawerState, sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (author != "playlist" && author != "null")
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            text = author,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
            TextButton(modifier = Modifier.padding(vertical = 6.dp), onClick = {
                clipboardManager.setText(AnnotatedString(url))
                ToastUtil.makeToast(context.getString(R.string.link_copied))
            }) {
                Icon(Icons.Outlined.Link, stringResource(R.string.video_url))
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

                OutlinedButtonWithIcon(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = {
                        videoListViewModel.hideDrawer(scope)
                        videoListViewModel.showDialog()
                    },
                    icon = Icons.Outlined.Delete,
                    text = stringResource(R.string.remove)
                )

                FilledTonalButtonWithIcon(
                    onClick = {
                        videoListViewModel.hideDrawer(scope)
                        context.startActivity(Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(url)
                        })
                    },
                    icon = Icons.Outlined.OpenInNew,
                    text = stringResource(R.string.open_url)
                )
            }
        })
    }
    RemoveItemDialog()
}