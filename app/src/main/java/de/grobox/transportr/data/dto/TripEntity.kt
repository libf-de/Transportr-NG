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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.schildbach.pte.NetworkId

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(entity = KLocation::class, parentColumns = ["id"], childColumns = ["fromId"]),
        ForeignKey(entity = KLocation::class, parentColumns = ["id"], childColumns = ["toId"])
    ],
    indices = [
        Index("id", unique = true),
        Index("fromId"),
        Index("toId"),
    ]
)
data class TripEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "fromId") val fromId: String,
    @ColumnInfo(name = "toId") val toId: String,
    val capacity: String,
    val changes: Int,
    val networkId: NetworkId?
)