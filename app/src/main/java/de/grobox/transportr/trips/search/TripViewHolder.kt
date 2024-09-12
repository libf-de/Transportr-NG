/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.google.common.base.Strings.isNullOrEmpty
import de.grobox.transportr.R
import de.grobox.transportr.ui.LineView
import de.grobox.transportr.ui.trips.BaseViewHolder
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatRelativeTime
import de.grobox.transportr.utils.DateUtils.formatTime
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import java.util.Date

class TripViewHolder(private val v: View) : BaseViewHolder(v) {

    private val fromTimeRel: TextView = v.findViewById(R.id.fromTimeRel)
    private val fromLocation: TextView = v.findViewById(R.id.fromLocation)
    private val toLocation: TextView = v.findViewById(R.id.toLocation)
    private val warning: View = v.findViewById(R.id.warning)
    private val lines: ViewGroup = v.findViewById(R.id.lines)
    private val duration: TextView = v.findViewById(R.id.duration)
    private val price: TextView = v.findViewById(R.id.price)

    fun bind(trip: Trip) {
        if (trip.isTravelable) {
            formatRelativeTime(fromTimeRel.context, Date(trip.firstDepartureTime!!)).let {
                fromTimeRel.apply {
                    text = it.relativeTime
                    visibility = it.visibility
                }
            }
        } else {
            fromTimeRel.setText(R.string.trip_not_travelable)
            fromTimeRel.visibility = VISIBLE
        }

        // Departure Time and Delay
        val firstLeg = trip.legs[0]
        if (firstLeg is PublicLeg) {
            setDepartureTimes(fromTime, fromDelay, firstLeg.departureStop)
        } else {
            fromTime.text = formatTime(context, Date(firstLeg.departureTime))
            val firstPublicLeg = trip.firstPublicLeg
            if (firstPublicLeg?.departureDelay != null && firstPublicLeg.departureDelay != 0L) {
                setDepartureTimes(null, toDelay, firstPublicLeg.departureStop)
            }
        }
        //fromLocation.text = getLocationName(trip.from)

        // Lines
        lines.removeAllViews()
        for (leg in trip.legs) {
            val lineView = LayoutInflater.from(context).inflate(R.layout.list_item_line, lines, false) as LineView
//            when (leg) {
//                is Public -> lineView.setLine(leg.line)
//                is Individual -> lineView.setWalk()
//                else -> throw RuntimeException()
//            }
            lines.addView(lineView)
        }

        // Warning and Duration
        warning.visibility = if (trip.hasProblem()) VISIBLE else GONE
        duration.text = formatDuration(trip.duration)
        //price.visibility = if (trip.hasFare()) VISIBLE else GONE
        //price.text = trip.getStandardFare()

        // Arrival Time and Delay
        val lastLeg = trip.legs[trip.legs.size - 1]
        if (lastLeg is PublicLeg) {
            setArrivalTimes(toTime, toDelay, lastLeg.arrivalStop)
        } else {
            toTime.text = formatTime(context, Date(lastLeg.arrivalTime))
            val lastPublicLeg = trip.lastPublicLeg
            if (lastPublicLeg?.arrivalDelay != null && lastPublicLeg.arrivalDelay != 0L) {
                setArrivalTimes(null, toDelay, lastPublicLeg.arrivalStop)
            }
        }
//        toLocation.text = getLocationName(trip.to)

        // Click Listener
//        v.setOnClickListener { listener.onClick(trip) }
    }

    private fun Trip.hasProblem(): Boolean {
        if (!isTravelable) return true
        for (leg in legs) {
            if (leg !is PublicLeg) continue
            if (!isNullOrEmpty(leg.message)) return true
            if (!isNullOrEmpty(leg.line.message)) return true
        }
        return false
    }

}
