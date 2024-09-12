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

package de.grobox.transportr.ui.trips

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.formatDelay
import de.grobox.transportr.utils.DateUtils.formatTime
import de.libf.ptek.dto.Position
import de.libf.ptek.dto.Stop
import java.util.Date

abstract class BaseViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    protected val context: Context = v.context
    protected val fromTime: TextView = v.findViewById(R.id.fromTime)
    protected val toTime: TextView = v.findViewById(R.id.toTime)
    protected val fromDelay: TextView = v.findViewById(R.id.fromDelay)
    protected val toDelay: TextView = v.findViewById(R.id.toDelay)

    fun setArrivalTimes(timeView: TextView?, delayView: TextView, stop: Stop) {
        //if (stop.arrival == null) return

        val time = Date()

        if (stop.isArrivalTimePredicted() && stop.arrivalDelay != null) {
            val delay = stop.arrivalDelay!!
            time.time -= delay
            formatDelay(delayView.context, delay).let {
                delayView.apply {
                    text = it.delay
                    setTextColor(it.color)
                    visibility = VISIBLE
                }
            }
        } else {
            delayView.visibility = GONE
        }
        timeView?.let { it.text = formatTime(context, time) }
    }

    fun setDepartureTimes(timeView: TextView?, delayView: TextView, stop: Stop) {
//        if (stop.departureTime == null) return

        val time = Date()

        if (stop.isDepartureTimePredicted() && stop.departureDelay != null) {
            val delay = stop.departureDelay!!
            time.time -= delay
            formatDelay(delayView.context, delay).let {
                delayView.apply {
                    text = it.delay
                    setTextColor(it.color)
                    visibility = VISIBLE
                }
            }
        } else {
            delayView.visibility = GONE
        }
        timeView?.let { it.text = formatTime(context, time) }
    }

    @SuppressLint("SetTextI18n")
    protected fun TextView.addPlatform(position: Position?) {
        if (position == null) return
        text = "$text ${context.getString(R.string.platform, position.toString())}"
    }

}
