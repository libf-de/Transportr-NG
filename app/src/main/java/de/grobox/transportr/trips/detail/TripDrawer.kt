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

package de.grobox.transportr.ui.trips.detail


import android.content.Context
import android.graphics.PorterDuff.Mode.MULTIPLY
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import de.grobox.transportr.R
import de.grobox.transportr.map.MapDrawer
import de.grobox.transportr.ui.map.getBackgroundColor
import de.grobox.transportr.ui.map.getForegroundColor
import de.grobox.transportr.utils.DateUtils.formatTime
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Stop
import de.libf.ptek.dto.Trip
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import java.util.Date

class TripDrawer(context: Context) : MapDrawer(context) {

    private enum class MarkerType {
        BEGIN, CHANGE, STOP, END, WALK
    }

    fun draw(map: MapLibreMap, trip: Trip, zoom: Boolean) {
        // draw leg path first, so it is always at the bottom
        var i = 1
        val builder = LatLngBounds.Builder()
        for (leg in trip.legs) {
            // get colors
            val backgroundColor = leg.getBackgroundColor(context)
            val foregroundColor = leg.getForegroundColor(context)

            // draw leg path first, so it is always at the bottom
            val points = ArrayList<LatLng>(leg.path?.size ?: 0)
            leg.path?.mapTo(points) { LatLng(it.lat, it.lon) }
            map.addPolyline(PolylineOptions()
                    .color(backgroundColor)
                    .addAll(points)
                    .width(5f)
            )

            // Only draw marker icons for public transport legs
            if (leg is PublicLeg) {
                // Draw intermediate stops below all others
                leg.intermediateStops?.let {
                    for (stop in it) {
                        val stopIcon = getMarkerIcon(MarkerType.STOP, backgroundColor, foregroundColor) ?: continue
                        val text = getStopText(stop)
                        marLocation(map, stop.location, stopIcon, text)
                    }
                }

                // Draw first station or change station
                if (i == 1 || i == 2 && trip.legs[0] !is PublicLeg) {
                    val icon = getMarkerIcon(MarkerType.BEGIN, backgroundColor, foregroundColor) ?: continue
                    marLocation(map, leg.departure, icon, getStationText(leg, MarkerType.BEGIN))
                } else {
                    val icon = getMarkerIcon(MarkerType.CHANGE, backgroundColor, foregroundColor) ?: continue
                    marLocation(map, leg.departure, icon, getStationText(trip.legs[i - 2], leg))
                }

                // Draw final station only at the end or if end is walking
                if (i == trip.legs.size || i == trip.legs.size - 1 && trip.legs[i] !is PublicLeg) {
                    val icon = getMarkerIcon(MarkerType.END, backgroundColor, foregroundColor) ?: continue
                    marLocation(map, leg.arrival, icon, getStationText(leg, MarkerType.END))
                }
            } else {
                // only draw an icon if walk is required in the middle of a trip
                if (i > 1 && i < trip.legs.size) {
                    val icon = getMarkerIcon(MarkerType.WALK, backgroundColor, foregroundColor) ?: continue
                    marLocation(map, leg.departure, icon, getStationText(trip.legs[i - 2], leg))
                }
            }
            i += 1
            builder.includes(points)
        }
        if (zoom) {
            zoomToBounds(map, builder, false)
        }
    }

    private fun marLocation(map: MapLibreMap, location: Location, icon: Icon, text: String) {
        marLocation(map, location, icon, location.uniqueShortName ?: "", text)
    }

    private fun getMarkerIcon(type: MarkerType, backgroundColor: Int, foregroundColor: Int): Icon? {
        // Get Drawable
        val drawable: Drawable
        if (type == MarkerType.STOP) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_trip_stop) ?: throw RuntimeException()
            drawable.mutate().setColorFilter(backgroundColor, SRC_IN)
        } else {
            val res: Int = when (type) {
                MarkerType.BEGIN -> R.drawable.ic_marker_trip_begin
                MarkerType.CHANGE -> R.drawable.ic_marker_trip_change
                MarkerType.END -> R.drawable.ic_marker_trip_end
                MarkerType.WALK -> R.drawable.ic_marker_trip_walk
                else -> throw IllegalArgumentException()
            }
            drawable = ContextCompat.getDrawable(context, res) as LayerDrawable
            drawable.getDrawable(0).mutate().setColorFilter(backgroundColor, MULTIPLY)
            drawable.getDrawable(1).mutate().setColorFilter(foregroundColor, SRC_IN)
        }
        return drawable.toIcon()
    }

    private fun getStopText(stop: Stop): String {
        var text = ""
        stop.getArrivalTime(false)?.let {
            text += "${context.getString(R.string.trip_arr)}: ${formatTime(context, Date(it))}"
        }
        stop.getDepartureTime(false)?.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${formatTime(context, Date(it))}"
        }
        return text
    }

    private fun getStationText(leg: Leg, type: MarkerType): String {
        return when (type) {
            MarkerType.BEGIN -> leg.departureTime.let {
                "${context.getString(R.string.trip_dep)}: ${formatTime(context, Date(it))}"
            }
            MarkerType.END -> leg.arrivalTime.let {
                "${context.getString(R.string.trip_arr)}: ${formatTime(context, Date(it))}"
            }
            else -> ""
        } ?: ""
    }

    private fun getStationText(leg1: Leg, leg2: Leg): String {
        var text = ""
        leg1.arrivalTime.let {
            text += "${context.getString(R.string.trip_arr)}: ${formatTime(context, Date(it))}"
        }
        leg2.departureTime.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${formatTime(context, Date(it))}"
        }
        return text
    }

}
