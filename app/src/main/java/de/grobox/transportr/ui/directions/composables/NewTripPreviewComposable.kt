/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2024 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.ui.directions.composables

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.ui.transport.composables.getDrawableRes
import de.grobox.transportr.ui.trips.detail.TripUtils.getStandardFare
import de.grobox.transportr.ui.trips.search.getArrivalTimes
import de.grobox.transportr.ui.trips.search.getDepartureTimes
import de.grobox.transportr.ui.trips.search.hasProblem
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Leg
import de.schildbach.pte.dto.Trip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTripPreviewComposable(
    trip: Trip,
    context: Context = LocalContext.current,
    onClick: () -> Unit,
) {
    var departureTime by remember { mutableStateOf("") }
    var departureDelay by remember { mutableStateOf("") }
    var departureName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var warning by remember { mutableStateOf(false) }
    var arrivalTime by remember { mutableStateOf("") }
    var arrivalDelay by remember { mutableStateOf("") }
    var arrivalName by remember { mutableStateOf("") }

    LaunchedEffect(trip) {
        val departureData = getDepartureTimes(trip, context)
        departureTime = departureData.first
        departureDelay = departureData.second
        departureName = getLocationName(trip.from) ?: "???"

        duration = formatDuration(trip.duration) ?: ""
        price = trip.getStandardFare() ?: ""
        warning = trip.hasProblem()

        val arrivalData = getArrivalTimes(trip, context)
        arrivalTime = arrivalData.first
        arrivalDelay = arrivalData.second
        arrivalName = getLocationName(trip.to) ?: "???"
    }

    //val busIcon = painterResource(R.drawable.product_bus)
    val tripIcons = trip.legs.mapNotNull {
        it.line?.let { line ->
            line.product to painterResource(line.product.getDrawableRes())
        }
    }.plus(null to painterResource(R.drawable.ic_walk)).toMap()

    val textMeasurer = rememberTextMeasurer()
    val lblStyle = MaterialTheme.typography.labelSmall.copy(
        color = Color.Gray
    )

    OutlinedCard(
        modifier = Modifier.clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = departureTime,
                    fontWeight = FontWeight.Bold,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = departureDelay,
                    color = if(departureDelay.startsWith("+")) Color.Red else Color.Blue
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                ) {
                    val lineCenter = 8.dp.toPx()
                    val circleRad = 6.dp.toPx()
                    val halfCircleStroke = 2.dp.toPx()
                    val halfCircleSize = (circleRad * 2) - halfCircleStroke

                    val testSegSize = size.width / 10
                    var curPos = 0f

                    trip.legs.forEachIndexed { index, leg ->
                        val previous = trip.legs.getOrNull(index - 1)
                        val next = trip.legs.getOrNull(index + 1)

                        previous?.let { prev ->
                            if(prev.arrivalTime != null && leg.departureTime != null && prev.arrivalTime != leg.departureTime) {
                                val diffMins = (leg.departureTime!! - prev.arrivalTime!!).toMins()
                                val diffTimeFraction = trip.duration?.toMins()?.let { totalMin -> diffMins / totalMin.toFloat() } ?: .1f
                                val diffWidth = diffTimeFraction * size.width

                                curPos = centerSegment(
                                    width = diffWidth,
                                    color = Color.Transparent,
                                    start = curPos,
                                    walk = true,
                                    startColor = prev.line?.style?.backgroundColor?.let(::Color) ?: Color.Magenta,
                                    endColor = leg.line?.style?.backgroundColor?.let(::Color) ?: Color.Magenta,
                                    attrs = TripPreviewAttrs()
                                )
                            }
                        }



                        val legTimeFraction = trip.duration?.toMins()?.let { totalMin -> leg.min / totalMin.toFloat() } ?: .1f
                        val tripWidth = legTimeFraction * size.width
                        val legColor = leg.line?.style?.backgroundColor?.let(::Color) ?: Color.Yellow
                        curPos = aSegment(
                            start = curPos,
                            width = tripWidth,
                            color = legColor,
                            icon = tripIcons[leg.line?.product],
                            text = leg.getLineLabelOrNull()?.let {
                                textMeasurer.measure(
                                    AnnotatedString(it),
                                    style = lblStyle.copy(
                                        color = leg.line?.style?.foregroundColor?.let(::Color) ?: lblStyle.color
                                    )
                                )
                            },
                            whichSegment = when {
                                previous == null -> SegmentPos.FIRST
                                next == null -> SegmentPos.LAST
                                else -> SegmentPos.CENTER
                            },
                            startColor = previous?.line?.style?.backgroundColor?.let(::Color) ?: legColor,
                            endColor = next?.line?.style?.backgroundColor?.let(::Color) ?: legColor,
                            walk = !leg.isPublicLeg,
                            attrs = TripPreviewAttrs()
                        )
                    }
                }

            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = arrivalTime,
                    fontWeight = FontWeight.Bold,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = arrivalDelay,
                    color = if(arrivalDelay.startsWith("+")) Color.Red else Color.Blue
                )
            }
        }
    }
}

private fun Leg.getLineLabelOrNull(): String? {
    return if(this.isPublicLeg) this.line?.label
    else ""
}

private fun Long.toMins(): Long = (this / 1000 / 60)

data class TripPreviewAttrs(
    val lineY: Dp = 8.dp,
    val circleRad: Dp = 6.dp,
    val halfCircleStroke: Dp = 2.dp,
    val textY: Dp = 24.dp,
    val iconSize: Dp = 12.dp,
    val iconPadding: Dp = 2.dp,
    val textBoxPadding: PaddingValues = PaddingValues(horizontal = 2.dp)
)

fun DrawScope.lineLabel(
    icon: Painter?,
    segmentCenter: Float,
    segmentWidth: Float,
    backgroundColor: Color,
    text: TextLayoutResult,
    attrs: TripPreviewAttrs = TripPreviewAttrs(),
) {
    val yPos: Float = attrs.textY.toPx()
    var _iconSize: Float = if(icon != null) attrs.iconSize.toPx() else 0f
    val iconPadding: Float = attrs.iconPadding.toPx()

    val textFits = text.size.width <= segmentWidth

    // Draw rounded rectangle background for text
    val padding = mutableListOf(
        if(textFits) attrs.textBoxPadding.calculateLeftPadding(layoutDirection).toPx() else 0f,
        attrs.textBoxPadding.calculateTopPadding().toPx(),
        if(textFits) attrs.textBoxPadding.calculateRightPadding(layoutDirection).toPx() else 0f,
        attrs.textBoxPadding.calculateBottomPadding().toPx()
    )

    val fullWidth = (padding[0] + _iconSize + iconPadding + text.size.width + padding[2]).takeIf { it <= segmentWidth }
        ?: (padding[0] + text.size.width + padding[2]).also { _iconSize = 0f }
    val start = segmentCenter - (fullWidth / 2)

    val cornerRadius = 3.dp.toPx()
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(start, yPos - (text.size.height / 2) - padding[1]),
        size = Size(_iconSize + text.size.width + padding[0] + padding[2] + iconPadding, text.size.height + padding[1] + padding[3]),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Draw the image
    icon?.let {
        with(icon) {
            translate(left = start + padding[0], top = yPos - (_iconSize / 2)) {
                draw(
                    Size(_iconSize, _iconSize),
                    colorFilter = ColorFilter.tint(text.layoutInput.style.color)
                )
            }
        }
    }

    // Draw the text
    drawText(
        textLayoutResult = text,
        topLeft = Offset(
            start + padding[0] + _iconSize + iconPadding,
            yPos - text.size.height / 2
        )
    )
}

enum class SegmentPos {
    FIRST,
    CENTER,
    LAST
}

fun DrawScope.aSegment(
    width: Float,
    color: Color,
    whichSegment: SegmentPos,
    walk: Boolean = false,
    startColor: Color = color,
    endColor: Color = color,
    useStartColor: Boolean = walk,
    useEndColor: Boolean = walk,
    icon: Painter?,
    text: TextLayoutResult?,
    start: Float = 0f,
    attrs: TripPreviewAttrs
): Float {
    text?.let {
        lineLabel(
            icon = icon,
            segmentCenter = start + width / 2,
            segmentWidth = width,
            text = it,
            backgroundColor = color,
            attrs = attrs
        )
    }

    return when(whichSegment) {
        SegmentPos.CENTER -> centerSegment(
                width = width,
                color = color,
                start = start,
                walk = walk,
                startColor = startColor,
                endColor = endColor,
                useStartColor = useStartColor,
                useEndColor = useEndColor,
                attrs = attrs
            )
        SegmentPos.FIRST -> firstSegment(
                width = width,
                color = color,
                start = start,
                walk = walk,
                attrs = attrs,
                startColor = startColor,
                endColor = endColor,
                useStartColor = useStartColor,
                useEndColor = useEndColor,
            )

        SegmentPos.LAST -> lastSegment(
                width = width,
                color = color,
                start = start,
                walk = walk,
                attrs = attrs
        )
    }


}

fun DrawScope.firstSegment(
    width: Float,
    color: Color,
    start: Float = 0f,
    walk: Boolean = false,
    startColor: Color = color,
    endColor: Color = color,
    useStartColor: Boolean = !walk,
    useEndColor: Boolean = !walk,
    attrs: TripPreviewAttrs = TripPreviewAttrs()
): Float {
    val lineCenter: Float = attrs.lineY.toPx()
    val circleRad: Float = attrs.circleRad.toPx()
    val halfCircleStroke: Float = attrs.halfCircleStroke.toPx()

    val halfCircleSize = (circleRad * 2) - halfCircleStroke
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    val end = start + width

    drawCircle(
        color = if(useStartColor) startColor else color,
        radius = circleRad,
        center = Offset(start + circleRad, lineCenter)
    )

    drawLine(
        color = color,
        start = Offset(start, lineCenter),
        end = Offset(end - halfCircleSize / 2, lineCenter),
        pathEffect = if(walk) dotEffect else null,
        strokeWidth = 2.dp.toPx()
    )

    drawArc(
        color = if(useEndColor) endColor else color,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(end - halfCircleSize / 2, lineCenter - (circleRad / 2) - halfCircleStroke),
        size = Size(halfCircleSize, halfCircleSize),
        style = Stroke(width = halfCircleStroke),
    )

    return start + width
}

fun DrawScope.centerSegment(
    width: Float,
    color: Color,
    start: Float = 0f,
    walk: Boolean = false,
    startColor: Color = color,
    endColor: Color = color,
    useStartColor: Boolean = walk,
    useEndColor: Boolean = walk,
    attrs: TripPreviewAttrs = TripPreviewAttrs()
): Float {
    val lineCenter: Float = attrs.lineY.toPx()
    val circleRad: Float = attrs.circleRad.toPx()
    val halfCircleStroke: Float = attrs.halfCircleStroke.toPx()
    val halfCircleSize = (circleRad * 2) - halfCircleStroke
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)

    val end = start + width

    drawLine(
        color = color,
        start = Offset(start + halfCircleSize / 2, lineCenter),
        end = Offset(end - (halfCircleSize / 2), lineCenter),
        pathEffect = if(walk) dotEffect else null,
        strokeWidth = 2.dp.toPx()
    )

    drawArc(
        color = if(useStartColor) startColor else color,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(start - halfCircleSize / 2, lineCenter - (circleRad / 2) - halfCircleStroke),
        size = Size(halfCircleSize, halfCircleSize),
        style = Stroke(width = halfCircleStroke),
    )

    drawArc(
        color = if(useEndColor) endColor else color,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(end - halfCircleSize / 2, lineCenter - (circleRad / 2) - halfCircleStroke),
        size = Size(halfCircleSize, halfCircleSize),
        style = Stroke(width = halfCircleStroke),
    )

    return start + width
}

fun DrawScope.lastSegment(
    width: Float,
    color: Color,
    start: Float = 0f,
    walk: Boolean = false,
    attrs: TripPreviewAttrs = TripPreviewAttrs()
): Float {
    val lineCenter: Float = attrs.lineY.toPx()
    val circleRad: Float = attrs.circleRad.toPx()
    val halfCircleStroke: Float = attrs.halfCircleStroke.toPx()

    val halfCircleSize = (circleRad * 2) - halfCircleStroke
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    val end = start + width

    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(start - halfCircleSize / 2, lineCenter - (circleRad / 2) - halfCircleStroke),
        size = Size(halfCircleSize, halfCircleSize),
        style = Stroke(width = halfCircleStroke),
    )


    drawLine(
        color = color,
        start = Offset(start + halfCircleSize / 2, lineCenter),
        end = Offset(end - halfCircleSize / 2, lineCenter),
        pathEffect = if(walk) dotEffect else null,
        strokeWidth = 2.dp.toPx()
    )

    drawCircle(
        color = color,
        radius = circleRad,
        center = Offset(end - circleRad, lineCenter)
    )



    return start + width
}