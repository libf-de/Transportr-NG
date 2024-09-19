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

package de.libf.transportrng.ui.trips.search

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Stop
import de.libf.ptek.dto.Style
import de.libf.ptek.dto.Trip
import de.libf.ptek.dto.min
import de.libf.transportrng.data.utils.formatDuration
import de.libf.transportrng.data.utils.getName
import de.libf.transportrng.data.utils.getStandardFare
import de.libf.transportrng.data.utils.hasProblem
import de.libf.transportrng.data.utils.largerThan
import de.libf.transportrng.data.utils.millisToMinutes
import de.libf.transportrng.ui.transport.composables.ProductComposable
import de.libf.transportrng.ui.transport.composables.WalkComposable
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_warning
import transportr_ng.composeapp.generated.resources.in_x_minutes
import transportr_ng.composeapp.generated.resources.now_small
import transportr_ng.composeapp.generated.resources.trip_not_travelable
import transportr_ng.composeapp.generated.resources.x_minutes_ago
import kotlin.time.Duration.Companion.milliseconds

val timeFmt = DateTimeComponents.Format {
    hour()
    chars(":")
    minute()
}

@Composable
fun FromRelativeTimeText(trip: Trip, max: Int = 99) {
    var value by remember { mutableStateOf("") }

    val nowSmall = stringResource(Res.string.now_small)
    val inMinutes = stringResource(Res.string.in_x_minutes)
    val agoMinutes = stringResource(Res.string.x_minutes_ago)
    val invalid = stringResource(Res.string.trip_not_travelable)

    LaunchedEffect(value) {
        if(trip.isTravelable) {
            val difference = Clock.System.now().minus(Instant.fromEpochMilliseconds(trip.firstDepartureTime)).inWholeMinutes ?: Long.MIN_VALUE
            value = when {
                difference !in -max..max -> ""
                difference == 0L -> nowSmall
                difference > 0 -> inMinutes.replace("%d", difference.toString())
                else -> agoMinutes.replace("%d", (difference * -1).toString())
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

fun Stop.getDepartureTimes(): Pair<String, String> {
    return this.getDepartureTime()?.let { departureTime ->
        var time = Instant.fromEpochMilliseconds(departureTime)

        if (isDepartureTimePredicted() && this.departureDelay != null) {
            val delay = this.departureDelay ?: 0L
            time = time.minus(delay.milliseconds)

            val delayMinutes = delay.milliseconds.inWholeMinutes
            val delayStr = when {
                delayMinutes > 0 -> "+$delayMinutes"
                delayMinutes < 0 -> "$delayMinutes"
                else -> ""
            }
            Pair(time.format(timeFmt), delayStr)
        } else {
            Pair(time.format(timeFmt), "")
        }
    } ?: Pair("", "")
}

fun Stop.getArrivalTimes(): Pair<String, String> {
    return this.getArrivalTime()?.let { arrivalTime ->
        var time = Instant.fromEpochMilliseconds(arrivalTime)

        if (isArrivalTimePredicted() && this.arrivalDelay != null) {
            val delay = this.arrivalDelay ?: 0L
            time = time.minus(delay.milliseconds)

            val delayMinutes = millisToMinutes(delay)
            val delayStr = when {
                delayMinutes > 0 -> "+$delayMinutes"
                delayMinutes < 0 -> "$delayMinutes"
                else -> ""
            }
            Pair(time.format(timeFmt), delayStr)
        } else {
            Pair(time.format(timeFmt), "")
        }
    } ?: Pair("", "")
}

fun getDepartureTimes(trip: Trip): Pair<String, String> {
    val firstLeg = trip.legs[0]
    return if (firstLeg is PublicLeg) {
        firstLeg.departureStop.getDepartureTimes()
    } else {
        Pair(Instant.fromEpochMilliseconds(firstLeg.departureTime).format(timeFmt), trip.firstPublicLeg.let {
            if (it?.departureDelay != null && it.departureDelay != 0L)
                it.departureStop.getDepartureTimes().second ?: ""
            else ""
        })
    }
}

fun getArrivalTimes(trip: Trip): Pair<String, String> {
    val lastLeg = trip.legs[trip.legs.size - 1]
    return if (lastLeg is PublicLeg) {
        lastLeg.arrivalStop.getArrivalTimes()
    } else {
        Pair(Instant.fromEpochMilliseconds(lastLeg.arrivalTime).format(timeFmt), trip.lastPublicLeg.let {
            if (it?.arrivalDelay != null && it.arrivalDelay != 0L)
                it.arrivalStop.getArrivalTimes().second
            else ""
        })
    }
}







@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripPreviewComposable(
    trip: Trip,
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
        val departureData = getDepartureTimes(trip)
        departureTime = departureData.first
        departureDelay = departureData.second
        departureName = trip.from.getName() ?: "???"

        duration = formatDuration(trip.duration) ?: ""
        price = trip.getStandardFare() ?: ""
        warning = trip.hasProblem()

        val arrivalData = getArrivalTimes(trip)
        arrivalTime = arrivalData.first
        arrivalDelay = arrivalData.second
        arrivalName = trip.to.getName() ?: "???"
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
                                painter = painterResource(Res.drawable.ic_warning),
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
                                if(it is PublicLeg) {
                                    ProductComposable(line = it.line)
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
                PublicLeg(
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
                        plannedArrivalTime = Clock.System.now().toEpochMilliseconds(),
                        predictedArrivalTime = null,
                        plannedDepartureTime = Clock.System.now().toEpochMilliseconds(),
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
                        plannedArrivalTime = Clock.System.now().toEpochMilliseconds(),
                        predictedArrivalTime = null,
                        plannedDepartureTime = Clock.System.now().toEpochMilliseconds(),
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


