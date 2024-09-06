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

package de.grobox.transportr.data.dto

import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class KLocation private constructor(
    @PrimaryKey val id: String?,
    val type: Type?,
    val coord: KPoint?,
    val place: String?,
    val name: String?,
    val products: Set<KProduct>? = null,
    @Ignore private val _igore: Boolean = true
) {

    enum class Type {
        /** Location can represent any of the below. Mainly meant for user input. */
        ANY,
        /** Location represents a station or stop. */
        STATION,
        /** Location represents a point of interest. */
        POI,
        /** Location represents a postal address. */
        ADDRESS,
        /** Location represents a just a plain coordinate, e.g. acquired by GPS. */
        COORD
    }

    constructor(
        locId: String? = null,
        type: Type,
        coord: KPoint? = null,
        place: String? = null,
        name: String? = null,
        products: Set<KProduct>? = null
    ) : this(id = locId, type = type, coord = coord, place = place, name = name, products = products) {
        if(type == Type.ANY) assert(locId == null) { "type ANY cannot have ID" }

        assert(locId?.isNotBlank() ?: true) { "ID cannot be the empty string" }

        assert(place == null || name != null) { "place cannot be set without name" }

        if(type == Type.COORD) {
            assert(coord != null) { "coordinates missing" }
            assert(place == null && name == null) { "coordinates cannot have place or name" }
        }
    }

    constructor(lat: Int, lon: Int) : this(
        type = Type.COORD,
        coord = KPoint.from1E6(lat, lon)
    )

    constructor(coord: KPoint) : this(
        type = Type.COORD,
        coord = coord
    )

    companion object {
        private val NON_UNIQUE_NAMES = listOf("Hauptbahnhof", "Hbf", "Bahnhof", "Bf", "Busbahnhof", "ZOB",
            "Schiffstation", "Schiffst.", "Zentrum", "Markt", "Dorf", "Kirche", "Nord", "Ost", "Süd", "West")
    }

    val hasId: Boolean
        get() = !id.isNullOrBlank()

    val hasCoords: Boolean
        get() = coord != null

    val latAsDouble: Double
        get() = coord!!.lat

    val lonAsDouble: Double
        get() = coord!!.lon

    val latAs1E6: Int
        get() = coord!!.latAs1E6

    val lonAs1E6: Int
        get() = coord!!.lonAs1E6

    val latAs1E5: Int
        get() = coord!!.latAs1E5

    val lonAs1E5: Int
        get() = coord!!.lonAs1E5

    val hasName: Boolean
        get() = !name.isNullOrBlank()

    val hasPlace: Boolean
        get() = !place.isNullOrBlank()

    val hasLocation: Boolean
        get() = hasCoords && (latAs1E6 != 0 || lonAs1E6 != 0)

    val isIdentified: Boolean
        get() = when(type) {
            Type.STATION -> hasId
            Type.POI -> true
            Type.ADDRESS, Type.COORD -> hasCoords
            else -> false
        }

    val uniqueShortName: String?
        get() {
            return if(place != null && name != null && NON_UNIQUE_NAMES.binarySearch(name) >= 0)
                "${place}, $name"
            else if(name != null)
                name
            else if(hasId)
                id!!
            else null
        }

    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if(other !is KLocation) return false

        if(this.type != other.type) return false
        if(this.hasId) return this.id == other.id
        if(this.hasCoords) return this.coord == other.coord

        // only discriminate by name/place if no ids are given
        if(this.place != other.place) return false
        if(this.name != other.name) return false

        return true
    }

    fun equalsAllFields(other: KLocation?): Boolean {
        if(other === this) return true
        if(other == null) return false
        if(other.type != this.type) return false
        if(other.id != this.id) return false
        if(other.coord != this.coord) return false
        if(other.place != this.place) return false
        if(other.name != this.name) return false
        if(other.products != this.products) return false
        return true
    }

    override fun hashCode(): Int {
        return if(id != null)
            type.hashCode()
                .times(31).plus(id.hashCode())
        else
            type.hashCode()
                .times(31).plus(coord.hashCode())
    }


}