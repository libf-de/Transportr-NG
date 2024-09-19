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
package de.libf.transportrng.data.locations

import androidx.room.Ignore
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
import de.libf.ptek.dto.Product
import de.libf.transportrng.data.maplibrecompat.LatLng
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_action_about
import transportr_ng.composeapp.generated.resources.ic_gps
import transportr_ng.composeapp.generated.resources.ic_location
import transportr_ng.composeapp.generated.resources.ic_location_address
import transportr_ng.composeapp.generated.resources.ic_location_station

@Serializable
open class WrapLocation(
    open val type: Location.Type,
    open val id: String? = null,
    open val lat: Int,
    open val lon: Int,
    open val place: String? = null,
    open val name: String? = null,
    open val products: Set<Product>? = null,
    @Ignore val wrapType: WrapType = WrapType.NORMAL
) {
    enum class WrapType {
        NORMAL, GPS
    }

    constructor(l: Location) : this(
        l.type!!,
        l.id,
        if (l.hasCoords) l.latAs1E6 else 0,
        if (l.hasCoords) l.lonAs1E6 else 0,
        l.place,
        l.name,
        l.products
    )

    constructor(wrapType: WrapType) : this(
        type = Location.Type.ANY,
        wrapType = wrapType,
        lat = 0,
        lon = 0
    ) {
        require(wrapType != WrapType.NORMAL) { "Type can't be normal" }
    }

    constructor(latLng: LatLng) : this(
        type = Location.Type.COORD,
        lat = (latLng.latitude * 1E6).toInt(),
        lon = (latLng.longitude * 1E6).toInt()
    ) {
        require(lat != 0 || lon != 0) { "Null Island is not a valid location" }
    }

    val location: Location
        get() {
            val point = Point.from1E6(lat, lon)
            if (type == Location.Type.ANY && id != null) {
                return Location(
                    id = null,
                    type = type,
                    coord = point,
                    place = place,
                    name = name,
                    products = products
                )
            }
            return Location(
                id = id,
                type = type,
                coord = point,
                place = place,
                name = name,
                products = products
            )
        }

    fun hasId(): Boolean {
        return !id.isNullOrEmpty()
    }

    fun hasLocation(): Boolean {
        return lat != 0 || lon != 0
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) {
            return true
        }
        if (o is WrapLocation) {
            return location == o.location
        }
        return false
    }

    override fun hashCode(): Int {
        return this.location.hashCode()
    }

    open val drawableInt: DrawableResource
        get() {
            return when (wrapType) {
                WrapType.GPS -> Res.drawable.ic_gps
                WrapType.NORMAL -> when (type) {
                    Location.Type.ADDRESS -> Res.drawable.ic_location_address
                    Location.Type.POI -> Res.drawable.ic_action_about
                    Location.Type.STATION -> Res.drawable.ic_location_station
                    Location.Type.COORD -> Res.drawable.ic_gps
                    else -> Res.drawable.ic_location
                }
                else -> Res.drawable.ic_location
            }
        }

    fun _getName(): String {
        // FIXME improve
        return if (type == Location.Type.COORD) {
            location.getCoordName()
        } else {
            location.uniqueShortName.takeIf { !it.isNullOrEmpty() }
                ?: id.takeIf { !it.isNullOrEmpty() }
                ?: ""
        }
    }

    val fullName: String
        get() = if (name != null) {
            if (place == null) name!! else "$name, $place"
        } else {
            _getName()
        }

    val latLng: LatLng
        get() = LatLng(lat / 1E6, lon / 1E6)

    fun isSamePlace(other: WrapLocation?): Boolean {
        return other != null && isSamePlaceInt(other.lat, other.lon)
    }

    fun isSamePlace(otherLat: Double, otherLon: Double): Boolean {
        return isSamePlaceInt((otherLat * 1E6).toInt(), (otherLon * 1E6).toInt())
    }

    private fun isSamePlaceInt(otherLat: Int, otherLon: Int): Boolean {
        return (lat / 1000 == otherLat / 1000) && (lon / 1000 == otherLon / 1000)
    }

    override fun toString(): String {
        return fullName
    }
}
