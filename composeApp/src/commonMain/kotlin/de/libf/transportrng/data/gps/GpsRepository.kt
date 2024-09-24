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

package de.libf.transportrng.data.gps

import de.libf.ptek.dto.Point
import de.libf.ptek.util.LocationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

interface GpsRepository {
    val isEnabled: Boolean
    fun getGpsStateFlow(): Flow<GpsState>
    fun setEnabled(enabled: Boolean)
}

sealed class GpsState {
    data object Denied : GpsState()
    data object Disabled : GpsState()
    data object EnabledSearching : GpsState()
    data class Enabled(val location: Point, val isAccurate: Boolean) : GpsState()
    data class Error(val message: String) : GpsState()
}

val GpsState.enabled: Boolean
    get() = this is GpsState.Enabled || this is GpsState.EnabledSearching

fun Flow<GpsState>.filterByDistance(minDistanceMeters: Float = 50f): Flow<GpsState> {
    var lastPoint: Point? = null
    var lastAccurate: Boolean = false

    return filter { newState ->
        if(newState is GpsState.Enabled) {
            val shouldInclude = lastPoint?.let { last ->
                LocationUtils.computeDistance(last, newState.location) >= minDistanceMeters
                        || (!lastAccurate && newState.isAccurate)
            } ?: true
            shouldInclude.also { if(it) {
                lastPoint = newState.location
                lastAccurate = newState.isAccurate
            } }
        } else true
    }
}

fun Flow<Point>.filterByDistance(minDistanceMeters: Double = 50.0): Flow<Point> {
    var lastPoint: Point? = null
    return filter { newPoint ->
        val shouldInclude = lastPoint?.let { last ->
            LocationUtils.computeDistance(last, newPoint) >= minDistanceMeters
        } ?: true
        shouldInclude.also {
            if(it) lastPoint = newPoint
        }
    }
}

//
//enum class GpsState {
//    DISABLED,
//    ENABLED,
//    DENIED
//}