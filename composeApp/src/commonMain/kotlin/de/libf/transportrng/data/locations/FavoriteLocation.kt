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

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product
import de.libf.ptek.NetworkId
import org.jetbrains.compose.resources.DrawableResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_location
import transportr_ng.composeapp.generated.resources.ic_location_address_fav
import transportr_ng.composeapp.generated.resources.ic_location_poi_fav
import transportr_ng.composeapp.generated.resources.ic_location_station_fav


@Entity(tableName = "locations", indices = [Index("networkId"), Index("id"), Index(value = ["networkId", "id"], unique = true)])
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true) override val uid: Long,
    override val networkId: NetworkId,
    override val type: Location.Type,
    override val id: String?,
    override val lat: Int,
    override val lon: Int,
    override val place: String?,
    override val name: String?,
    override val products: Set<Product>?,
    var fromCount: Int,
    var viaCount: Int,
    var toCount: Int
) : StoredLocation(uid, networkId, type, id, lat, lon, place, name, products) {
    enum class FavLocationType {
        FROM, VIA, TO
    }


    @Ignore
    constructor(uid: Long, networkId: NetworkId, l: WrapLocation) : this(uid,
        networkId,
        l.type,
        l.id,
        l.lat,
        l.lon,
        l.place,
        l.name,
        l.products,
        0,
        0,
        0)

    @Ignore
    constructor(networkId: NetworkId, l: WrapLocation) : this(0,
        networkId,
        l.type,
        l.id,
        l.lat,
        l.lon,
        l.place,
        l.name,
        l.products,
        0,
        0,
        0)

    @Ignore
    constructor(networkId: NetworkId, l: Location) : this(
        0,
        networkId,
        l.type,
        l.id,
        if (l.hasCoords) l.latAs1E6 else 0,
        if (l.hasCoords) l.lonAs1E6 else 0,
        l.place,
        l.name,
        l.products,
        0,
        0,
        0)

    override val drawableInt: DrawableResource
        get() = when (type) {
            Location.Type.ADDRESS -> Res.drawable.ic_location_address_fav
            Location.Type.POI -> Res.drawable.ic_location_poi_fav
            Location.Type.STATION -> Res.drawable.ic_location_station_fav
            else -> Res.drawable.ic_location
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
        val FromComparator: Comparator<FavoriteLocation> =
            Comparator { loc1: FavoriteLocation, loc2: FavoriteLocation -> loc2.fromCount - loc1.fromCount }

        val ViaComparator: Comparator<FavoriteLocation> =
            Comparator { loc1: FavoriteLocation, loc2: FavoriteLocation -> loc2.viaCount - loc1.viaCount }

        val ToComparator: Comparator<FavoriteLocation> =
            Comparator { loc1: FavoriteLocation, loc2: FavoriteLocation -> loc2.toCount - loc1.toCount }
    }
}
