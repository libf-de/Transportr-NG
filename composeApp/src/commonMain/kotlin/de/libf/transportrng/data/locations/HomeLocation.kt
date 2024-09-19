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
import de.libf.ptek.NetworkId
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product
import org.jetbrains.compose.resources.DrawableResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_action_home


@Entity(tableName = "home_locations", indices = [Index(value = ["networkId"], unique = true)])
class HomeLocation(
    @PrimaryKey(autoGenerate = true) override val uid: Long,
    override val networkId: NetworkId,
    override val type: Location.Type,
    override val id: String?,
    override val lat: Int,
    override val lon: Int,
    override val place: String?,
    override val name: String?,
    override val products: Set<Product>?
) : StoredLocation(
    uid, networkId, type, id, lat, lon, place, name, products
) {
    @Ignore
    constructor(networkId: NetworkId, l: WrapLocation) : this(0,
        networkId,
        l.type,
        l.id,
        l.lat,
        l.lon,
        l.place,
        l.name,
        l.products)

    @Ignore
    constructor(networkId: NetworkId, l: Location) : this(0,
        networkId,
        l.type,
        l.id,
        if (l.hasCoords) l.latAs1E6 else 0,
        if (l.hasCoords) l.lonAs1E6 else 0,
        l.place,
        l.name,
        l.products)

    override val drawableInt: DrawableResource
        get() = Res.drawable.ic_action_home
}
