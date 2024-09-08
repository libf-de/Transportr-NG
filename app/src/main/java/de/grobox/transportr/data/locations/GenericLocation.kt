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

package de.grobox.transportr.data.locations

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KPoint
import de.grobox.transportr.data.dto.KProduct
import de.schildbach.pte.NetworkId

@Entity(tableName = "genericLocations", indices = [Index("networkId"), Index("id"), Index(value = ["networkId", "id"], unique = true)])
class GenericLocation(
    uid: Long,
    networkId: NetworkId?,
    type: KLocation.Type?,
    id: String?,
    lat: Int,
    lon: Int,
    place: String?,
    name: String?,
    products: Set<KProduct>?
) : StoredLocation(uid, networkId!!, type, id, lat, lon, place, name, products) {
    @Ignore
    fun toKLocation(): KLocation {
        return KLocation(
            locId = id,
            type = type,
            coord = KPoint.from1E6(lat, lon),
            place = place,
            name = name,
            products = products
        )
    }

    constructor(networkId: NetworkId, l: KLocation) : this(
        0,
        networkId,
        l.type,
        l.id,
        if (l.hasCoords) l.latAs1E6 else 0,
        if (l.hasCoords) l.lonAs1E6 else 0,
        l.place,
        l.name,
        l.products
    )
}