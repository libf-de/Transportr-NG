///*
// * Copyright the original author or authors.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//package de.libf.transportrng.data.pteextension
//
//import com.google.common.base.Strings
//import de.libf.ptek.NetworkId
//import de.libf.ptek.dto.Line
//import de.libf.ptek.dto.Product
//import de.libf.ptek.dto.Style
//import okhttp3.HttpUrl
//import okhttp3.HttpUrl.Companion.toHttpUrl
//import org.json.JSONArray
//import java.util.regex.Pattern
//
///**
// * Provider implementation for Deutsche Bahn (Germany).
// *
// * @author Andreas Schildbach
// */
//class AdvancedDbProvider(apiClient: String?, apiAuthorization: String?, salt: ByteArray?) :
//    ExtendableAbstractHafasClientInterfaceProvider(NetworkId.DB, API_BASE, PRODUCTS_MAP) {
//    constructor(apiAuthorization: String?, salt: ByteArray?) : this(DEFAULT_API_CLIENT, apiAuthorization, salt)
//
//    override fun defaultProducts(): Set<Product> {
//        return Product.ALL
//    }
//
//    init {
//        apiVersion = "1.15"
//        apiExt = "DB.R18.06.a"
//        setApiClient(apiClient)
//        setApiAuthorization(apiAuthorization)
//        requestChecksumSalt = salt
//    }
//
//    override fun splitStationName(name: String): Array<String> {
//        val m = P_SPLIT_NAME_ONE_COMMA.matcher(name)
//        if (m.matches()) return arrayOf(m.group(2), m.group(1))
//        return super.splitStationName(name)
//    }
//
//    override fun splitPOI(poi: String): Array<String> {
//        val m = P_SPLIT_NAME_FIRST_COMMA.matcher(poi)
//        if (m.matches()) return arrayOf(m.group(1), m.group(2))
//        return super.splitStationName(poi)
//    }
//
//    override fun splitAddress(address: String): Array<String> {
//        val m = P_SPLIT_NAME_FIRST_COMMA.matcher(address)
//        if (m.matches()) return arrayOf(m.group(1), m.group(2))
//        return super.splitStationName(address)
//    }
//
//    override fun parseProdList(prodList: JSONArray?, operators: MutableList<String>?, styles: MutableList<Style>?): MutableList<Line> {
//        val prodListLen = prodList!!.length()
//        val lines: MutableList<Line> = ArrayList(prodListLen)
//
//        for (iProd in 0 until prodListLen) {
//            val prod = prodList.getJSONObject(iProd)
//            val name = Strings.emptyToNull(prod.getString("name"))
//            val realNameS = prod.optString("nameS", null)
//            val nameS = prod.optString("addName", realNameS)
//            val number = prod.optString("number", null)
//            val icoIndex = prod.getInt("icoX")
//            val style = styles!![icoIndex]
//            val oprIndex = prod.optInt("oprX", -1)
//            val operator = if (oprIndex != -1) operators!![oprIndex] else null
//            val cls = prod.optInt("cls", -1)
//            val prodCtx = prod.optJSONObject("prodCtx")
//            val id = prodCtx?.optString("lineId", null)
//            val product = if (cls != -1) intToProduct(cls) else continue
//            lines.add(newLine(id, operator, product, name, nameS, number, style))
//        }
//
//        return lines
//    }
//
//    override fun newLine(
//        id: String?,
//        operator: String?,
//        product: Product?,
//        name: String?,
//        shortName: String?,
//        number: String?,
//        style: Style?
//    ): Line {
//        val longName = if (name != null) name + (if (number != null && !name.endsWith(number)) " ($number)" else "")
//        else if (shortName != null) shortName + (if (number != null && !shortName.endsWith(number)) " ($number)" else "")
//        else number
//
//        if (product == Product.BUS || product == Product.TRAM) {
//            // For bus and tram, prefer a slightly shorter label without the product prefix
//            val label = shortName ?: if (number != null && name != null && name.endsWith(number)) number
//            else name
//            return Line(id, operator, product, label, longName, lineStyle(operator, product, label))
//        } else if(product == Product.REGIONAL_TRAIN) {
//            return Line(id, operator, product, shortName, longName, lineStyle(operator, product, name))
//        } else {
//            // Otherwise the longer label is fine
//            return Line(id, operator, product, name, longName, lineStyle(operator, product, name))
//        }
//    }
//
////    override fun newLine(
////        id: String?, operator: String?, product: Product, name: String?,
////        shortName: String?, number: String?, style: Style?
////    ): Line {
////        val longName = if (name != null) name + (if (number != null && !name.endsWith(number)) " ($number)" else "")
////        else if (shortName != null) shortName + (if (number != null && !shortName.endsWith(number)) " ($number)" else "")
////        else number
////
////        if (product == Product.BUS || product == Product.TRAM) {
////            // For bus and tram, prefer a slightly shorter label without the product prefix
////            val label = shortName ?: if (number != null && name != null && name.endsWith(number)) number
////            else name
////            return Line(id, operator, product, label, longName, lineStyle(operator, product, label))
////        } else {
////            // Otherwise the longer label is fine
////            return Line(id, operator, product, shortName ?: name, longName, lineStyle(operator, product, name))
////        }
////    }
//
//    companion object {
//        private val API_BASE: HttpUrl = "https://reiseauskunft.bahn.de/bin/".toHttpUrl()
//        private val PRODUCTS_MAP = arrayOf(
//            Product.HIGH_SPEED_TRAIN,  // ICE-Züge
//            Product.HIGH_SPEED_TRAIN,  // Intercity- und Eurocityzüge
//            Product.HIGH_SPEED_TRAIN,  // Interregio- und Schnellzüge
//            Product.REGIONAL_TRAIN,  // Nahverkehr, sonstige Züge
//            Product.SUBURBAN_TRAIN,  // S-Bahn
//            Product.BUS,  // Busse
//            Product.FERRY,  // Schiffe
//            Product.SUBWAY,  // U-Bahnen
//            Product.TRAM,  // Straßenbahnen
//            Product.ON_DEMAND,  // Anruf-Sammeltaxi
//            null, null, null, null
//        )
//        private const val DEFAULT_API_CLIENT = "{\"id\":\"DB\",\"v\":\"16040000\",\"type\":\"AND\",\"name\":\"DB Navigator\"}"
//
//        private val P_SPLIT_NAME_ONE_COMMA: Pattern = Pattern.compile("([^,]*), ([^,]*)")
//    }
//}
