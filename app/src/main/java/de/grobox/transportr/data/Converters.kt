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
import de.libf.ptek.NetworkId
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
import de.libf.ptek.dto.Position
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.Product.Companion.toCodes
import de.libf.ptek.dto.Style
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
    fun fromStyle(value: Style?): String {
        return Json.encodeToString(value)
    }

    @JvmStatic
    @TypeConverter
    fun toStyle(json: String): Style? {
        return Json.decodeFromString(json)
    }

    @JvmStatic
    @TypeConverter
    fun fromAttrSet(value: Set<Line.Attr>?): String {
        return Json.encodeToString(value)
    }

    @JvmStatic
    @TypeConverter
    fun toAttrSet(json: String): Set<Line.Attr>? {
        return Json.decodeFromString(json)
    }

    @JvmStatic
    @TypeConverter
    fun fromPosition(lineType: Position?): String {
        return Json.encodeToString(lineType)
    }

    @JvmStatic
    @TypeConverter
    fun toPosition(type: String): Position? {
        return Json.decodeFromString(type)
    }

    @JvmStatic
    @TypeConverter
    fun fromPointList(type: List<Point>?): String {
        return Json.encodeToString(type)
    }

    @JvmStatic
    @TypeConverter
    fun toPointList(type: String): List<Point>? {
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
    fun fromLocationType(locationType: Location.Type): String {
        return locationType.name
    }

    @JvmStatic
    @TypeConverter
    fun toLocationType(type: String?): Location.Type {
        return try {
            Location.Type.valueOf(type!!)
        } catch (e: IllegalArgumentException) {
            Location.Type.ANY
        }
    }

    @TypeConverter
    fun fromProducts(products: Set<Product>?): String? {
        if (products == null) return null
        return String(products.toCodes())
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
