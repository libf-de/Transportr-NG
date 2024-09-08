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

package de.grobox.transportr.data.trips

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KStyle
import de.schildbach.pte.NetworkId

@Entity(
    tableName = "lines",
    indices = [
        Index("id", unique = true),
        Index("uid", "id", unique = true)
    ]
)
data class LineEntity(
    @PrimaryKey val uid: Long,
    val id: String,
    val networkId: NetworkId?,
    val product: KProduct,
    val label: String?,
    val name: String?,
    val style: KStyle?,
    val attributes: Set<KLine.Attr>?,
    val message: String?,
    val altName: String?
) {
    fun toKLine(): KLine {
        return KLine(
            id = id,
            network = networkId?.name,
            product = product,
            label = label,
            name = name,
            style = style,
            attributes = attributes,
            message = message,
            altName = altName
        )
    }
}
