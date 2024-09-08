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

import android.text.format.DateFormat
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.isNow
import de.grobox.transportr.utils.DateUtils.isToday
import de.grobox.transportr.utils.DateUtils.millisToMinutes
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date

@Composable
fun DepartureTimeComposable(
    departure: Calendar?,
    isDeparture: Boolean,
    modifier: Modifier = Modifier,
    maxMins: Int = 99
) {
    val dvNow = stringResource(R.string.now_small)
    val dvInMinutes = stringResource(R.string.in_x_minutes)
    val dvAgoMinutes = stringResource(R.string.x_minutes_ago)
    val dvTimeFmt = DateFormat.getTimeFormat(LocalContext.current)
    val dvDateFmt = DateFormat.getDateFormat(LocalContext.current)

    var departureTimeStr by remember { mutableStateOf(dvNow) }

    var lastTime by remember { mutableStateOf(Date().time) }

    fun formatTime(date: Date?, withDate: Boolean): String {
        if (date == null) return ""

        val fTime = dvTimeFmt.format(date).let {
            if (dvTimeFmt.numberFormat.minimumIntegerDigits == 1
                && it.indexOf(':') == 1) {
                "0$it"
            } else it
        }

        val fDate = dvDateFmt.format(date)

        return if (withDate) "$fDate $fTime" else fTime
    }


    LaunchedEffect(lastTime) {
        departure?.let {
            when {
                isNow(it) -> {
                    departureTimeStr = dvNow
                }
                isToday(it) -> {
                    val difference = millisToMinutes(it.time.time - Date().time)
                    departureTimeStr = when {
                        difference !in -maxMins..maxMins -> formatTime(it.time, false)
                        difference == 0L -> dvNow
                        difference > 0 -> String.format(dvInMinutes, difference)
                        else -> String.format(dvAgoMinutes, difference * -1)
                    }
                }
                else -> {
                    departureTimeStr = formatTime(it.time, true)
                }
            }
        }

        lastTime = Date().time
        delay(1000 * 60)
    }


    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_time),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = if (isDeparture) stringResource(R.string.trip_dep) else stringResource(R.string.trip_arr),
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