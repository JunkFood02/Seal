package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.SealTextField
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.isNumberInRange
import com.junkfood.seal.util.toDurationText
import com.junkfood.seal.util.toIntRange
import kotlin.math.roundToInt

private const val TAG = "VideoSectionSlider"

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
            Box(modifier = Modifier.size(20.dp)) {
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
            Box(modifier = Modifier.size(20.dp)) {
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

@Composable
fun VideoSelectionSlider(
    modifier: Modifier = Modifier,
    value: ClosedFloatingPointRange<Float>,
    duration: Int,
    onDurationClick: () -> Unit,
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
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
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
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onDurationClick,
                        onClickLabel = stringResource(id = R.string.edit)
                    )
            ) {
                Text(
                    text = "$startText / $endText",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp)
                )
            }
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
fun VideoClipDialog(
    onDismissRequest: () -> Unit,
    initialValue: ClosedFloatingPointRange<Float>,
    valueRange: ClosedFloatingPointRange<Float>,
    onConfirm: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    var fromMin by remember { mutableStateOf((initialValue.start.roundToInt() / 60).toString()) }
    var toMin by remember { mutableStateOf((initialValue.endInclusive.roundToInt() / 60).toString()) }
    var fromSec by remember { mutableStateOf((initialValue.start.roundToInt() % 60).toString()) }
    var toSec by remember { mutableStateOf((initialValue.endInclusive.roundToInt() % 60).toString()) }


    var error by remember(fromMin, toMin, fromSec, toSec) { mutableStateOf(false) }
    val valueIntRange = valueRange.toIntRange()

    val start = stringResource(id = R.string.clip_start)
    val end = stringResource(id = R.string.clip_end)
    val minute = "," + stringResource(id = R.string.minute)
    val second = "," + stringResource(id = R.string.second)


    fun onDone() {
        val startTime = convertToSecs(fromMin, fromSec)
        val endTime = convertToSecs(toMin, toSec)
        if (startTime != -1
            && endTime != -1
            && startTime < endTime
            && valueIntRange.contains(startTime)
            && valueIntRange.contains(endTime)
        ) {
            onConfirm((startTime.toFloat())..endTime.toFloat())
            onDismissRequest()
        } else error = true
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    SealDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.clip_video)) },
        icon = { Icon(Icons.Outlined.ContentCut, null) },
        confirmButton = { ConfirmButton { onDone() } },
        dismissButton = { DismissButton { onDismissRequest() } },
        text = {
            Column() {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SealTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = start + minute
                                    },
                                value = fromMin,
                                onValueChange = {
                                    if (it.isDigitsOnly()) fromMin = it
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                isError = error,
                            )
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp),
                                text = ":",
                                style = MaterialTheme.typography.labelLarge
                            )
                            SealTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = start + second
                                    },
                                value = fromSec,
                                onValueChange = {
                                    if (it.isNumberInRange(0, 60)) fromSec = it
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                isError = error,
                            )
                        }
                    }
                    Icon(
                        imageVector = if (!isRtl) Icons.Outlined.ArrowRight else Icons.Outlined.ArrowLeft,
                        contentDescription = null
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        SealTextField(
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = end + minute
                                },
                            value = toMin,
                            onValueChange = {
                                if (it.isDigitsOnly()) toMin = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            isError = error,
                        )
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 4.dp),
                            text = ":",
                            style = MaterialTheme.typography.labelLarge
                        )
                        SealTextField(
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = end + second
                                },
                            value = toSec,
                            onValueChange = {
                                if (it.isNumberInRange(0, 60)) toSec = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onDone() }),
                            singleLine = true,
                            isError = error,
                        )
                    }
                }
            }
        }
    )
}

@Composable
@Preview
fun VideoClipDialogPreview() {
    VideoClipDialog(
        onDismissRequest = {},
        initialValue = 0f..560f,
        valueRange = 0f..660f,
        onConfirm = {}
    )
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
            onValueChange = { value = it },
            onDurationClick = {})
    }
}

private fun convertToSecs(min: String, sec: String): Int {
    return if (sec.isNumberInRange(0, 60)) {
        if (min.isNumberInRange(0, Int.MAX_VALUE)) {
            min.toInt() * 60 + sec.toInt()
        } else -1
    } else -1
}

