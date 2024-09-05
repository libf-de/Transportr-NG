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
import de.grobox.transportr.locations.WrapLocation
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Product

@Entity(tableName = "work_locations", indices = [Index(value = ["networkId"], unique = true)])
class WorkLocation : StoredLocation {
    constructor(
        uid: Long,
        networkId: NetworkId?,
        type: LocationType?,
        id: String?,
        lat: Int,
        lon: Int,
        place: String?,
        name: String?,
        products: Set<Product>?
    ) : super(uid, networkId!!, type, id, lat, lon, place, name, products)

    @Ignore
    constructor(networkId: NetworkId, l: WrapLocation?) : super(networkId, l!!)

    @Ignore
    constructor(networkId: NetworkId, l: Location?) : super(networkId, l!!)

    @get:DrawableRes
    override val drawableInt: Int
        get() = R.drawable.ic_work
}
