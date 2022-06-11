package com.junkfood.seal.ui.page.settings.download

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CONCURRENT
import kotlin.math.roundToInt

@Composable
fun CommandTemplateDialog(
    onDismissRequest: () -> Unit,
    confirmationCallback: () -> Unit = {},
) {
    val context = LocalContext.current
    val ytdlpReference = "https://github.com/yt-dlp/yt-dlp#usage-and-options"
    var template by remember { mutableStateOf(PreferenceUtil.getTemplate()) }

    AlertDialog(
        title = { Text(stringResource(R.string.edit_custom_command_template)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                PreferenceUtil.updateString(PreferenceUtil.TEMPLATE, template)
                confirmationCallback()
                onDismissRequest()
            }) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        }, text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = stringResource(R.string.edit_template_desc))
                OutlinedTextField(
                    modifier = Modifier.padding(vertical = 12.dp),
                    value = template,
                    onValueChange = { template = it },
                    label = { Text(stringResource(R.string.custom_command_template)) })

                TextButton(
                    onClick = {
                        context.startActivity(Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(ytdlpReference)
                        })
                    },
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
        })
}

@Composable
fun ConcurrentDownloadDialog(
    onDismissRequest: () -> Unit,
) {
    var concurrentFragments by remember { mutableStateOf(PreferenceUtil.getConcurrentFragments()) }
    val count by remember {
        derivedStateOf {
            if (concurrentFragments <= 0.125f) 1 else
                ((concurrentFragments * 4f).roundToInt()) * 4
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        }, confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                PreferenceUtil.updateInt(CONCURRENT, count)
            }) {
                Text(stringResource(R.string.confirm))
            }
        }, title = { Text(stringResource(R.string.concurrent_download)) }, text = {
            Column {
                Text(text = stringResource(R.string.concurrent_download_num, count))
                Slider(
                    value = concurrentFragments,
                    onValueChange = { concurrentFragments = it },
                    steps = 3, valueRange = 0f..1f
                )
            }
        })
}