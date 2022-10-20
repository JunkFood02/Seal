package com.junkfood.seal.ui.page.settings.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.CONCURRENT
import com.junkfood.seal.util.PreferenceUtil.COOKIES_FILE
import com.junkfood.seal.util.PreferenceUtil.MAX_RATE
import com.junkfood.seal.util.PreferenceUtil.SPONSORBLOCK_CATEGORIES
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val ytdlpReference = "https://github.com/yt-dlp/yt-dlp#usage-and-options"
const val sponsorBlockReference = "https://github.com/yt-dlp/yt-dlp#sponsorblock-options"

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CommandTemplateDialog(
    commandTemplate: CommandTemplate = CommandTemplate(0, "", ""),
    newTemplate: Boolean = false,
    onDismissRequest: () -> Unit = {},
    confirmationCallback: () -> Unit = {},
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var templateText by remember { mutableStateOf(commandTemplate.template) }
    var templateName by remember { mutableStateOf(commandTemplate.name) }
    var isError by remember { mutableStateOf(false) }
    AlertDialog(
        icon = { Icon(if (newTemplate) Icons.Outlined.Add else Icons.Outlined.EditNote, null) },
        title = {
            Text(
                stringResource(if (newTemplate) R.string.new_template else R.string.edit_custom_command_template)
            )
        },
        onDismissRequest = {},
        confirmButton = {
            ConfirmButton {
                if (templateName.isBlank() || templateName.isEmpty()) {
                    isError = true
                } else {
                    scope.launch {
                        if (newTemplate) {
                            DatabaseUtil.insertTemplate(
                                CommandTemplate(0, templateName, templateText)
                            )
                        } else {
                            DatabaseUtil.updateTemplate(
                                commandTemplate.copy(
                                    name = templateName, template = templateText
                                )
                            )
                        }
                    }
                    confirmationCallback()
                    onDismissRequest()
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.edit_template_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    modifier = Modifier.padding(top = 16.dp),
                    value = templateName,
                    onValueChange = {
                        templateName = it
                        isError = false
                    },
                    label = { Text(stringResource(R.string.template_label)) },
                    maxLines = 1,
                    isError = isError
                )
                OutlinedTextField(
                    modifier = Modifier.padding(vertical = 12.dp),
                    value = templateText,
                    onValueChange = { templateText = it },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText().toString().let { templateText = it }
                        }) { Icon(Icons.Outlined.ContentPaste, stringResource(R.string.paste)) }
                    },
                    label = { Text(stringResource(R.string.custom_command_template)) },
                    maxLines = 12
                )
                LinkButton()
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
            if (concurrentFragments <= 0.125f) 1 else ((concurrentFragments * 4f).roundToInt()) * 4
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dismiss))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                PreferenceUtil.updateInt(CONCURRENT, count)
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        icon = { Icon(Icons.Outlined.OfflineBolt, null) },
        title = { Text(stringResource(R.string.concurrent_download)) },
        text = {
            Column {
                Text(text = stringResource(R.string.concurrent_download_num, count))
                Slider(
                    value = concurrentFragments,
                    onValueChange = { concurrentFragments = it },
                    steps = 3,
                    valueRange = 0f..1f
                )
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorBlockDialog(onDismissRequest: () -> Unit) {
    var categories by remember {
        mutableStateOf(PreferenceUtil.getSponsorBlockCategories())
    }
    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.MoneyOff, null)
    }, title = { Text(stringResource(R.string.sponsorblock)) }, text = {
        Column {
            Text(
                stringResource(R.string.sponsorblock_categories_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                value = categories,
                label = { Text(stringResource(R.string.sponsorblock_categories)) },
                onValueChange = { categories = it })
            LinkButton(link = sponsorBlockReference)
        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            onDismissRequest()
            PreferenceUtil.updateString(SPONSORBLOCK_CATEGORIES, categories)
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateLimitDialog(onDismissRequest: () -> Unit) {
    var maxRate by remember {
        mutableStateOf(PreferenceUtil.getMaxDownloadRate().toString())
    }
    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Speed, null)
    }, title = { Text(stringResource(R.string.rate_limit)) }, text = {
        Column {
            Text(
                stringResource(R.string.rate_limit_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                value = maxRate,
                label = { Text(stringResource(R.string.max_rate)) },
                onValueChange = {
                    if (it.isDigitsOnly()) maxRate = it
                }, trailingIcon = { Text("K") })
            LinkButton(link = sponsorBlockReference)
        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            onDismissRequest()
            PreferenceUtil.updateInt(MAX_RATE, maxRate.toInt())
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookiesDialog(onDismissRequest: () -> Unit) {
    var cookies by remember {
        mutableStateOf(PreferenceUtil.getCookies())
    }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Cookie, null)
    }, title = { Text(stringResource(R.string.cookies)) }, text = {
        Column {
            Text(
                stringResource(R.string.cookies_desc),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                value = cookies,
                label = { Text(stringResource(R.string.cookies_file_name)) },
                onValueChange = { cookies = it }, trailingIcon = {
                    IconButton(onClick = {
                        clipboardManager.getText().toString().let { cookies = it }
                    }) { Icon(Icons.Outlined.ContentPaste, stringResource(R.string.paste)) }
                }, maxLines = 5
            )
//            LinkButton(link = sponsorBlockReference)
        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton {
            onDismissRequest()
            PreferenceUtil.updateString(COOKIES_FILE, cookies)
        }
    })
}