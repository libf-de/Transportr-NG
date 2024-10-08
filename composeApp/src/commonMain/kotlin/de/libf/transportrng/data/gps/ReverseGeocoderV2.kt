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
import de.libf.transportrng.data.locations.WrapLocation

class ReverseGeocoderV2(
    private val geocoders: List<GeocodeProvider>
) {
    suspend fun findLocation(point: Point): Result<WrapLocation> {
        val failures: MutableList<Throwable> = mutableListOf()

        geocoders.forEach { geocoder ->
            geocoder.findLocation(point.lat, point.lon)
                .onSuccess { location ->
                    return Result.success(location)
                }
                .onFailure {
                    failures.add(it)
                }
        }

        return Result.failure(Exception("All geocoders failed: " + failures.map { it.message }.joinToString(" || ")))
    }

    suspend fun findLocation(lat: Double, lon: Double): Result<WrapLocation> {
        val failures: MutableList<Throwable> = mutableListOf()

        geocoders.forEach { geocoder ->
            geocoder.findLocation(lat, lon)
                .onSuccess { location ->
                    return Result.success(location)
                }
                .onFailure {
                    failures.add(it)
                }
        }

        return Result.failure(Exception("All geocoders failed: " + failures.map { it.message }.joinToString(" || ")))
    }
}