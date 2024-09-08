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
import de.grobox.transportr.data.dto.KPosition
import de.grobox.transportr.data.locations.GenericLocation

@Entity(
    tableName = "stops",
    foreignKeys = [
        ForeignKey(entity = GenericLocation::class, parentColumns = ["uid"], childColumns = ["locationId"], onDelete = ForeignKey.CASCADE),
    ]
)
data class StopEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "locationId") val locationId: Long,
    @ColumnInfo(name = "legId") val legId: Long? = null,
    val plannedArrivalTime: Long? = null,
    val predictedArrivalTime: Long? = null,
    val plannedArrivalPosition: KPosition? = null,
    val predictedArrivalPosition: KPosition? = null,
    val arrivalCancelled: Boolean = false,

    val plannedDepartureTime: Long? = null,
    val predictedDepartureTime: Long? = null,
    val plannedDeparturePosition: KPosition? = null,
    val predictedDeparturePosition: KPosition? = null,
    val departureCancelled: Boolean = false
)

@Entity(
    tableName = "tripLegToStopsCrossRef",
    primaryKeys = ["tripLegId", "stopId"],
    indices = [
        Index("tripLegId", "stopId")
    ]
)
data class TripLegToStopsCrossRef(
    val tripLegId: Long,
    val stopId: Long,
)
