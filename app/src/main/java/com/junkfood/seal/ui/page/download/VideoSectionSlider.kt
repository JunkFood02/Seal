package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.toDurationText
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumbSize: DpSize = DpSize(12.dp, 12.dp)
) {
    RangeSlider(
        modifier = modifier,
        value = value,
        valueRange = valueRange,
        onValueChange = onValueChange,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = {
            Box(modifier= Modifier.size(20.dp)){
                SliderDefaults.Thumb(
                    modifier = Modifier.align(Alignment.Center),
                    interactionSource = startInteractionSource,
                    colors = colors,
                    enabled = enabled,
                    thumbSize = thumbSize
                )
            }
        },
        endThumb = {
            Box(modifier= Modifier.size(20.dp)){
                SliderDefaults.Thumb(
                    modifier = Modifier.align(Alignment.Center),
                    interactionSource = startInteractionSource,
                    colors = colors,
                    enabled = enabled,
                    thumbSize = thumbSize
                )
            }
        },
        track = { sliderPositions ->
            SliderDefaults.Track(
                colors = colors,
                enabled = enabled,
                sliderPositions = sliderPositions
            )
        },
        onValueChangeFinished = onValueChangeFinished
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSelectionSlider(
    modifier: Modifier = Modifier,
    value: ClosedFloatingPointRange<Float>,
    duration: Int,
    onDiscard: () -> Unit,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val startText by remember(value) {
        derivedStateOf {
            (value.start).roundToInt().toDurationText()
        }
    }
    val endText by remember(value) {
        derivedStateOf {
            (value.endInclusive).roundToInt().toDurationText()
        }
    }
    Column(modifier = modifier) {

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            CustomRangeSlider(
                modifier = Modifier.weight(1f),
                value = value,
                valueRange = 0f..duration.toFloat(),
                onValueChange = onValueChange
            )
        }

        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$startText / $endText",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButtonWithIcon(
                onClick = onDiscard,
                icon = Icons.Outlined.Delete,
                text = stringResource(id = R.string.discard),
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
@Preview
fun SliderPreview() {
    val time = 3700
    var value by remember {
        mutableStateOf(0f..time.toFloat())
    }
    Surface() {
        VideoSelectionSlider(
            value = value,
            duration = time,
            onDiscard = {},
            onValueChange = { value = it })
    }
}
