package com.junkfood.seal.ui.page.settings.command

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.seal.R
import com.junkfood.seal.database.objects.CommandTemplate
import com.junkfood.seal.ui.component.AdjacentLabel
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.LinkButton
import com.junkfood.seal.ui.component.PasteFromClipBoardButton
import com.junkfood.seal.ui.component.ShortcutChip
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.DatabaseUtil
import com.junkfood.seal.util.PreferenceUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun TemplateEditPage(onDismissRequest: () -> Unit, templateId: Int) {
    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val commandTemplate =
        PreferenceUtil.templateListStateFlow.collectAsState().value.find { it.id == templateId }
            ?: CommandTemplate(0, "", "")

    var templateText by remember { mutableStateOf(commandTemplate.template) }
    var templateName by remember { mutableStateOf(commandTemplate.name) }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    var isEditingShortcuts by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(if (templateId <= 0) R.string.new_template else R.string.edit),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
            }, navigationIcon = {
                BackButton { onDismissRequest() }
            }, actions = {
                TextButton(
                    modifier = Modifier.padding(end = 8.dp), onClick = {
                        scope.launch {
                            commandTemplate.copy(name = templateName, template = templateText).run {
                                if (id == 0) DatabaseUtil.insertTemplate(this)
                                else DatabaseUtil.updateTemplate(this)
                            }

                            onDismissRequest()
                        }
                    }, enabled = templateName.isNotEmpty()
                ) {
                    Text(text = stringResource(androidx.appcompat.R.string.abc_action_mode_done))
                }
            }, scrollBehavior = scrollBehavior
            )
        }) { paddings ->
        LazyColumn(
            modifier = Modifier.padding(paddings), contentPadding = PaddingValues()
        ) {

            item {
                val description = stringResource(R.string.template_label)

                Column(Modifier.padding(horizontal = 24.dp)) {
                    AdjacentLabel(
                        text = description,
                        modifier = Modifier
                            .padding(top = 12.dp)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        value = templateName,
                        onValueChange = { templateName = it },
                        keyboardActions = KeyboardActions.Default,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                }

            }
            item {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    val description = stringResource(R.string.custom_command_template)
                    AdjacentLabel(
                        text = description,
                        modifier = Modifier
                    )
                    ProvideTextStyle(value = LocalTextStyle.current.merge(fontFamily = FontFamily.Monospace)) {
                        OutlinedTextField(
                            supportingText = { Text(text = stringResource(id = R.string.edit_template_desc)) },
                            modifier = Modifier.fillMaxWidth(),
                            value = templateText,
                            onValueChange = { templateText = it },
                            trailingIcon = {
                                if (templateText.isEmpty()) PasteFromClipBoardButton {
                                    templateText = it
                                }
                                else ClearButton { templateText = "" }
                            },
                            maxLines = 12,
                            minLines = 6,
                        )
                    }
                    LinkButton(modifier = Modifier.padding(vertical = 12.dp))
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .size(DividerDefaults.Thickness)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.shortcuts),
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    TextButtonWithIcon(
                        modifier = Modifier,
                        onClick = { isEditingShortcuts = true },
                        icon = Icons.Outlined.Edit,
                        text = stringResource(id = R.string.edit_shortcuts),
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            item {
                val shortcuts by DatabaseUtil.getShortcuts().collectAsState(emptyList())
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(500.dp),
//                        mainAxisSize = SizeMode.Expand,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        shortcuts.forEach { item ->
                            ShortcutChip(text = item.option, onClick = {
                                templateText = templateText.run {
                                    if (isEmpty()) item.option
                                    else this.removeSuffix(" ")
                                        .removeSuffix("\n") + "\n${item.option}"
                                }
                            })
                        }
                    }

                }
            }
        }
    }
    if (isEditingShortcuts) OptionChipsDialog { isEditingShortcuts = false }
}

