package com.junkfood.seal.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.ui.common.LocalFixedColorRoles
import com.junkfood.seal.ui.theme.SealTheme

@Composable
fun SelectionGroupRow(
    modifier: Modifier = Modifier,
    content: @Composable SelectionGroupScope.() -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.selectableGroup().fillMaxWidth(),
    ) {
        val scope = remember { SelectionGroupScope(this) }
        content.invoke(scope)
    }
}

class SelectionGroupScope(rowScope: RowScope) : RowScope by rowScope

@Composable
fun SelectionGroupScope.SelectionGroupItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = SelectionGroupDefaults.shape(selected),
    colors: SelectionGroupItemColors = SelectionGroupDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = colors.containerColor(enabled, selected),
        contentColor = colors.contentColor(enabled, selected),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier =
                Modifier.heightIn(min = 40.dp)
                    .widthIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) { content.invoke(this) }
        }
    }
}

object SelectionGroupDefaults {

    @Composable
    fun shape(selected: Boolean): Shape {
        val animatedRoundedCorner by
            animateDpAsState(if (selected) 28.dp else 12.dp, label = "itemShape")
        return RoundedCornerShape(animatedRoundedCorner)
    }

    @Composable
    fun colors(
        activeContainerColor: Color = Color.Unspecified,
        activeContentColor: Color = Color.Unspecified,
        inactiveContainerColor: Color = Color.Unspecified,
        inactiveContentColor: Color = Color.Unspecified,
        disabledActiveContainerColor: Color = Color.Unspecified,
        disabledActiveContentColor: Color = Color.Unspecified,
        disabledInactiveContainerColor: Color = Color.Unspecified,
        disabledInactiveContentColor: Color = Color.Unspecified,
    ): SelectionGroupItemColors {
        return defaultSelectionGroupItemColors.run {
            copy(
                activeContainerColor =
                    activeContainerColor.takeOrElse { this.activeContainerColor },
                activeContentColor = activeContentColor.takeOrElse { this.activeContentColor },
                inactiveContainerColor =
                    inactiveContainerColor.takeOrElse { this.inactiveContainerColor },
                inactiveContentColor =
                    inactiveContentColor.takeOrElse { this.inactiveContentColor },
                disabledActiveContainerColor =
                    disabledActiveContainerColor.takeOrElse { this.disabledActiveContainerColor },
                disabledActiveContentColor =
                    disabledActiveContentColor.takeOrElse { this.disabledActiveContentColor },
                disabledInactiveContainerColor =
                    disabledInactiveContainerColor.takeOrElse {
                        this.disabledInactiveContainerColor
                    },
                disabledInactiveContentColor =
                    disabledInactiveContentColor.takeOrElse { this.disabledInactiveContentColor },
            )
        }
    }

    private val defaultSelectionGroupItemColors: SelectionGroupItemColors
        @Composable
        @ReadOnlyComposable
        get() {
            val colorScheme = MaterialTheme.colorScheme
            val fixedColorRoles = LocalFixedColorRoles.current
            return SelectionGroupItemColors(
                activeContainerColor = fixedColorRoles.primaryFixed,
                activeContentColor = fixedColorRoles.onPrimaryFixed,
                inactiveContainerColor = colorScheme.surfaceContainerHigh,
                inactiveContentColor = colorScheme.onSurface,
                disabledActiveContainerColor = colorScheme.onSurface.copy(alpha = 0.12f),
                disabledActiveContentColor = colorScheme.onSurface.copy(alpha = 0.38f),
                disabledInactiveContainerColor = colorScheme.onSurface.copy(alpha = 0.12f),
                disabledInactiveContentColor = colorScheme.onSurface.copy(alpha = 0.38f),
            )
        }
}

@Immutable
data class SelectionGroupItemColors(
    val activeContainerColor: Color,
    val activeContentColor: Color,
    val inactiveContainerColor: Color,
    val inactiveContentColor: Color,
    val disabledActiveContainerColor: Color,
    val disabledActiveContentColor: Color,
    val disabledInactiveContainerColor: Color,
    val disabledInactiveContentColor: Color,
) {

    @Stable
    internal fun contentColor(enabled: Boolean, checked: Boolean): Color {
        return when {
            enabled && checked -> activeContentColor
            enabled && !checked -> inactiveContentColor
            !enabled && checked -> disabledActiveContentColor
            else -> disabledInactiveContentColor
        }
    }

    @Stable
    internal fun containerColor(enabled: Boolean, active: Boolean): Color {
        return when {
            enabled && active -> activeContainerColor
            enabled && !active -> inactiveContainerColor
            !enabled && active -> disabledActiveContainerColor
            else -> disabledInactiveContainerColor
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SealTheme {
        Surface {
            var selected by remember { mutableIntStateOf(0) }
            val itemSet = setOf("All", "Downloaded", "Canceled", "Finished")
            SelectionGroupRow(
                modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState())
            ) {
                itemSet.forEachIndexed { index, s ->
                    SelectionGroupItem(
                        selected = selected == index,
                        onClick = { selected = index },
                    ) {
                        Text(s, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
