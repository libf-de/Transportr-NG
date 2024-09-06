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

package de.grobox.transportr.trips.search

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KLeg
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KStop
import de.grobox.transportr.data.dto.KStyle
import de.grobox.transportr.data.dto.KTrip
import de.grobox.transportr.trips.detail.TripUtils.getStandardFare
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.DateUtils.getDifferenceInMinutes
import de.grobox.transportr.utils.DateUtils.millisToMinutes
import de.grobox.transportr.utils.TransportrUtils
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale

@Composable
fun FromRelativeTimeText(trip: KTrip, max: Int = 99) {
    var value by remember { mutableStateOf("") }

    val nowSmall = stringResource(R.string.now_small)
    val inMinutes = stringResource(R.string.in_x_minutes)
    val agoMinutes = stringResource(R.string.x_minutes_ago)
    val invalid = stringResource(R.string.trip_not_travelable)

    LaunchedEffect(value) {
        if(trip.isTravelable) {
            val difference = getDifferenceInMinutes(trip.firstDepartureTime?.let(::Date)) ?: Long.MIN_VALUE
            value = when {
                difference !in -max..max -> ""
                difference == 0L -> nowSmall
                difference > 0 -> String.format(inMinutes, difference)
                else -> String.format(agoMinutes, difference * -1)
            }
        } else {
            value = invalid
        }

        delay(1000 * 60)
    }

    if(value.isNotBlank()) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

fun KStop.getDepartureTimes(context: Context): Pair<String, String> {
    return this.getDepartureTime()?.let { departureTime ->
        val time = Date(departureTime)

        if (isDepartureTimePredicted() && this.departureDelay != null) {
            val delay = this.departureDelay ?: 0L
            time.time -= delay

            val delayMinutes = millisToMinutes(delay)
            val delayStr = when {
                delayMinutes > 0 -> "+$delayMinutes"
                delayMinutes < 0 -> "$delayMinutes"
                else -> ""
            }
            Pair(formatTime(context, time), delayStr)
        } else {
            Pair(formatTime(context, time), "")
        }
    } ?: Pair("", "")
}

fun KStop.getArrivalTimes(context: Context): Pair<String, String> {
    return this.getArrivalTime()?.let { arrivalTime ->
        val time = Date(arrivalTime)

        if (isArrivalTimePredicted() && this.arrivalDelay != null) {
            val delay = this.arrivalDelay ?: 0L
            time.time -= delay

            val delayMinutes = millisToMinutes(delay)
            val delayStr = when {
                delayMinutes > 0 -> "+$delayMinutes"
                delayMinutes < 0 -> "$delayMinutes"
                else -> ""
            }
            Pair(formatTime(context, time), delayStr)
        } else {
            Pair(formatTime(context, time), "")
        }
    } ?: Pair("", "")
}

fun getDepartureTimes(trip: KTrip, context: Context): Pair<String, String> {
    val firstLeg = trip.legs[0]
    return if (firstLeg.isPublicLeg) {
        firstLeg.departureStop?.getDepartureTimes(context)
    } else {
        firstLeg.getDepartureTime()?.let { firstLegDepartureTime ->
            Pair(formatTime(context, Date(firstLegDepartureTime)), trip.firstPublicLeg.let {
                if (it?.departureDelay != null && it.departureDelay != 0L)
                    it.departureStop?.getDepartureTimes(context)?.second ?: ""
                else ""
            })
        }
    } ?: Pair("", "")
}

fun getArrivalTimes(trip: KTrip, context: Context): Pair<String, String> {
    val lastLeg = trip.legs[trip.legs.size - 1]
    return if (lastLeg.isPublicLeg) {
        lastLeg.arrivalStop?.getArrivalTimes(context)
    } else {
        lastLeg.getArrivalTime()?.let { lastLegArrivalTime ->
            Pair(formatTime(context, Date(lastLegArrivalTime)), trip.lastPublicLeg.let {
                if (it?.arrivalDelay != null && it.arrivalDelay != 0L)
                    it.arrivalStop?.getArrivalTimes(context)?.second ?: ""
                else ""
            })
        }
    } ?: Pair("", "")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripListItem(
    trip: KTrip,
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

    Card(
        onClick = { onClick() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp).height(IntrinsicSize.Min),
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                //fromTimeRel
                FromRelativeTimeText(trip = trip)

                Row(
                    modifier = Modifier.padding(top = 4.dp).height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        //fromTime
                        Text(text = departureTime)

                        Text(text = departureDelay)

                        Spacer(modifier = Modifier.weight(1f))

                        if(warning) {
                            Icon(
                                painter = painterResource(R.drawable.ic_warning),
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp).align(Alignment.CenterHorizontally)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = arrivalTime
                        )

                        if(arrivalDelay.isNotBlank())  Text(text = arrivalDelay)
                    }

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = departureName)

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            trip.legs.forEach {
                                if(it.isPublicLeg) {
                                    ProductViewComposable(
                                        line = it.line!!,
                                        drawableForProductGetter = TransportrUtils::getDrawableForProduct
                                    )
                                } else {
                                    WalkView()
                                }
                            }
                        }

                        Column {
                            Text(text = arrivalName)
                            if(arrivalDelay.isNotBlank()) Text(text = "")
                        }

                    }
                }

            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Spacer
                Text(text = duration, modifier = Modifier.padding(bottom = 4.dp))
                if(price.isNotBlank()) {
                    Text(text = price)
                }
            }
        }
    }
}

@Composable
fun WalkView() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp, 6.dp, 6.dp, 6.dp))
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(3.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_walk),
            contentDescription = "Walk",
            tint = MaterialTheme.colorScheme.onTertiary
        )
    }
}

@Composable
fun ProductViewComposable(
    line: KLine,
    drawableForProductGetter: (p: KProduct?) -> Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp, 6.dp, 6.dp, 6.dp))
            .background(line.style?.backgroundColor?.let { Color(it) } ?: Color.Gray)
            .padding(start = 2.dp, top = 3.dp, bottom = 3.dp, end = 6.dp)
    ) {
        Icon(
            painter = painterResource(drawableForProductGetter(line.product)),
            contentDescription = line.product.name.capitalize(Locale.getDefault()),
            tint = line.style?.foregroundColor?.let { Color(it) } ?: LocalContentColor.current,
            modifier = Modifier.padding(end = 4.dp)
        )

        Text(
            text = line.label ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = line.style?.foregroundColor?.let { Color(it) } ?: Color.Unspecified
        )
    }
}

private fun KTrip.hasProblem(): Boolean {
    if (!isTravelable) return true
    for (leg in legs) {
        if (!leg.isPublicLeg) continue
        if (!leg.message.isNullOrEmpty()) return true
        if (!leg.line?.message.isNullOrEmpty()) return true
    }
    return false
}

//Public(Line line, Location destination, Stop departureStop, Stop arrivalStop, List<Stop> intermediateStops, List<Point> path, String message) {
//Trip(String id, Location from, Location to, List<Leg> legs, List<Fare> fares, int[] capacity, Integer numChanges) {
//public Location(LocationType type, String id, String place, String name) {
//        this(type, id, (Point)null, place, name);
//    }

//id: String? = null,
//        from: KLocation,
//        to: KLocation,
//        legs: List<KLeg>,
//        fares: List<KFare>? = null,
//        capacity: List<Int>? = null,
//        changes: Int? = null
@Composable
@Preview
fun TripListItemPreview() {
    TripListItem(
        onClick = {},
        trip = KTrip(
            id = "FOOBAR",
            from = KLocation("", KLocation.Type.STATION, null, "StPlace A", "Station A"),
            to = KLocation("", KLocation.Type.STATION, null, "StPlace B", "Station B"),
            legs = listOf(
                KLeg(
                    line = KLine(
                        "3_800755_28",
                        "DB Regio AG Bayern",
                        KProduct.REGIONAL_TRAIN,
                        "RE 4950",
                        "FooBar",
                        KStyle(
                            KStyle.Shape.RECT,
                            -7829368,
                            0,
                            -1,
                            0)
                    ),
                    destination = KLocation(
                        type = KLocation.Type.STATION,
                        name = "Coburg"
                    ),
                    departureStop = KStop(
                        location = KLocation(
                            "",
                            KLocation.Type.STATION,
                            null,
                            "StPlace B",
                            "Lichtenfels"
                        ),
                        plannedArrivalTime = Date().time,
                        predictedArrivalTime = null,
                        plannedDepartureTime = Date().time,
                        predictedDepartureTime = null
                    ),
                    arrivalStop = KStop(
                        location = KLocation(
                            "",
                            KLocation.Type.STATION,
                            null,
                            "StPlace B",
                            "Lichtenfels"
                        ),
                        plannedArrivalTime = Date().time,
                        predictedArrivalTime = null,
                        plannedDepartureTime = Date().time,
                        predictedDepartureTime = null
                    ),
                    intermediateStops = emptyList(),
                    null,
                    "hi"
                )
            ),
            fares = emptyList(),
            capacity = listOf(1, 2),
            changes = 0
        ),
    )
}


