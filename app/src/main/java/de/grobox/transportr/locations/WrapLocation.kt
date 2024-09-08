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
package de.grobox.transportr.locations

import androidx.annotation.DrawableRes
import androidx.room.Ignore
import com.google.common.base.Preconditions
import com.google.common.base.Strings
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KPoint
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.utils.TransportrUtils.getCoordName
import org.maplibre.android.geometry.LatLng
import java.io.Serializable

@kotlinx.serialization.Serializable
open class WrapLocation(
    val type: KLocation.Type,
    val id: String? = null,
    val lat: Int,
    val lon: Int,
    val place: String? = null,
    @JvmField val name: String? = null,
    val products: Set<KProduct>? = null,
    @Ignore val wrapType: WrapType = WrapType.NORMAL
) : Serializable {
    enum class WrapType {
        NORMAL, GPS
    }

    constructor(l: KLocation) : this(
        l.type!!,
        l.id,
        if (l.hasCoords) l.latAs1E6 else 0,
        if (l.hasCoords) l.lonAs1E6 else 0,
        l.place,
        l.name,
        l.products
    )

    constructor(wrapType: WrapType) : this(
        type = KLocation.Type.ANY,
        wrapType = wrapType,
        lat = 0,
        lon = 0
    ) {
        Preconditions.checkArgument(wrapType != WrapType.NORMAL, "Type can't be normal")
    }

    constructor(latLng: LatLng) : this(
        type = KLocation.Type.COORD,
        lat = (latLng.latitude * 1E6).toInt(),
        lon = (latLng.longitude * 1E6).toInt()
    ) {
        Preconditions.checkArgument(lat != 0 || lon != 0, "Null Island is not a valid location")
    }

    val location: KLocation
        get() {
            val point = KPoint.from1E6(lat, lon)
            if (type == KLocation.Type.ANY && id != null) {
                return KLocation(
                    locId = null,
                    type = type,
                    coord = point,
                    place = place,
                    name = name,
                    products = products
                )
            }
            return KLocation(
                locId = id,
                type = type,
                coord = point,
                place = place,
                name = name,
                products = products
            )
        }

    fun hasId(): Boolean {
        return !Strings.isNullOrEmpty(id)
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

    @get:DrawableRes
    open val drawableInt: Int
        get() {
            return when (wrapType) {
                WrapType.GPS -> R.drawable.ic_gps
                WrapType.NORMAL -> when (type) {
                    KLocation.Type.ADDRESS -> R.drawable.ic_location_address
                    KLocation.Type.POI -> R.drawable.ic_action_about
                    KLocation.Type.STATION -> R.drawable.ic_location_station
                    KLocation.Type.COORD -> R.drawable.ic_gps
                    else -> R.drawable.ic_location
                }
                else -> R.drawable.ic_location
            }
        }

    fun getName(): String {
        // FIXME improve
        return if (type == KLocation.Type.COORD) {
            getCoordName(location)
        } else {
            location.uniqueShortName.takeIf { !it.isNullOrEmpty() }
                ?: id.takeIf { !it.isNullOrEmpty() }
                ?: ""
        }
    }

    val fullName: String
        get() = if (name != null) {
            if (place == null) name else "$name, $place"
        } else {
            getName()
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
