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
package de.grobox.transportr.data

import androidx.room.TypeConverter
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Product
import java.util.Date


object Converters {
    @JvmStatic
    @TypeConverter
    fun fromNetworkId(networkId: NetworkId?): String? {
        if (networkId == null) return null
        return networkId.name
    }

    @JvmStatic
    @TypeConverter
    fun toNetworkId(network: String?): NetworkId? {
        return try {
            NetworkId.valueOf(network!!)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @JvmStatic
    @TypeConverter
    fun fromLocationType(locationType: LocationType): String {
        return locationType.name
    }

    @JvmStatic
    @TypeConverter
    fun toLocationType(type: String?): LocationType {
        return try {
            LocationType.valueOf(type!!)
        } catch (e: IllegalArgumentException) {
            LocationType.ANY
        }
    }

    @TypeConverter
    fun fromProducts(products: Set<Product>?): String? {
        if (products == null) return null
        return String(Product.toCodes(products))
    }

    @JvmStatic
    @TypeConverter
    fun toProducts(codes: String?): Set<Product>? {
        if (codes == null) return null
        return Product.fromCodes(codes.toCharArray())
    }

    @JvmStatic
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @JvmStatic
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
}
