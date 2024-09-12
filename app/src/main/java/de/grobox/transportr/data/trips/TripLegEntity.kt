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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.grobox.transportr.data.locations.GenericLocation
import de.libf.ptek.dto.IndividualLeg
import de.libf.ptek.dto.Point

@Entity(
    tableName = "tripLegs",
    foreignKeys = [
        ForeignKey(entity = TripEntity::class, parentColumns = ["uid"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = LineEntity::class, parentColumns = ["uid"], childColumns = ["lineId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = GenericLocation::class, parentColumns = ["uid"], childColumns = ["destinationId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StopEntity::class, parentColumns = ["uid"], childColumns = ["departureStopId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StopEntity::class, parentColumns = ["uid"], childColumns = ["arrivalStopId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("tripId", "departureTime", "departureStopId", "arrivalTime", "arrivalStopId", unique = true)
    ]
)
data class TripLegEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "tripId") val tripId: Long,
    val legNumber: Int,

    val isPublicLeg: Boolean,
    val departureId: Long?,
    val arrivalId: Long?,
    val path: List<Point>?,

    @ColumnInfo(name = "lineId") val lineId: Long? = null,
    @ColumnInfo(name = "destinationId") val destinationId: Long? = null,
    @ColumnInfo(name = "departureStopId") val departureStopId: Long? = null,
    @ColumnInfo(name = "arrivalStopId") val arrivalStopId: Long? = null,
    val intermediateStops: List<Long>? = null,
    //intermediateStops
    val message: String? = null,

    val individualType: IndividualLeg.Type? = null,
    val departureTime: Long?,
    val arrivalTime: Long?,
    val min: Int? = null,
    val distance: Int? = null
)
