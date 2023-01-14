package com.junkfood.seal.ui.component

import android.content.IntentSender.OnFinished
import android.text.TextUtils
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.theme.PreviewThemeLight
import com.junkfood.seal.ui.theme.harmonizeWithPrimary


@Composable
//@Preview
fun PlaylistPreview() {
    var selected by remember { mutableStateOf(false) }
    Column() {
        PreviewThemeLight {
            PlaylistItem(selected = selected) { selected = !selected }
        }
    }
}


@Composable
fun PlaylistItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    imageModel: Any = R.drawable.sample,
    title: String = "sample title ".repeat(5),
    author: String? = "author sample ".repeat(5),
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected) { onClick() },
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                modifier = Modifier
                    .padding(start = 4.dp, end = 12.dp)
                    .align(Alignment.CenterVertically), checked = selected, onCheckedChange = null
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .padding(end = 4.dp)
                    .weight(if (LocalWindowWidthState.current == WindowWidthSizeClass.Compact) 2f else 1f)
            ) {
                AsyncImageImpl(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(3f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                author?.let {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
        }
    }
}

@Composable
@Preview
fun TaskItemPreview() {
    PreviewThemeLight {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                CustomCommandTaskItem(status = TaskStatus.RUNNING)
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.FINISHED)
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.ERROR)
            }
            item {
                CustomCommandTaskItem(status = TaskStatus.CANCELED)
            }
        }
    }
}

enum class TaskStatus {
    FINISHED, CANCELED, RUNNING, ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCommandTaskItem(
    modifier: Modifier = Modifier,
    status: TaskStatus = TaskStatus.ERROR,
    progress: Float = 1f,
    url: String = "https://www.example.com",
    templateName: String = "Template Example",
    progressText: String = "[sample] Extracting URL: https://www.example.com\n" +
            "[sample] sample: Downloading webpage\n" +
            "[sample] sample: Downloading android player API JSON\n" +
            "[info] Available automatic captions for sample:" + "[info] Available automatic captions for sample:",
    onCopyLog: () -> Unit = {},
    onCopyError: () -> Unit = {},
    onRestart: () -> Unit = {},
    onShowLog: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val containerColor = MaterialTheme.colorScheme.run {
        when (status) {
            TaskStatus.FINISHED -> primaryContainer
            TaskStatus.CANCELED -> surfaceVariant
            TaskStatus.RUNNING -> tertiaryContainer
            TaskStatus.ERROR -> errorContainer
        }
    }
    val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
    val labelText = stringResource(
        id = when (status) {
            TaskStatus.FINISHED -> R.string.status_completed
            TaskStatus.CANCELED -> R.string.status_canceled
            TaskStatus.RUNNING -> R.string.status_downloading
            TaskStatus.ERROR -> R.string.status_error
        }
    )
    Surface(
        color = containerColor,
        shape = CardDefaults.shape,
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)

        ) {
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (status) {
                    TaskStatus.FINISHED -> {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            imageVector = Icons.Filled.CheckCircle,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = stringResource(id = R.string.status_completed)
                        )
                    }

                    TaskStatus.CANCELED -> {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            imageVector = Icons.Filled.Cancel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = stringResource(id = R.string.status_canceled)
                        )
                    }

                    TaskStatus.RUNNING -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
//                            progress = progress
                        )
                    }

                    TaskStatus.ERROR -> {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp),
                            imageVector = Icons.Filled.Error,
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = stringResource(id = R.string.status_error)
                        )
                    }
                }

                Column(
                    Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = templateName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary, maxLines = 1
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        color = contentColor
                    )
                }
                IconButton(
                    modifier = Modifier.semantics(mergeDescendants = true) { },
                    onClick = { onShowLog() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = stringResource(
                            id = R.string.show_logs
                        )
                    )
                }
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                color = if (status == TaskStatus.ERROR) MaterialTheme.colorScheme.error else contentColor,
                maxLines = 3,
                minLines = 3
            )

            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ButtonChip(
                    icon = Icons.Outlined.ContentCopy,
                    label = stringResource(id = R.string.copy_log)
                ) { onCopyLog() }
                if (status == TaskStatus.ERROR)
                    ButtonChip(
                        icon = Icons.Outlined.ErrorOutline,
                        label = stringResource(id = R.string.copy_error_report)
                    ) { onCopyError() }
                if (status == TaskStatus.RUNNING)
                    ButtonChip(
                        icon = Icons.Outlined.Cancel,
                        label = stringResource(id = R.string.cancel)
                    ) { onCancel() }
                if (status == TaskStatus.CANCELED)
                    ButtonChip(
                        icon = Icons.Outlined.RestartAlt,
                        label = stringResource(id = R.string.restart)
                    ) { onRestart() }
            }
        }
    }
}