package de.libf.transportrng.ui.directions.composables

import androidx.annotation.FloatRange
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.libf.transportrng.data.utils.getDateLabel
import de.libf.transportrng.ui.datetimepicker.Calendar
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_trip_calendar
import transportr_ng.composeapp.generated.resources.cancel
import transportr_ng.composeapp.generated.resources.now
import transportr_ng.composeapp.generated.resources.ok
import transportr_ng.composeapp.generated.resources.time_picker_next_day
import transportr_ng.composeapp.generated.resources.time_picker_previous_day
import transportr_ng.composeapp.generated.resources.today
import transportr_ng.composeapp.generated.resources.tomorrow
import transportr_ng.composeapp.generated.resources.trip_arr
import transportr_ng.composeapp.generated.resources.trip_dep
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

sealed class DateTimePickerState {
    data class PickHour(val preselected: Int?) : DateTimePickerState()
    data class PickMinute(val preselected: Int?) : DateTimePickerState()
    data object PickDate : DateTimePickerState()
}



@Composable
fun DateTimePickerDialog(
    showPicker: MutableState<Boolean>,
    time: Pair<Int, Int>,
    date: Triple<Int, Int, Int>,
    isDeparture: Boolean,
    onDateTimeSelected: (LocalDateTime, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var sTime by remember { mutableStateOf(time) }
    var sDate by remember { mutableStateOf(date) }
    var sDep by remember { mutableStateOf(isDeparture) }

    var dateLbl by remember { mutableStateOf("") }

    LaunchedEffect(sDate) {
        scope.launch {
            dateLbl = LocalDate(
                year = sDate.third,
                monthNumber = sDate.second,
                dayOfMonth = sDate.first
            ).getDateLabel()
        }
    }

    var state by remember { mutableStateOf<DateTimePickerState>(DateTimePickerState.PickHour(sTime.first)) }

    if(showPicker.value) {
        AlertDialog(
            onDismissRequest = { showPicker.value = false },
//            title = {
//                Text(
//                    text = stringResource(Res.string.action_trip_calendar) + "?",
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            },
            text = {
                Crossfade(state) {
                    when(state) {
                        is DateTimePickerState.PickHour,
                        is DateTimePickerState.PickMinute -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TimeDisplay(
                                    hour = sTime.first,
                                    min = sTime.second,
                                    activeHour = state is DateTimePickerState.PickHour,
                                    onSelect = { state = if(it) DateTimePickerState.PickHour(sTime.first)
                                    else DateTimePickerState.PickMinute(sTime.second) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                )

                                Crossfade(
                                    targetState = state,
                                    label = "cross fade",
                                    modifier = Modifier.fillMaxWidth()
                                ) { screen ->
                                    when (screen) {
                                        is DateTimePickerState.PickHour -> HourWheel(
                                            selected = screen.preselected,
                                            onSelected = { value, confirm ->
                                                sTime = sTime.copy(first = value)
                                                if(confirm)
                                                    state = DateTimePickerState.PickMinute(sTime.second)
                                            },
                                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        )
                                        is DateTimePickerState.PickMinute -> MinuteWheel(
                                            selected = screen.preselected,
                                            onSelected = { value, _ ->
                                                sTime = sTime.copy(second = value)
                                            },
                                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                        )
                                        is DateTimePickerState.PickDate -> { }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { sDate = sDate.previousDay() }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = stringResource(Res.string.time_picker_previous_day)
                                        )
                                    }

                                    TextButton(
                                        onClick = { state = DateTimePickerState.PickDate },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(dateLbl)
                                    }

                                    IconButton(
                                        onClick = { sDate = sDate.nextDay() }
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = stringResource(Res.string.time_picker_next_day)
                                        )
                                    }
                                }

                                Row {
                                    Spacer(Modifier.weight(1f))

                                    LabelledRadio(
                                        selected = sDep,
                                        onSelect = { sDep = true },
                                        label = stringResource(Res.string.trip_dep)
                                    )

                                    Spacer(Modifier.weight(1f))

                                    LabelledRadio(
                                        selected = !sDep,
                                        onSelect = { sDep = false },
                                        label = stringResource(Res.string.trip_arr)
                                    )

                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        is DateTimePickerState.PickDate -> {
                            Calendar(
                                modifier = Modifier.fillMaxWidth(),
                                preselected = sDate
                            ) { date, _ ->
                                sDate = date
                            }
                        }


                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if(state !is DateTimePickerState.PickDate) {
                            showPicker.value = false
                            onDateTimeSelected(
                                LocalDateTime(
                                    LocalDate(
                                        year = sDate.third,
                                        monthNumber = sDate.second,
                                        dayOfMonth = sDate.first
                                    ),
                                    LocalTime(
                                        hour = sTime.first,
                                        minute = sTime.second
                                    )
                                ), sDep)
                        } else {
                            state = DateTimePickerState.PickHour(sTime.first)
                        }
                    }
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            if(state !is DateTimePickerState.PickDate)
                                showPicker.value = false
                            else
                                state = DateTimePickerState.PickHour(sTime.first)
                        }
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            if(state is DateTimePickerState.PickDate) {
                                sDate = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                                    .let { Triple(it.dayOfMonth, it.monthNumber, it.year) }
                            } else {
                                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
                                    sTime = Pair(it.time.hour, it.time.minute)
                                    sDate = Triple(it.date.dayOfMonth, it.date.monthNumber, it.date.year)
                                    sDep = true
                                    showPicker.value = false
                                    onDateTimeSelected(it, true)
                                }
                            }
                        }
                    ) {
                        Text(text = if(state is DateTimePickerState.PickDate)
                                        stringResource(Res.string.today)
                                    else
                                        stringResource(Res.string.now))
                    }
                }

            }
        )
    }
}

private fun Triple<Int, Int, Int>.previousDay(): Triple<Int, Int, Int> {
    return LocalDate(
        year = this.third,
        monthNumber = this.second,
        dayOfMonth = this.first
    ).minus(1, DateTimeUnit.DAY).let {
        Triple(it.dayOfMonth, it.monthNumber, it.year)
    }
}

private fun Triple<Int, Int, Int>.nextDay(): Triple<Int, Int, Int> {
    return LocalDate(
        year = this.third,
        monthNumber = this.second,
        dayOfMonth = this.first
    ).plus(1, DateTimeUnit.DAY).let {
        Triple(it.dayOfMonth, it.monthNumber, it.year)
    }
}

@Composable
fun TimeDisplay(
    hour: Int,
    min: Int,
    activeHour: Boolean,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = hour.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayLarge,
            color = if(activeHour) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = { onSelect(true) })
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = min.toString().padStart(2, '0'),
            color = if(!activeHour) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.clickable(onClick = { onSelect(false) })
        )
    }
}

/**
 * Used to export the relevant parameters to calculate the tapped hour for the tap gesture, as this
 * immediately confirms the value.
 * TODO: Is there a nicer way to retrieve the center position of the canvas from the pointerInput?
 */
private data class ExportedParams(
    val centerPos: Offset,
    val hourInnerRad: Float,
    val hourOuterRad: Float
)

@Composable
private fun HourWheel(
    selected: Int? = null,
    onSelected: (Int, Boolean) -> Unit = { _, _ -> },
    attrs: TimeWheelAttrs = TimeWheelAttrs.default(),
    modifier: Modifier = Modifier
) {
    var expParams: ExportedParams? by remember { mutableStateOf(null) }
    var touchPosition: Offset by remember { mutableStateOf(Offset.Zero) }

    var dragData: DragData? by remember { mutableStateOf(null) }

//    var dragSnap: Polar? by remember { mutableStateOf(null) }
//    var dragData: Pair<Int, Offset>? by remember { mutableStateOf(null) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(selected) {
        dragData = selected?.let {
            DragData(
                value = it,
                polar = Polar.Null,
                cartesian = Offset.Zero
            )
        }
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { touchPosition = it },
                    onDragEnd = { dragData?.value?.let { onSelected(it, true) } },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchPosition += dragAmount
                    }
                )
            }.pointerInput(Unit) {
                detectTapGestures { pos ->
                    touchPosition = pos

                    expParams?.let { p ->
                        pos.snapToPolar(
                            p.centerPos,
                            30f,
                            listOf(p.hourInnerRad, p.hourOuterRad)
                        ).let { polar ->
                            polar.theta.angleToHour(polar.radius == p.hourInnerRad)
                        }
                    }?.let { hourVal ->
                        onSelected(hourVal, true)
                    }
                }
            }
    ) {
        val circleRadius = attrs.circleRadius ?: (min(size.width, size.height) * 0.45f)
        val hoursOuterRadius = circleRadius * 0.8f
        val hoursInnerRadius = circleRadius * 0.5f
        val center = Offset(size.width / 2, size.height / 2).also {
            expParams = ExportedParams(it, hoursInnerRadius, hoursOuterRadius)
        }

        // Preselect time
        if(dragData.needsPopulating) {
            val hour = dragData?.value ?: 0
            val polar = hour.hourToPolar(hoursOuterRadius, hoursInnerRadius)
            dragData = DragData(
                polar = polar,
                cartesian = polar.toCartesian(center),
                value = hour
            )
        }

        // Update view when user dragged
        touchPosition.takeIf { it != Offset.Zero }?.let {
            val snapping = it.snapToPolar(
                center,
                30f,
                listOf(hoursInnerRadius, hoursOuterRadius)
            )

            if(snapping != dragData?.polar) {
                val dragHour = snapping.theta.angleToHour(snapping.radius == hoursInnerRadius)
                onSelected(dragHour, false)

                dragData = DragData(
                    polar = snapping,
                    cartesian = snapping.toCartesian(center),
                    value = dragHour
                )

                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }

        // Background circle
        drawCircle(
            color = attrs.backgroundColor,
            radius = circleRadius,
            center = center
        )

        // Highlighting indicator
        dragData?.let {
            drawCircle(
                color = attrs.highlightColor,
                radius = 5f,
                center = center
            )

            drawLine(
                color = attrs.highlightColor,
                start = center,
                end = it.cartesian,
                strokeWidth = 5f
            )

            drawCircle(
                color = attrs.highlightColor,
                radius = 40f,
                center = it.cartesian
            )
        }

        // Hours
        (0 until 12).forEach { hourStep ->
            val theta = hourStep.times(30f)
            val hourOuter = hourStep.plus(2).mod(12).plus(1)
            radialText(
                attrs.textMeasurer.measure(hourOuter.toString(), attrs.outerTextStyle),
                point = Polar(radius = hoursOuterRadius, theta = theta),
                color = if(hourOuter == dragData?.value) attrs.highlightTextColor else attrs.outerTextColor,
            )

            val hourInner = hourOuter.plus(12).mod(24)
            radialText(
                attrs.textMeasurer.measure(hourInner.toString(), attrs.innerTextStyle),
                point = Polar(radius = hoursInnerRadius, theta = theta),
                color = if(hourInner == dragData?.value) attrs.highlightTextColor else attrs.outerTextColor,
            )
        }
    }
}

@Composable
private fun MinuteWheel(
    selected: Int?,
    onSelected: (Int, Boolean) -> Unit = { _, _ -> },
    attrs: TimeWheelAttrs = TimeWheelAttrs.default(),
    modifier: Modifier = Modifier
) {
    var touchPosition by remember { mutableStateOf(Offset.Zero) }

    var centerPos: Offset? by remember { mutableStateOf(null) }

    var dragData: DragData? by remember { mutableStateOf(null) }

//    var dragSnap: Polar? by remember { mutableStateOf(null) }
//    var dragData: Pair<Int, Offset>? by remember { mutableStateOf(null) }
//    var dragHour: Int? by remember { mutableStateOf(null) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(selected) {
        dragData = selected?.let {
            DragData(polar = Polar.Null, cartesian = Offset.Zero, value = it)
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchPosition = offset
                    },
                    onDragEnd = {
                        dragData?.value?.let { onSelected(it, true) }
                    },
                    onDragCancel = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchPosition += dragAmount
                    }
                )

                detectTapGestures {
                    touchPosition = it

                    centerPos?.let { centerPos ->
                        it.snapToPolar(
                            centerPos,
                            6f,
                            listOf(10f)
                        ).polarToMinutes().let { mins ->
                            onSelected(mins, true)
                        }
                    }
                }
            }
    ) {
        val circleRadius = attrs.circleRadius ?: (min(size.width, size.height) * 0.45f)
        val center = Offset(size.width / 2, size.height / 2).also { centerPos = it }
        val minutesRadius = circleRadius * 0.8f

        val snapping = touchPosition.snapToPolar(center, 6f, listOf(minutesRadius))
        if(dragData.needsPopulating) {
            val minute = dragData?.value ?: 0
            val polar = minute.minuteToPolar(minutesRadius)

            dragData = DragData(
                polar = polar,
                cartesian = polar.toCartesian(center),
                value = minute
            )
        }

        if(snapping != dragData?.polar) {
            val dragMinutes = snapping.polarToMinutes()

            onSelected(dragMinutes, false)

            dragData = DragData(
                polar = snapping,
                cartesian = snapping.toCartesian(center),
                value = dragMinutes
            )

            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        drawCircle(
            color = attrs.backgroundColor,
            radius = circleRadius,
            center = center
        )

        (0 until 12).forEach { minuteSteps ->
            val theta = minuteSteps.times(30f)
            val hourOuter = minuteSteps.plus (3).mod(12).times(5)
            radialText(
                attrs.textMeasurer.measure(hourOuter.toString().padStart(2, '0'), attrs.outerTextStyle),
                point = Polar(radius = minutesRadius, theta = theta),
                color = if(hourOuter == dragData?.value) attrs.highlightTextColor else attrs.outerTextColor,
            )
        }

        dragData?.let {
            drawCircle(
                color = attrs.highlightColor,
                radius = 5f,
                center = center
            )

            drawLine(
                color = attrs.highlightColor,
                start = center,
                end = it.cartesian,
                strokeWidth = 5f
            )

            drawCircle(
                color = attrs.highlightColor,
                radius = 40f,
                center = it.cartesian
            )

            attrs.textMeasurer.measure(
                it.value.toString().padStart(2, '0'),
                attrs.outerTextStyle
            ).let { mText ->
                drawText(
                    textLayoutResult = mText,
                    topLeft = it.cartesian.let { Offset(it.x - (mText.size.width / 2f), it.y - (mText.size.height / 2f))},
                    color = attrs.highlightTextColor
                )
            }
        }
    }
}

private fun Float.toDegrees(): Float = this.times(180f / PI).toFloat()
private fun Float.toRadians(): Float = this.times(PI / 180f).toFloat()

/**
 * Holds polar coordinates as radius and theta in degrees [0-360)
 */
data class Polar(
    val radius: Float,
    @FloatRange(from = 0.0, to = 360.0) val theta: Float
) {
    companion object {
        val Null = Polar(-Float.MAX_VALUE, 0f)
    }
}

/**
 * Converts the specified angle to the equivalent hour value.
 * isInnerRing is used to select between AM/PM
 */
private fun Float.angleToHour(isInnerRing: Boolean): Int
    = this.div(30)
            .roundToInt()
            .plus(2)
            .mod(12)
            .plus(if(isInnerRing) 13 else 1)
            .mod(24)

private fun Polar.polarToMinutes(): Int
    = this.theta.div(6).roundToInt().plus(15).mod(60)

fun Polar.toCartesian(withCenter: Offset = Offset.Zero): Offset {
    val x = cos(theta.toRadians()).times(radius).plus(withCenter.x).toFloat()
    val y = sin(theta.toRadians()).times(radius).plus(withCenter.y).toFloat()
    return Offset(x, y)
}

private fun Int.minuteToPolar(radius: Float): Polar
    = Polar(radius = radius, theta = this.mod(60).times(6f))

private fun Int.hourToPolar(amRadius: Float, pmRadius: Float): Polar {
    val theta = this
        .minus(3)
        .mod(12)
        .times(30f)

    val rad = if(this > 12 || this == 0) pmRadius else amRadius

    return Polar(rad, theta)
}

/**
 * Snaps the tapped point "this" from the given center point "center" to the nearest "degreeMultiple"
 * on the closest radii from the "radii" list, as a Polar.
 */
fun Offset.snapToPolar(center: Offset, degreeMultiple: Float, radii: List<Float>): Polar  {
    val polar = this.toPolar(center)
    val snappedTheta = (polar.theta / degreeMultiple).roundToInt() * degreeMultiple
    val snappedRadius = radii.minBy { abs(polar.radius - it) }
    return Polar(snappedRadius, snappedTheta)
}

/**
 * Converts
 */
fun Offset.toPolar(withCenter: Offset): Polar {
    val dx = this.x - withCenter.x
    val dy = this.y - withCenter.y
    return Polar(
        radius = sqrt(dx * dx + dy * dy),
        theta = atan2(dy, dx).times(180 / PI).plus(360f).mod(360f).toFloat()
    )
}

/**
 * Holds the currently highlighted value and its corresponding coordinates in polar form
 * (for comparing to the snapped-tapped-value) and in cartesian form (for drawing)
 */
data class DragData(
    val polar: Polar,
    val cartesian: Offset,
    val value: Int
)

/**
 * When pre-filling the inputs, we can't compute the polar/cartesian values until we are
 * inside the Canvas() as we need the Canvas center point for that. This is indicated by
 * DragData being null or the polar having the special "Null" value (maximum negative radius)
 */
val DragData?.needsPopulating: Boolean
    get() = this == null || polar == Polar.Null

/**
 * Holder for styling attributes of the timing wheel, to not have many composable parameters
 */
data class TimeWheelAttrs(
    val outerTextStyle: TextStyle,
    val innerTextStyle: TextStyle,
    val textMeasurer: TextMeasurer,
    val circleRadius: Float?,
    val backgroundColor: Color,
    val highlightColor: Color,
    val outerTextColor: Color,
    val innerTextColor: Color,
    val highlightTextColor: Color
) {
    companion object {
        @Composable
        fun default() = TimeWheelAttrs(
            outerTextStyle = MaterialTheme.typography.bodyLarge,
            innerTextStyle = MaterialTheme.typography.bodyMedium,
            textMeasurer = rememberTextMeasurer(),
            circleRadius = null,
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            highlightColor = MaterialTheme.colorScheme.primary,
            outerTextColor = MaterialTheme.colorScheme.onSurface,
            innerTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            highlightTextColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * drawText, but with Polar coordinates
 */
fun DrawScope.radialText(
    text: TextLayoutResult,
    point: Polar,
    color: Color = Color.Unspecified
) {
    val topLeftOffset = point.toCartesian().let {
        Offset(
            x = it.x + (size.width / 2f) - (text.size.width / 2f),
            y = it.y + (size.height / 2f) - (text.size.height / 2f)
        )
    }
    drawText(
        textLayoutResult = text,
        topLeft = topLeftOffset,
        color = color
    )
}


@Composable
fun LabelledRadio(
    selected: Boolean,
    label: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onSelect
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )

        Text(text = label)
    }
}