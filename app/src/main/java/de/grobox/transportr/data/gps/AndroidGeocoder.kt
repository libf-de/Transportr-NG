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

package de.grobox.transportr.data.gps

import android.content.Context
import android.location.Geocoder
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Point
import de.grobox.transportr.locations.WrapLocation
import java.io.IOException
import java.util.Locale

class AndroidGeocoder(private val context: Context) : GeocodeProvider {
    override suspend fun findLocation(lat: Double, lon: Double): Result<WrapLocation> {
        if(!Geocoder.isPresent()) return Result.failure(Exception("Android Geocoder not present"))

        try {
            val geoCoder = Geocoder(context, Locale.getDefault())
            val addresses = geoCoder.getFromLocation(lat, lon, 1)
            if (addresses == null || addresses.size == 0) throw IOException("No results")

            val address = addresses[0]

            var name: String? = address.thoroughfare ?: throw IOException("Empty Address")
            if (address.featureName != null) name += " " + address.featureName
            val place = address.locality

            val point = Point.fromDouble(lat, lon)
            return Result.success(
                WrapLocation(
                    Location(null, Location.Type.ADDRESS, point, place, name)
                )
            )
        } catch (e: IOException) {
            if (e.message != "Service not Available") {
                e.printStackTrace()
            }
            return Result.failure(e)
        }
    }
}