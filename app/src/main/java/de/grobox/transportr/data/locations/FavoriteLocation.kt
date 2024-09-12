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
package de.grobox.transportr.data.locations

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import de.grobox.transportr.R
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product
import de.grobox.transportr.locations.WrapLocation
import de.libf.ptek.NetworkId


@Entity(tableName = "locations", indices = [Index("networkId"), Index("id"), Index(value = ["networkId", "id"], unique = true)])
class FavoriteLocation : StoredLocation {
    enum class FavLocationType {
        FROM, VIA, TO
    }

    var fromCount: Int
    var viaCount: Int
    var toCount: Int

    constructor(
        uid: Long, networkId: NetworkId?, type: Location.Type?, id: String?, lat: Int, lon: Int,
        place: String?, name: String?, products: Set<Product>?, fromCount: Int, viaCount: Int,
        toCount: Int
    ) : super(uid, networkId!!, type, id, lat, lon, place, name, products) {
        this.fromCount = fromCount
        this.viaCount = viaCount
        this.toCount = toCount
    }

    @Ignore
    constructor(uid: Long, networkId: NetworkId?, l: WrapLocation?) : super(uid, networkId!!, l!!) {
        this.fromCount = 0
        this.viaCount = 0
        this.toCount = 0
    }

    @Ignore
    constructor(networkId: NetworkId?, l: WrapLocation?) : super(networkId!!, l!!) {
        this.fromCount = 0
        this.viaCount = 0
        this.toCount = 0
    }

    @Ignore
    constructor(networkId: NetworkId?, l: Location?) : super(networkId!!, l!!) {
        this.fromCount = 0
        this.viaCount = 0
        this.toCount = 0
    }

    @get:DrawableRes
    override val drawableInt: Int
        get() = when (type) {
            Location.Type.ADDRESS -> R.drawable.ic_location_address_fav
            Location.Type.POI -> R.drawable.ic_location_poi_fav
            Location.Type.STATION -> R.drawable.ic_location_station_fav
            else -> R.drawable.ic_location
        }

    fun add(type: FavLocationType) {
        when (type) {
            FavLocationType.FROM -> {
                fromCount++
                return
            }

            FavLocationType.VIA -> {
                viaCount++
                return
            }

            FavLocationType.TO -> toCount++
        }
    }

    override fun toString(): String {
        return super.toString() + "[" + fromCount + "]"
    }

    companion object {
        @JvmField
        val FromComparator: Comparator<FavoriteLocation> =
            Comparator { loc1: FavoriteLocation, loc2: FavoriteLocation -> loc2.fromCount - loc1.fromCount }

        @JvmField
        val ViaComparator: Comparator<FavoriteLocation> =
            Comparator { loc1: FavoriteLocation, loc2: FavoriteLocation -> loc2.viaCount - loc1.viaCount }

        @JvmField
        val ToComparator: Comparator<FavoriteLocation> =
            Comparator { loc1: FavoriteLocation, loc2: FavoriteLocation -> loc2.toCount - loc1.toCount }
    }
}
