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

package de.grobox.transportr.ui.trips.search

import android.content.Context
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.schildbach.pte.dto.Leg
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Style
import de.schildbach.pte.dto.Trip
import de.grobox.transportr.ui.transport.composables.ProductComposable
import de.grobox.transportr.ui.transport.composables.WalkComposable
import de.grobox.transportr.ui.trips.detail.TripUtils.getStandardFare
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.DateUtils.getDifferenceInMinutes
import de.grobox.transportr.utils.DateUtils.millisToMinutes
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun FromRelativeTimeText(trip: Trip, max: Int = 99) {
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

fun Stop.getDepartureTimes(context: Context): Pair<String, String> {
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

fun Stop.getArrivalTimes(context: Context): Pair<String, String> {
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

fun getDepartureTimes(trip: Trip, context: Context): Pair<String, String> {
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

fun getArrivalTimes(trip: Trip, context: Context): Pair<String, String> {
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
fun TripPreviewComposable(
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
                        Text(
                            text = departureName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            trip.legs.forEach {
                                if(it.isPublicLeg) {
                                    ProductComposable(line = it.line!!)
                                } else if(it.min.largerThan(5)) {
                                    WalkComposable()
                                }
                            }
                        }

                        Column {
                            Text(
                                text = arrivalName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
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

private fun Long?.largerThan(i: Int): Boolean {
    return this?.let { it > i } ?: false
}

fun Trip.hasProblem(): Boolean {
    if (!isTravelable) return true
    for (leg in legs) {
        if (!leg.isPublicLeg) continue
        if (!leg.message.isNullOrEmpty()) return true
        if (!leg.line?.message.isNullOrEmpty()) return true
    }
    return false
}

@Composable
//@Preview
fun TripPreviewComposablePreview() {
    TripPreviewComposable(
        onClick = {},
        trip = Trip(
            id = "FOOBAR",
            from = Location("", Location.Type.STATION, null, "StPlace A", "Station A"),
            to = Location("", Location.Type.STATION, null, "StPlace B", "Station B"),
            legs = listOf(
                Leg(
                    line = Line(
                        "3_800755_28",
                        "DB Regio AG Bayern",
                        Product.REGIONAL_TRAIN,
                        "RE 4950",
                        "FooBar",
                        Style(
                            Style.Shape.RECT,
                            -7829368,
                            0,
                            -1,
                            0)
                    ),
                    destination = Location(
                        type = Location.Type.STATION,
                        name = "Coburg"
                    ),
                    departureStop = Stop(
                        location = Location(
                            "",
                            Location.Type.STATION,
                            null,
                            "StPlace B",
                            "Lichtenfels"
                        ),
                        plannedArrivalTime = Date().time,
                        predictedArrivalTime = null,
                        plannedDepartureTime = Date().time,
                        predictedDepartureTime = null
                    ),
                    arrivalStop = Stop(
                        location = Location(
                            "",
                            Location.Type.STATION,
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


