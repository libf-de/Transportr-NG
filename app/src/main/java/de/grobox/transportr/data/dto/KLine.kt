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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class KLine(
    @PrimaryKey val id: String,
    val network: String? = null,
    val product: KProduct = KProduct.UNKNOWN,
    val label: String? = null,
    val name: String? = null,
    val style: KStyle? = null,
    val attributes: Set<Attr>? = null,
    val message: String? = null,
    val altName: String? = null
) : Comparable<KLine> {
    enum class Attr { CIRCLE_CLOCKWISE, CIRCLE_ANTICLOCKWISE, SERVICE_REPLACEMENT, LINE_AIRPORT, WHEEL_CHAIR_ACCESS, BICYCLE_CARRIAGE }

    companion object {
        val FOOTWAY = KLine(id = "FOOTWAY", product = KProduct.FOOTWAY)
        val TRANSFER = KLine(id = "TRANSFER", product = KProduct.TRANSFER)
        val SECURE_CONNECTION = KLine(id = "SECURE_CONNECTION", product = KProduct.SECURE_CONNECTION)
        val DO_NOT_CHANGE = KLine(id = "DO_NOT_CHANGE", product = KProduct.DO_NOT_CHANGE)

        val DEFAULT_LINE_COLOR = KStyle.RED
    }

    val productCode: Char
        get() = product.c

    fun hasAttr(attr: Attr): Boolean {
        return attributes?.contains(attr) ?: false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KLine) return false

        if(!other.network.equals(this.network)) return false
        if(other.product != this.product) return false
        if(!other.label.equals(this.label)) return false

        return true
    }

    override fun hashCode(): Int {
        return network.hashCode()
            .times(31).plus(product.hashCode())
            .times(31).plus(label.hashCode())
    }

    override fun compareTo(other: KLine): Int {
        return ComparisonChain.start() //
            .compare(this.network, other.network, Ordering.natural<String?>().nullsLast()) //
            .compare(this.product, other.product, Ordering.natural<KProduct>().nullsLast()) //
            .compare(this.label, other.label, Ordering.natural<String?>().nullsLast()) //
            .result();
    }
}