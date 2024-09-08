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

package de.grobox.transportr.ui.trips.composables

import android.content.Context
import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KLeg
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.data.dto.KLine.Companion.DEFAULT_LINE_COLOR
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KStop
import de.grobox.transportr.ui.transport.composables.ProductComposable
import de.grobox.transportr.ui.trips.search.getArrivalTimes
import de.grobox.transportr.ui.trips.search.getDepartureTimes
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.TransportrUtils.getDrawableForProduct
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import java.util.Date


fun <T> List<T>.forEachWithNeighbors(action: (prev: T?, current: T, next: T?) -> Unit) {
    for ((index, item) in this.withIndex()) {
        val prev = if (index > 0) this[index - 1] else null
        val next = if (index < size - 1) this[index + 1] else null
        action(prev, item, next)
    }
}

@ColorInt
private fun KLine.getColorInt(): Int {
    if (this.style == null) return DEFAULT_LINE_COLOR
    if (this.style.backgroundColor != 0) return this.style.backgroundColor
    if (this.style.backgroundColor2 != 0) return this.style.backgroundColor2
    if (this.style.foregroundColor != 0) return this.style.foregroundColor
    return if (this.style.borderColor != 0) this.style.borderColor else DEFAULT_LINE_COLOR
}


@Composable
fun LegListComposable(
    legs: List<KLeg>,
    showLineNames: Boolean,
    modifier: Modifier = Modifier
) {
    var expandedLegs: List<KLeg> by remember { mutableStateOf(emptyList()) }

    LazyColumn(
        modifier = Modifier
    ) {
        legs.forEachWithNeighbors { prev, current, next ->
            if(current.isPublicLeg) {
                val lineName = if(showLineNames) null else getLocationName(current.destination)
                val collapsed = !expandedLegs.contains(current)
                item {
                    FirstLegComponent(
                        leg = current,
                        otherLegColor = prev?.getColor(),
                        dispTime = current.getDepartureTimes(LocalContext.current),
                        customLineName = lineName,
                        location = current.departure,
                        collapsed = collapsed,
                        stopCountClicked = {
                            expandedLegs = if(collapsed)
                                expandedLegs + current
                            else
                                expandedLegs - current
                        }
                    )
                }

                current.intermediateStops?.takeIf { expandedLegs.contains(current) } ?.let {
                    items(it) { stop ->
                        MiddleStopComponent(
                            stop = stop,
                            thisLegColor = current.getColor(),
                            type = LegType.MIDDLE
                        )
                    }
                }

                item {
                    LastLegComponent(
                        leg = current,
                        otherLegColor = next?.getColor(),
                        dispTime = current.getArrivalTimes(LocalContext.current),
                    )
                }
            } else {
                if(prev != null && next != null) {
                    item {
                        IntermediateComponent(
                            leg = current,
                            duration = formatDuration(prev.arrivalTime?.let(::Date), next.departureTime?.let(::Date))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstLegComponent(
    leg: KLeg,
    dispTime: Pair<String, String>,
    location: KLocation,
    customLineName: String? = null,
    stopCountClicked: () -> Unit,
    thisLegColor: Color = leg.getColor(),
    otherLegColor: Color? = null,
    collapsed: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textPad = if(otherLegColor == null) 0.dp else 4.dp

    Row(
        modifier = modifier.height(IntrinsicSize.Min).heightIn(min = 48.dp),
        verticalAlignment = if(otherLegColor == null) Alignment.Top else Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = if(otherLegColor == null) Arrangement.Top else Arrangement.Center,
            modifier = Modifier.width(50.dp).padding(top = textPad).fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.height(22.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dispTime.first,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyLarge.fontSize
                )
                if(dispTime.second.isNotBlank()) {
                    Text(
                        text = dispTime.second,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier.weight(1f))

            if(collapsed) {
                Text(
                    text = "Dauer:",
                    fontSize = 7.sp,
                    lineHeight = 7.sp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = formatDuration(leg.departureTime?.let(::Date), leg.arrivalTime?.let(::Date)) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier.weight(1f))
            }
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
                //.padding(top = if(otherLegColor == null) 4.dp else 0.dp)
        ) {
            if(otherLegColor == null) {
                drawFirstLeg(thisLegColor)
            } else {
                drawIntermediaryFirstLeg(otherLegColor, true, thisLegColor, false, dotPhase = 1f)
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = getLocationName(location) ?: "Start",
                style = MaterialTheme.typography.titleMedium,
                fontSize = MaterialTheme.typography.titleMedium.fontSize.times(1.1),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = textPad).height(22.dp).wrapContentHeight(align = Alignment.CenterVertically)
            )

            if(leg.isPublicLeg) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ProductComposable(
                        drawableId = getDrawableForProduct(leg.line?.product),
                        backgroundColor = leg.line?.style?.backgroundColor?.let { Color(it) },
                        foregroundColor = leg.line?.style?.foregroundColor?.let { Color(it) },
                        label = leg.line?.label,
                    )

                    val displayedLine = customLineName ?: (leg.line?.name.takeIf { !it.isNullOrEmpty() } ?: "")
                    Text(
                        text = displayedLine
                    )
                }

                leg.message.takeIf { !it.isNullOrEmpty() }?.let { msg ->
                    Text(
                        text = AnnotatedString.fromHtml(msg),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                leg.intermediateStops?.takeIf { it.isNotEmpty() }?.let { stops ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(48.dp)
                            .fillMaxWidth()
                            .clickable {
                                stopCountClicked()
                            }
                    ) {
                        Text(
                            text = pluralStringResource(R.plurals.stops, stops.size, stops.size)
                        )

                        Icon(
                            painter = if(collapsed) painterResource(R.drawable.ic_action_navigation_unfold_more)
                                        else painterResource(R.drawable.ic_action_navigation_unfold_less),
                            contentDescription = if(collapsed) stringResource(R.string.more) else "Display less",
                            modifier = Modifier.width(48.dp)
                        )
                    }
                }
            }
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_horiz),
                    contentDescription = stringResource(R.string.more)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiddleStopComponent(
    stop: KStop,
    type: LegType,
    thisLegColor: Color,
    modifier: Modifier = Modifier
) {
    if(type != LegType.MIDDLE) throw IllegalArgumentException("MiddleStopComponent can only be used for middle leg stop")

    val dispTime = stop.getDepartureTimes(LocalContext.current)

    Row(
        modifier = modifier.height(IntrinsicSize.Min).heightIn(min = 48.dp),
        verticalAlignment = if(type.isFirstStop()) Alignment.Top else Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(50.dp).fillMaxHeight()
        ) {
            Text(
                text = dispTime.first,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = MaterialTheme.typography.bodySmall.fontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if(dispTime.second.isNotBlank()) {
                Text(
                    text = dispTime.second,
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            drawMiddleLeg(thisLegColor)
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stop.location.name ?: stop.location.place ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_horiz),
                    contentDescription = stringResource(R.string.more)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastLegComponent(
    leg: KLeg,
    dispTime: Pair<String, String>,
    thisLegColor: Color = leg.getColor(),
    otherLegColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min).heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(50.dp).fillMaxHeight()
        ) {
            Text(
                text = dispTime.first
            )
            if(dispTime.second.isNotBlank()) {
                Text(
                    text = dispTime.second,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            if(otherLegColor == null)
                drawLastLeg(thisLegColor, !leg.isPublicLeg)
            else
                drawIntermediaryLastLeg(thisLegColor, false, otherLegColor ?: thisLegColor, true)
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = getLocationName(leg.arrival) ?: "Start",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if(leg.isPublicLeg) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                IconButton(
                    onClick = { /* TODO */ },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.more)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntermediateComponent(
    leg: KLeg,
    thisLegColor: Color = leg.getColor(),
    duration: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .heightIn(min = 48.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(50.dp).fillMaxHeight()
        ) { }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            drawIntermediaryLeg(thisLegColor)
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            val textPad = (MaterialTheme.typography.bodyMedium.fontSize.value / 2.5f).dp
            Text(
                text = "Umsteigen (${duration} min)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = textPad).height(64.dp).wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun KLeg.getColor(): Color {
    //TODO: Harmonize colors
    return if(isPublicLeg) Color(this.line?.getColorInt() ?: -0x1000000) else colorResource(R.color.walking)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegComponent(
    dispTime: Pair<String, String>,
    location: KLocation,
    line: KLine?,
    lineName: String? = null,
    type: LegType,
    thisLegColor: Color,
    otherLegColor: Color? = null,
    duration: String? = null,
    collapsed: Boolean = true,

    modifier: Modifier = Modifier
) {
    if(type == LegType.MIDDLE && collapsed) return

    Row(
        modifier = modifier.height(IntrinsicSize.Min).heightIn(min = 48.dp),
        verticalAlignment = if(type.isFirstStop()) Alignment.Top else Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = if(type == LegType.FIRST) Arrangement.Top else Arrangement.Center,
            modifier = Modifier.width(38.dp).fillMaxHeight()
        ) {

            if(type.isEndingStop()) {
                Text(
                    text = dispTime.first
                )
                Text(
                    text = dispTime.second,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else if(type == LegType.MIDDLE) {
                Text(
                    text = dispTime.first,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dispTime.second,
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }

            if(type.isFirstStop() && collapsed && duration != null) {
                Spacer(modifier.weight(1f))
                Text(
                    text = "Dauer:",
                    fontSize = 7.sp,
                    lineHeight = 7.sp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier.weight(1f))
            }
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
                .padding(top = if(type == LegType.FIRST) 4.dp else 0.dp)
        ) {
            when(type) {
                LegType.FIRST -> drawFirstLeg(thisLegColor)
                LegType.MIDDLE -> drawMiddleLeg(thisLegColor)
                LegType.INTERMEDIATE_LAST -> drawIntermediaryLastLeg(thisLegColor, false, otherLegColor ?: thisLegColor, true)
                LegType.INTERMEDIATE -> drawIntermediaryLeg(thisLegColor)
                LegType.INTERMEDIATE_FIRST -> drawIntermediaryFirstLeg(otherLegColor ?: thisLegColor, true, thisLegColor, false, dotPhase = 1f)
                LegType.LAST -> drawLastLeg(thisLegColor, false)
                LegType.LAST_WALK -> drawLastLeg(thisLegColor, true)
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            if(type.isFirstStop()) {
                val textPad = (MaterialTheme.typography.bodyMedium.fontSize.value / 2.5f).dp
                Text(
                    text = getLocationName(location) ?: "Start",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    modifier = Modifier.padding(top = textPad).weight(1f)
                )

                if(line != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        ProductComposable(
                            drawableId = getDrawableForProduct(line.product),
                            label = line.label,
                        )

                        val displayedLine = lineName ?: (line.name.takeIf { !it.isNullOrEmpty() } ?: "")
                        Text(
                            text = displayedLine
                        )
                    }
                }


                Text(
                    text = "This is an important message that can be several lines long and even include HTML.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .clickable {
                            /* TODO */
                        }
                ) {
                    Text(
                        text = "42 Stops"
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_action_navigation_unfold_more),
                        contentDescription = stringResource(R.string.more),
                        modifier = Modifier.width(48.dp)
                    )
                }
            }
            else if(type.isEndingStop()) {
                Text(
                    text = "Michelau (Oberfr)",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else if(type == LegType.INTERMEDIATE) {
                val textPad = (MaterialTheme.typography.bodyMedium.fontSize.value / 2.5f).dp
                Text(
                    text = "Umsteigen (5min)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = textPad).height(64.dp).wrapContentHeight(align = Alignment.CenterVertically)
                )
            } else {
                Text(
                    text = "Umsteigen (5min)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }

        if(type != LegType.LAST_WALK && type != LegType.INTERMEDIATE) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                IconButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .padding(end = if(type == LegType.MIDDLE) 12.dp else 0.dp)
                        .size(if(type == LegType.MIDDLE) 32.dp else 40.dp)

                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.more)
                    )
                }
            }
        }
    }
}


enum class LegType {
    FIRST,
    MIDDLE,
    INTERMEDIATE_LAST,
    INTERMEDIATE,
    INTERMEDIATE_FIRST,
    LAST,
    LAST_WALK
}

fun LegType.isEndingStop(): Boolean {
    return this != LegType.MIDDLE && this != LegType.INTERMEDIATE
}

fun LegType.isFirstStop(): Boolean {
    return this == LegType.FIRST || this == LegType.INTERMEDIATE_FIRST
}

fun LegType.isLastStop(): Boolean {
    return this == LegType.LAST || this == LegType.LAST_WALK || this == LegType.INTERMEDIATE_LAST
}

fun DrawScope.drawFirstLeg(color: Color) {
    val legBaseSizePx = 11.dp.toPx()

    drawCircle(
        color = color,
        radius = legBaseSizePx,
        center = Offset(size.width / 2, legBaseSizePx)
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2, legBaseSizePx),
        end = Offset(size.width / 2, size.height),
        strokeWidth = 4.5.dp.toPx()
    )
}

fun DrawScope.drawFirstLegBtmLine(color: Color) {
    val legBaseSizePx = 11.dp.toPx()

    drawLine(
        color = color,
        start = Offset(size.width / 2, legBaseSizePx),
        end = Offset(size.width / 2, size.height),
        strokeWidth = 4.5.dp.toPx()
    )
}

fun DrawScope.drawMiddleLeg(
    color: Color,
    legBaseSize: Dp = 9.dp,
    strokeSize: Dp = 4.5.dp,
) {
    val legBaseSizePx = legBaseSize.toPx()
    val legHeight = (size.height / 2 - legBaseSizePx)
    val strokeWidthPx = strokeSize.toPx()

    drawLine(
        color = color,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, legHeight * 1.1f),
        strokeWidth = strokeWidthPx
    )

    drawCircle(
        color = color,
        radius = legBaseSizePx - (strokeWidthPx/2),
        center = Offset(size.width / 2, size.height / 2),
        style = Stroke(
            width = strokeWidthPx
        )
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
        end = Offset(size.width / 2, size.height),
        strokeWidth = strokeWidthPx
    )
}

fun DrawScope.drawIntermediaryLastLeg(
    colorTop: Color,
    dotTop: Boolean,
    colorBottom: Color,
    dotBottom: Boolean,
    dotPhase: Float = 10f,
    legBaseSize: Dp = 11.dp,
    strokeSize: Dp = 4.5.dp,
) {
    val legBaseSizePx = legBaseSize.toPx()
    val strokeSizePx = strokeSize.toPx()
    val legHeight = (size.height / 2 - legBaseSizePx)
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dotPhase)

    drawLine(
        color = colorTop,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, legHeight * 1.1f),
        pathEffect = if(dotTop) dotEffect else null,
        strokeWidth = strokeSizePx
    )

    drawArc(
        color = colorTop,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset((size.width / 2) - legBaseSizePx + (strokeSizePx / 2), (size.height / 2)- legBaseSizePx + (strokeSizePx / 2 )),
        size = Size((legBaseSizePx * 2) - strokeSizePx, (legBaseSizePx * 2) - strokeSizePx),
        style = Stroke(width = strokeSizePx),
    )

    drawArc(
        color = colorBottom,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset((size.width / 2) - legBaseSizePx + (strokeSizePx / 2), (size.height / 2)- legBaseSizePx + (strokeSizePx / 2 )),
        size = Size((legBaseSizePx * 2) - strokeSizePx, (legBaseSizePx * 2) - strokeSizePx),
        style = Stroke(width = strokeSizePx),
    )

    drawLine(
        color = colorBottom,
        start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
        end = Offset(size.width / 2, size.height),
        pathEffect = if(dotBottom) dotEffect else null,
        strokeWidth = strokeSizePx
    )
}

fun DrawScope.drawIntermediaryLeg(
    color: Color,
    dotPhase: Float = 11f,
    strokeSize: Dp = 4.5.dp,
) {
    val strokeWidthPx = strokeSize.toPx()
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dotPhase)

    drawLine(
        color = color,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, size.height),
        pathEffect = dotEffect,
        strokeWidth = strokeWidthPx
    )
}

fun DrawScope.drawIntermediaryFirstLeg(
    colorTop: Color,
    dotTop: Boolean,
    colorBottom: Color,
    dotBottom: Boolean,
    dotPhase: Float = 10f,
    legBaseSize: Dp = 11.dp,
    strokeSize: Dp = 4.5.dp,
) {
    val legBaseSizePx = legBaseSize.toPx()
    val strokeSizePx = strokeSize.toPx()
    val circleY = 16.dp.toPx()
    val legHeight = (circleY - legBaseSizePx)
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dotPhase)


    drawLine(
        color = colorTop,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, legHeight * 1.1f),
        pathEffect = if(dotTop) dotEffect else null,
        strokeWidth = strokeSizePx
    )

    drawArc(
        color = colorTop,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset( (size.width / 2)- legBaseSizePx + (strokeSizePx / 2),  circleY- legBaseSizePx + (strokeSizePx / 2 )),
        size = Size((legBaseSizePx * 2) - strokeSizePx, (legBaseSizePx * 2) - strokeSizePx),
        style = Stroke(width = strokeSizePx),
    )

    drawArc(
        color = colorBottom,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset((size.width / 2) - legBaseSizePx + (strokeSizePx / 2), circleY - legBaseSizePx + (strokeSizePx / 2 )),
        size = Size((legBaseSizePx * 2) - strokeSizePx, (legBaseSizePx * 2) - strokeSizePx),
        style = Stroke(width = strokeSizePx),
    )

    drawLine(
        color = colorBottom,
        start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
        end = Offset(size.width / 2, size.height),
        pathEffect = if(dotBottom) dotEffect else null,
        strokeWidth = strokeSizePx
    )
}

fun DrawScope.drawLastLeg(
    color: Color,
    dot: Boolean = false,
    dotPhase: Float = 10f,
    legBaseSize: Dp = 11.dp,
    strokeSize: Dp = 4.5.dp,
) {
    val legBaseSizePx = legBaseSize.toPx()
    val strokeWidthPx = strokeSize.toPx()
    val legHeight = (size.height / 2 - legBaseSizePx)
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dotPhase)

    drawLine(
        color = color,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, legHeight * 1.1f),
        pathEffect = if(dot) dotEffect else null,
        strokeWidth = strokeWidthPx
    )

    drawCircle(
        color = color,
        radius = legBaseSizePx,
        center = Offset(size.width / 2, size.height / 2),

        )
}


fun KLeg.getDepartureTimes(context: Context): Pair<String, String> {
    return if(isPublicLeg)
        this.departureStop?.getDepartureTimes(context) ?: Pair("", "")
    else
        Pair(formatTime(context, departureTime?.let(::Date)), "")
}

fun KLeg.getArrivalTimes(context: Context): Pair<String, String> {
    return if(isPublicLeg)
        this.arrivalStop?.getArrivalTimes(context) ?: Pair("", "")
    else
        Pair(formatTime(context, arrivalTime?.let(::Date)), "")
}


