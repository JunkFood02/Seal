package com.junkfood.seal.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
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
import androidx.compose.ui.unit.dp

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
        Spacer(Modifier.height(8.dp))
        ProvideTextStyle(LocalTextStyle.current.merge(MaterialTheme.typography.labelMedium)) {
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
