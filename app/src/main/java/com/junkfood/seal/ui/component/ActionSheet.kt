package com.junkfood.seal.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.common.LocalFixedColorRoles
import com.junkfood.seal.ui.theme.ErrorTonalPalettes
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.toLocalizedString

@Composable
fun ActionSheetPrimaryButton(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    outlineColor: Color = Color.Unspecified,
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .widthIn(min = 88.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = interactionSource,
                )
                .padding(8.dp),
    ) {
        Box(
            modifier =
                Modifier.width(80.dp)
                    .height(64.dp)
                    .clip(CircleShape)
                    .then(
                        if (outlineColor.isSpecified)
                            Modifier.border(
                                width = 1.dp,
                                outlineColor.takeOrElse { Color.Transparent },
                                shape = CircleShape,
                            )
                        else Modifier
                    )
                    .background(containerColor)
                    .indication(interactionSource, indication = LocalIndication.current)
        ) {
            Icon(
                imageVector,
                null,
                modifier = Modifier.size(24.dp).align(Alignment.Center),
                tint = contentColor,
            )
        }
        Spacer(Modifier.height(4.dp))
        ProvideTextStyle(LocalTextStyle.current.merge(MaterialTheme.typography.labelSmall)) {
            Text(text)
        }
    }
}

@Composable
fun ActionSheetItem(
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (ColumnScope.() -> Unit),
    trailingIcon: @Composable (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onClick == null) {
                        Modifier
                    } else if (onLongClick == null) {
                        Modifier.clickable(onClickLabel = onClickLabel, onClick = onClick)
                    } else {
                        Modifier.combinedClickable(
                            onClick = onClick,
                            onLongClick = onLongClick,
                            onClickLabel = onClickLabel,
                            onLongClickLabel = onLongClickLabel,
                        )
                    }
                )
                .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) Spacer(Modifier.width(20.dp))
        ProvideTextStyle(LocalTextStyle.current.merge(MaterialTheme.typography.titleSmall)) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                text.invoke(this)
            }
        }
        trailingIcon?.invoke()
    }
}

@Composable
fun ActionSheetInfo(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HorizontalDivider()
        ActionSheetItem(
            text = {
                Text(
                    1678886400000L.toLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text("03:02 · 1280x720 · 72.00MB", style = MaterialTheme.typography.bodySmall)
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Downloading, contentDescription = null)
            },
        )
        ActionSheetItem(
            text = {
                Text("Example", style = MaterialTheme.typography.titleSmall)
                Text("https://www.example.com", style = MaterialTheme.typography.bodySmall)
            },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Link, contentDescription = null) },
        ) {}
        ActionSheetItem(
            text = {
                Text("Video: vp9", style = MaterialTheme.typography.titleSmall)
                Text("745.7Kbps · 1280x720 · 69.00MB", style = MaterialTheme.typography.bodySmall)
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.VideoFile, contentDescription = null)
            },
        )
        ActionSheetItem(
            text = {
                Text("Audio: mp4a", style = MaterialTheme.typography.titleSmall)
                Text("72Kbps · 3.00MB", style = MaterialTheme.typography.bodySmall)
            },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.AudioFile, contentDescription = null)
            },
        )
    }
}

@Composable
fun Title() {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImageImpl(
            model = R.drawable.sample3,
            modifier =
                Modifier.height(64.dp).aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column() {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.video_title_sample_text),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    stringResource(R.string.video_creator_sample_text),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("59%", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SheetContent() {
    val fixedColors = LocalFixedColorRoles.current
    Column {
        Title()
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ActionSheetPrimaryButton(
                modifier = Modifier,
                containerColor = fixedColors.secondaryFixed,
                contentColor = fixedColors.onSecondaryFixedVariant,
                imageVector = Icons.Rounded.PlayArrow,
                text = stringResource(R.string.open_file),
            ) {}
            ActionSheetPrimaryButton(
                containerColor = fixedColors.tertiaryFixed,
                contentColor = fixedColors.onTertiaryFixedVariant,
                imageVector = Icons.Outlined.RestartAlt,
                text = stringResource(R.string.resume),
            ) {}
            ActionSheetPrimaryButton(
                containerColor = ErrorTonalPalettes.accent1(80.0),
                contentColor = ErrorTonalPalettes.accent1(10.0),
                imageVector = Icons.Outlined.ErrorOutline,
                text = stringResource(R.string.copy_error_report),
            ) {}

            ActionSheetPrimaryButton(
                modifier = Modifier,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                imageVector = Icons.Outlined.Delete,
                outlineColor = MaterialTheme.colorScheme.outlineVariant,
                text = stringResource(R.string.delete),
            ) {}
            ActionSheetPrimaryButton(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                imageVector = Icons.Outlined.Cancel,
                outlineColor = MaterialTheme.colorScheme.outlineVariant,
                text = stringResource(R.string.cancel),
            ) {}
        }

        ActionSheetInfo()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SheetPreview() {
    val sheetState =
        SheetState(
            density = LocalDensity.current,
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Expanded,
        )

    SealTheme {
        Surface() {
            SealModalBottomSheet(
                contentPadding = PaddingValues(),
                onDismissRequest = {},
                sheetState = sheetState,
            ) {
                SheetContent()
            }
        }
    }
}
