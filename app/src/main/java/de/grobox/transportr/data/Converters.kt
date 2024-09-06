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
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KProduct.Companion.toCodes
import de.schildbach.pte.NetworkId
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
    fun fromLocationType(locationType: KLocation.Type): String {
        return locationType.name
    }

    @JvmStatic
    @TypeConverter
    fun toLocationType(type: String?): KLocation.Type {
        return try {
            KLocation.Type.valueOf(type!!)
        } catch (e: IllegalArgumentException) {
            KLocation.Type.ANY
        }
    }

    @TypeConverter
    fun fromProducts(products: Set<KProduct>?): String? {
        if (products == null) return null
        return String(products.toCodes())
    }

    @JvmStatic
    @TypeConverter
    fun toProducts(codes: String?): Set<KProduct>? {
        if (codes == null) return null
        return KProduct.fromCodes(codes.toCharArray())
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
