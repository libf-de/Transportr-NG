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

package de.libf.transportrng.ui.directions.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import de.libf.transportrng.data.utils.isNow
import de.libf.transportrng.data.utils.isToday
import de.libf.transportrng.data.utils.millisToMinutes
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_time
import transportr_ng.composeapp.generated.resources.in_x_minutes
import transportr_ng.composeapp.generated.resources.now_small
import transportr_ng.composeapp.generated.resources.trip_arr
import transportr_ng.composeapp.generated.resources.trip_dep
import transportr_ng.composeapp.generated.resources.x_minutes_ago

@Composable
fun DepartureTimeComposable(
    departure: Instant?,
    isDeparture: Boolean,
    modifier: Modifier = Modifier,
    maxMins: Int = 99
) {
    val dvNow = stringResource(Res.string.now_small)
    val dvInMinutes = stringResource(Res.string.in_x_minutes)
    val dvAgoMinutes = stringResource(Res.string.x_minutes_ago)
    val dvTimeFmt = DateTimeComponents.Format {
        hour()
        chars(":")
        minute()
    }
    // TODO: Localize
    val dvDateFmt = DateTimeComponents.Format {
        dayOfMonth()
        chars(".")
        monthNumber()
        chars(".")
        year()
    }

    var departureTimeStr by remember { mutableStateOf(dvNow) }

    var lastTime by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }

    fun formatTime(date: Instant?, withDate: Boolean): String {
        if (date == null) return ""

        val fTime = date.format(dvTimeFmt)
        val fDate = date.format(dvDateFmt)

        return if (withDate) "$fDate $fTime" else fTime
    }


    LaunchedEffect(lastTime) {
        departure?.let {
            when {
                it.isNow() -> {
                    departureTimeStr = dvNow
                }
                it.isToday() -> {
                    val difference = Clock.System.now().minus(it).inWholeMinutes
                    departureTimeStr = when {
                        difference !in -maxMins..maxMins -> formatTime(it, false)
                        difference == 0L -> dvNow
                        difference > 0 -> dvInMinutes.replace("%d", difference.toString())
                        else -> dvAgoMinutes.replace("%d", (difference * -1).toString())
                    }
                }
                else -> {
                    departureTimeStr = formatTime(it, true)
                }
            }
        }

        lastTime = Clock.System.now().toEpochMilliseconds()
        delay(1000 * 60)
    }


    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_time),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = if (isDeparture) stringResource(Res.string.trip_dep) else stringResource(Res.string.trip_arr),
            modifier = Modifier.padding(start = 6.dp, end = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = departureTimeStr,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}