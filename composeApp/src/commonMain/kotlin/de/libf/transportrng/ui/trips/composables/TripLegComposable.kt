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

package de.libf.transportrng.ui.trips.composables

import androidx.annotation.ColorInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import de.libf.ptek.dto.IndividualLeg
import de.libf.transportrng.ui.transport.composables.ProductComposable
import de.libf.transportrng.ui.trips.search.getArrivalTimes
import de.libf.transportrng.ui.trips.search.getDepartureTimes
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.Line.Companion.DEFAULT_LINE_COLOR
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Position
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Stop
import de.libf.transportrng.data.utils.formatDuration
import de.libf.transportrng.data.utils.getDrawable
import de.libf.transportrng.data.utils.getName
import de.libf.transportrng.ui.favorites.composables.PopupMenuItem
import de.libf.transportrng.ui.trips.search.formatAsLocal
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_share
import transportr_ng.composeapp.generated.resources.ic_action_navigation_unfold_less
import transportr_ng.composeapp.generated.resources.ic_action_navigation_unfold_more
import transportr_ng.composeapp.generated.resources.ic_action_social_share
import transportr_ng.composeapp.generated.resources.ic_more_horiz
import transportr_ng.composeapp.generated.resources.more
import transportr_ng.composeapp.generated.resources.platform
import transportr_ng.composeapp.generated.resources.platform_short
import transportr_ng.composeapp.generated.resources.stops


fun <T> List<T>.forEachWithNeighbors(action: (prev: T?, current: T, next: T?) -> Unit) {
    for ((index, item) in this.withIndex()) {
        val prev = if (index > 0) this[index - 1] else null
        val next = if (index < size - 1) this[index + 1] else null
        action(prev, item, next)
    }
}

@ColorInt
private fun Line.getColorInt(): Int {
    if (this.style == null) return DEFAULT_LINE_COLOR
    if (this.style!!.backgroundColor != 0) return this.style!!.backgroundColor
    if (this.style!!.backgroundColor2 != 0) return this.style!!.backgroundColor2
    if (this.style!!.foregroundColor != 0) return this.style!!.foregroundColor
    return if (this.style!!.borderColor != 0) this.style!!.borderColor else DEFAULT_LINE_COLOR
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LegListComposable(
    legs: List<Leg>,
    showLineNames: Boolean,
    modifier: Modifier = Modifier,
    onLegClick: (Leg?, Location) -> Unit,
    onStopLongClick: (Stop) -> Unit
) {
    var expandedLegs: List<Leg> by remember { mutableStateOf(emptyList()) }

    LazyColumn(
        modifier = Modifier
    ) {
        legs.forEachWithNeighbors { prev, current, next ->
            if(current is PublicLeg) {
                val lineName = if(showLineNames) null else current.destination.getName()
                val collapsed = !expandedLegs.contains(current)
                item {
                    FirstLegComponent(
                        leg = current,
                        otherLegColor = prev?.getColor(),
                        dispTime = current.getDepartureTimes(),
                        customLineName = lineName,
                        location = current.departure,
                        collapsed = collapsed,
                        stopCountClicked = {
                            expandedLegs = if(collapsed)
                                expandedLegs + current
                            else
                                expandedLegs - current
                        },
                        onLegClick = onLegClick,
                        onStopLongClick = onStopLongClick,
                        modifier = Modifier
                    )
                }

                current.intermediateStops?.takeIf { expandedLegs.contains(current) } ?.let {
                    items(it) { stop ->
                        MiddleStopComponent(
                            stop = stop,
                            thisLegColor = current.getColor(),
                            type = LegType.MIDDLE,
                            onStopClick = { onLegClick(null, it.location) },
                            onStopLongClick = onStopLongClick,
                        )
                    }
                }

                item {
                    LastLegComponent(
                        leg = current,
                        otherLegColor = next?.getColor(),
                        dispTime = current.getArrivalTimes(),
                        onLegClick = onLegClick,
                        onStopLongClick = onStopLongClick,
//                        modifier = Modifier.combinedClickable(
//                            onClick = { onLegClick(current) },
//                            onLongClick = { onLegLongClick(current) }
//                        )
                    )
                }
            } else {
                if(prev != null && next != null) {
                    item {
                        IntermediateComponent(
                            leg = current,
                            duration = formatDuration(next.departureTime - prev.arrivalTime)
                        )
                    }
                } else if(prev == null) {
                    item {
                        FirstIndividualLegComponent(
                            leg = current,
                            otherLegColor = prev?.getColor(),
                            dispTime = current.getDepartureTimes(),
                            location = current.departure,
                            modifier = Modifier,
                            onLegClick = onLegClick,
                            onLongClick = {
                                onStopLongClick(Stop(it))
                            }
                        )
                    }
                } else if(next == null) {
                    item {
                        LastIndividualLegComponent(
                            leg = current,
                            otherLegColor = next?.getColor(),
                            dispTime = current.getArrivalTimes(),
                            modifier = Modifier,
                            onLegClick = onLegClick,
                            onLongClick = {
                                onStopLongClick(Stop(it))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FirstIndividualLegComponent(
    leg: Leg,
    dispTime: Pair<String, String>,
    location: Location,
    thisLegColor: Color = leg.getColor(),
    otherLegColor: Color? = null,
    modifier: Modifier = Modifier,
    onLegClick: (Leg?, Location) -> Unit,
    onLongClick: (Location) -> Unit,
) {
    val textPad = if(otherLegColor == null) 12.dp else 4.dp

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .heightIn(min = 48.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        verticalAlignment = if(otherLegColor == null) Alignment.Top else Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = if(otherLegColor == null) Arrangement.Top else Arrangement.Center,
            modifier = Modifier
                .width(50.dp)
                .padding(top = textPad)
                .fillMaxHeight()
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
            }

            Spacer(modifier.weight(1f))
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 12.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            drawFirstLeg(thisLegColor, dotted = true, dotPhase = 1f)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(Color.Transparent)
                .combinedClickable(
                    onClick = { onLegClick(null, leg.departure) },
                    onLongClick = {
                        onLongClick(leg.departure)
                    }
                )
        ) {
            Text(
                text = location.getName() ?: "Start",
                style = MaterialTheme.typography.titleMedium,
                fontSize = MaterialTheme.typography.titleMedium.fontSize.times(1.1),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = textPad).height(22.dp).wrapContentHeight(align = Alignment.CenterVertically)
            )
            Text(
                text = "zu Fuß",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.height(32.dp).wrapContentHeight(align = Alignment.Top)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FirstLegComponent(
    leg: Leg,
    dispTime: Pair<String, String>,
    location: Location,
    customLineName: String? = null,
    stopCountClicked: () -> Unit,
    thisLegColor: Color = leg.getColor(),
    otherLegColor: Color? = null,
    collapsed: Boolean = true,
    onLegClick: (Leg, Location) -> Unit,
    onStopLongClick: (Stop) -> Unit,
    modifier: Modifier = Modifier
) {
    val textPad = if(otherLegColor == null) 0.dp else 13.dp

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .heightIn(min = 48.dp),
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
                    lineHeight = MaterialTheme.typography.bodyLarge.fontSize,
                    maxLines = 1
                )
                DelayTextComposable(dispTime.second)
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
                    text = formatDuration(leg.arrivalTime - leg.departureTime) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
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
                drawIntermediaryFirstLeg(
                    colorTop = otherLegColor,
                    dotTop = true,
                    colorBottom = thisLegColor,
                    dotBottom = false,
                    dotPhase = 1f,
                    circleY = 24.dp
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f).combinedClickable(
                onLongClick = {
                    if(leg is PublicLeg)
                        onStopLongClick(leg.departureStop)
                },
                onClick = {
                    onLegClick(leg, leg.departure)
                }
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = textPad, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = location.getName() ?: "Start",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize.times(1.1),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .height(22.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .weight(1f)
                )

                if(leg is PublicLeg && leg.departurePosition != null)
                    StationDisplay(
                        position = leg.departurePosition
                    )
            }

            if(leg is PublicLeg) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ProductComposable(
                        drawableId = leg.line?.product.getDrawable(),
                        backgroundColor = leg.line?.style?.backgroundColor?.let { Color(it) },
                        foregroundColor = leg.line?.style?.foregroundColor?.let { Color(it) },
                        label = leg.line?.label,
                    )

                    val displayedLine = customLineName ?: (leg.line?.name.takeIf { !it.isNullOrEmpty() } ?: "")
                    Text(
                        text = displayedLine,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                leg.message.takeIf { !it.isNullOrEmpty() }?.let { msg ->
                    Text(
//                        text = AnnotatedString.fromHtml(msg),
                        text = msg,
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
                            text = pluralStringResource(Res.plurals.stops, stops.size, stops.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Icon(
                            painter = if(collapsed) painterResource(Res.drawable.ic_action_navigation_unfold_more)
                                        else painterResource(Res.drawable.ic_action_navigation_unfold_less),
                            contentDescription = if(collapsed) stringResource(Res.string.more) else "Display less",
                            modifier = Modifier.width(48.dp)
                        )
                    }
                }
            }
        }

//        Column {
//
//
//            Spacer(modifier.weight(1f))
//
//            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
//                IconButton(
//                    onClick = { /* TODO */ },
//                    modifier = Modifier
//                ) {
//                    Icon(
//                        painter = painterResource(Res.drawable.ic_more_horiz),
//                        contentDescription = stringResource(Res.string.more)
//                    )
//                }
//            }
//
//            Spacer(modifier.weight(1f))
//        }
    }
}

@Composable
fun StationDisplay(
    position: Position?,
    modifier: Modifier = Modifier
) {
    if(position == null) return

    Text(
        text = stringResource(Res.string.platform_short, listOfNotNull(position.name, position.section).joinToString(" ")),
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MiddleStopComponent(
    stop: Stop,
    type: LegType,
    thisLegColor: Color,
    onStopClick: (Stop) -> Unit,
    onStopLongClick: (Stop) -> Unit,
    modifier: Modifier = Modifier
) {
    if(type != LegType.MIDDLE) throw IllegalArgumentException("MiddleStopComponent can only be used for middle leg stop")

    val dispTime = stop.getDepartureTimes()

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
            DelayTextComposable(dispTime.second, true)
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            drawMiddleLeg(thisLegColor)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .combinedClickable(
                    onClick = { onStopClick(stop) },
                    onLongClick = { onStopLongClick(stop) }
                )
        ) {
            Text(
                text = stop.location.name ?: stop.location.place ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LastIndividualLegComponent(
    leg: Leg,
    dispTime: Pair<String, String>,
    thisLegColor: Color = leg.getColor(),
    otherLegColor: Color? = null,
    onLegClick: (Leg?, Location) -> Unit,
    onLongClick: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .heightIn(min = 48.dp)
            .background(if(leg is IndividualLeg) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 32.dp).width(50.dp).fillMaxHeight()
        ) {
            Text(
                text = dispTime.first,
                modifier = Modifier.padding(bottom = 12.dp).height(22.dp).wrapContentHeight(align = Alignment.CenterVertically)
            )
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 12.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            drawLastIndividualLeg(thisLegColor, true)
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxSize().combinedClickable(
                onClick = { onLegClick(null, leg.departure) },
                onLongClick = { onLongClick(leg.departure) }
            )
        ) {
            Text(
                text = "zu Fuß",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.height(32.dp).wrapContentHeight(align = Alignment.Bottom)
            )
            Text(
                text = leg.arrival.getName() ?: "Ziel",
                style = MaterialTheme.typography.titleMedium,
                fontSize = MaterialTheme.typography.titleMedium.fontSize.times(1.1),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp).height(22.dp).wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun DelayTextComposable(
    delay: String?,
    smaller: Boolean = false,
    modifier: Modifier = Modifier
) {
    if(!delay.isNullOrBlank()) {
        Text(
            text = delay,
            lineHeight = if(smaller) MaterialTheme.typography.bodySmall.fontSize else TextUnit.Unspecified,
            style = MaterialTheme.typography.bodySmall,
            fontSize = if(smaller) MaterialTheme.typography.labelSmall.fontSize else TextUnit.Unspecified,
            color = when {
                delay.startsWith("+") -> Color.Red
                delay.startsWith("-") -> Color.Blue
                else -> Color.Unspecified
            },
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LastLegComponent(
    leg: Leg,
    dispTime: Pair<String, String>,
    thisLegColor: Color = leg.getColor(),
    otherLegColor: Color? = null,
    onLegClick: (Leg, Location) -> Unit,
    onStopLongClick: (Stop) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .heightIn(min = 48.dp)
            .background(if(leg is IndividualLeg) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
        ,
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
            DelayTextComposable(dispTime.second)
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(20.dp)
                .fillMaxHeight()
        ) {
            if(otherLegColor == null)
                drawLastLeg(thisLegColor, leg !is PublicLeg)
            else
                drawIntermediaryLastLeg(thisLegColor, false, otherLegColor ?: thisLegColor, true)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .combinedClickable(
                    onClick = { onLegClick(leg, leg.arrival) },
                    onLongClick = { if(leg is PublicLeg) onStopLongClick(leg.arrivalStop) }
                )
        ) {
            Text(
                text = leg.arrival.getName() ?: "Start",
                style = MaterialTheme.typography.titleMedium,
                fontSize = MaterialTheme.typography.titleMedium.fontSize.times(1.1),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if(leg is PublicLeg && leg.departurePosition != null)
                StationDisplay(
                    position = leg.arrivalPosition,
                    modifier = Modifier.padding(end = 8.dp)
                )
        }



//        if(leg is PublicLeg) {
//            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
//                IconButton(
//                    onClick = { /* TODO */ },
//                ) {
//                    Icon(
//                        painter = painterResource(Res.drawable.ic_more_horiz),
//                        contentDescription = stringResource(Res.string.more)
//                    )
//                }
//            }
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntermediateComponent(
    leg: Leg,
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
private fun Leg.getColor(): Color {
    //TODO: Harmonize colors
    return if(this is PublicLeg) Color(this.line.getColorInt()) else Color.Yellow //colorResource(R.color.walking)
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

fun DrawScope.drawFirstLeg(
    color: Color,
    dotted: Boolean = false,
    dotPhase: Float = 0f
) {
    val legBaseSizePx = 11.dp.toPx()
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dotPhase)

    drawCircle(
        color = color,
        radius = legBaseSizePx,
        center = Offset(size.width / 2, legBaseSizePx)
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2, legBaseSizePx),
        end = Offset(size.width / 2, size.height),
        strokeWidth = 4.5.dp.toPx(),
        pathEffect = if(dotted) dotEffect else null
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
    circleY: Dp = 16.dp
) {
    val legBaseSizePx = legBaseSize.toPx()
    val strokeSizePx = strokeSize.toPx()
    val circleY = circleY.toPx()
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

fun DrawScope.drawLastIndividualLeg(
    color: Color,
    dot: Boolean = false,
    dotPhase: Float = 10f,
    legBaseSize: Dp = 11.dp,
    strokeSize: Dp = 4.5.dp,
) {
    val legBaseSizePx = legBaseSize.toPx()
    val strokeWidthPx = strokeSize.toPx()
    val legHeight = (size.height - legBaseSizePx)
    val dotEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dotPhase)


    drawCircle(
        color = color,
        radius = legBaseSizePx,
        center = Offset(size.width / 2, legHeight)
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, legHeight * 1.1f),
        strokeWidth = strokeWidthPx,
        pathEffect = if(dot) dotEffect else null
    )
}


fun Leg.getDepartureTimes(): Pair<String, String> {
    return if(this is PublicLeg)
        this.departureStop.getDepartureTimes()
    else
        Pair(Instant.fromEpochMilliseconds(departureTime).formatAsLocal(), "")
}

fun Leg.getArrivalTimes(): Pair<String, String> {
    return if(this is PublicLeg)
        this.arrivalStop.getArrivalTimes()
    else
        Pair(Instant.fromEpochMilliseconds(arrivalTime).formatAsLocal(), "")
}


