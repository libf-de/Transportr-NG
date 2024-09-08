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
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KPoint
import de.grobox.transportr.data.dto.KPosition
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KProduct.Companion.toCodes
import de.grobox.transportr.data.dto.KStyle
import de.schildbach.pte.NetworkId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date


object Converters {
    @JvmStatic
    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return Json.encodeToString(list)
    }

    @JvmStatic
    @TypeConverter
    fun toIntList(json: String): List<Int> {
        return Json.decodeFromString(json)
    }

    @JvmStatic
    @TypeConverter
    fun fromKStyle(value: KStyle?): String {
        return Json.encodeToString(value)
    }

    @JvmStatic
    @TypeConverter
    fun toKStyle(json: String): KStyle? {
        return Json.decodeFromString(json)
    }

    @JvmStatic
    @TypeConverter
    fun fromAttrSet(value: Set<KLine.Attr>?): String {
        return Json.encodeToString(value)
    }

    @JvmStatic
    @TypeConverter
    fun toAttrSet(json: String): Set<KLine.Attr>? {
        return Json.decodeFromString(json)
    }

    @JvmStatic
    @TypeConverter
    fun fromKPosition(lineType: KPosition?): String {
        return Json.encodeToString(lineType)
    }

    @JvmStatic
    @TypeConverter
    fun toKPosition(type: String): KPosition? {
        return Json.decodeFromString(type)
    }

    @JvmStatic
    @TypeConverter
    fun fromKPointList(type: List<KPoint>?): String {
        return Json.encodeToString(type)
    }

    @JvmStatic
    @TypeConverter
    fun toKPointList(type: String): List<KPoint>? {
        return Json.decodeFromString(type)
    }

    @JvmStatic
    @TypeConverter
    fun fromLongList(list: List<Long>?): String {
        return Json.encodeToString(list)
    }

    @JvmStatic
    @TypeConverter
    fun toLongList(json: String): List<Long> {
        return Json.decodeFromString(json)
    }

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
