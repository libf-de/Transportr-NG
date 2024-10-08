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
import androidx.room.PrimaryKey
import de.libf.ptek.NetworkId
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product

abstract class StoredLocation internal constructor(
    @PrimaryKey(autoGenerate = true) open val uid: Long,
    open val networkId: NetworkId,
    type: Location.Type?,
    id: String?,
    lat: Int,
    lon: Int,
    place: String?,
    name: String?,
    products: Set<Product>?
) : WrapLocation(
    type!!, id, lat, lon, place, name, products
) {
    @Ignore
    internal constructor(uid: Long, networkId: NetworkId, l: WrapLocation) : this(
        uid,
        networkId,
        l.type,
        l.id,
        l.lat,
        l.lon,
        l.place,
        l.name,
        l.products
    )

    @Ignore
    internal constructor(networkId: NetworkId, l: WrapLocation) : this(0, networkId, l)

    @Ignore
    internal constructor(networkId: NetworkId, l: Location) : this(
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
