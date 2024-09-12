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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import de.grobox.transportr.data.locations.GenericLocation
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.Leg
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Trip

private fun Stop.toStopEntity(locationId: Long): StopEntity {
    return StopEntity(
        uid = 0,
        locationId = locationId,
        plannedArrivalTime = this.plannedArrivalTime,
        predictedArrivalTime = this.predictedArrivalTime,
        plannedArrivalPosition = this.plannedArrivalPosition,
        predictedArrivalPosition = this.predictedArrivalPosition,
        arrivalCancelled = this.arrivalCancelled,
        plannedDepartureTime = this.plannedDepartureTime,
        predictedDepartureTime = this.predictedDepartureTime,
        plannedDeparturePosition = this.plannedDeparturePosition,
        predictedDeparturePosition = this.predictedDeparturePosition,
        departureCancelled = this.departureCancelled,
    )
}

@Dao
interface TripsDao {

    data class StopWithLocation(
        @Embedded
        val stop: StopEntity,

        @Relation(parentColumn = "locationId", entityColumn = "uid")
        val location: GenericLocation
    ) {
        fun toStop(): Stop {
            return Stop(
                location = location.toLocation(),
                plannedArrivalTime = stop.plannedArrivalTime,
                predictedArrivalTime = stop.predictedArrivalTime,
                plannedArrivalPosition = stop.plannedArrivalPosition,
                predictedArrivalPosition = stop.predictedArrivalPosition,
                arrivalCancelled = stop.arrivalCancelled,
                plannedDepartureTime = stop.plannedDepartureTime,
                predictedDepartureTime = stop.predictedDepartureTime,
                plannedDeparturePosition = stop.plannedDeparturePosition,
                predictedDeparturePosition = stop.predictedDeparturePosition,
                departureCancelled = stop.departureCancelled
            )
        }
    }


    data class TripLegWithStops(
        @Embedded
        val tripLeg: TripLegEntity,

        @Relation(
            entity = StopEntity::class,
            associateBy = Junction(
                TripLegToStopsCrossRef::class,
                parentColumn = "tripLegId",
                entityColumn = "stopId"
            ),
            parentColumn = "uid",
            entityColumn = "uid"
        )
        val intermediateStops: List<StopWithLocation>,

        @Relation(
            entity = StopEntity::class,
            parentColumn = "departureStopId",
            entityColumn = "uid"
        )
        val departureStop: StopWithLocation,

        @Relation(
            entity = StopEntity::class,
            parentColumn = "arrivalStopId",
            entityColumn = "uid"
        )
        val arrivalStop: StopWithLocation,

        @Relation(
            parentColumn = "lineId",
            entityColumn = "id"
        )
        val line: LineEntity,

        @Relation(
            parentColumn = "destinationId",
            entityColumn = "uid"
        )
        val destination: GenericLocation
    )

    data class TripWithLegs(
        @Embedded
        val trip: TripEntity,

        @Relation(
            entity = TripLegEntity::class,
            parentColumn = "id",
            entityColumn = "tripId"
        )
        val legs: List<TripLegWithStops>,

        @Relation(
            parentColumn = "fromId",
            entityColumn = "id"
        )
        val from: GenericLocation,

        @Relation(
            parentColumn = "toId",
            entityColumn = "id"
        )
        val to: GenericLocation
    ) {
        fun toTrip(): Trip {
            return Trip(
                id = trip.id,
                from = from.toLocation(),
                to = to.toLocation(),
                legs = legs.map {
                    if(it.tripLeg.isPublicLeg) {
                        Leg(
                            line = it.line.toLine(),
                            destination = it.destination.toLocation(),
                            departureStop = it.departureStop.toStop(),
                            arrivalStop = it.arrivalStop.toStop(),
                            intermediateStops = it.intermediateStops.map { intStop ->
                                intStop.toStop()
                            },
                            path = it.tripLeg.path,
                            message = it.tripLeg.message,
                        )
                    } else {
                        Leg(
                            type = it.tripLeg.individualType ?: Leg.IndividualType.WALK,
                            departure = it.departureStop.location.toLocation(),
                            departureTime = it.tripLeg.departureTime ?: 0L,
                            arrival = it.arrivalStop.location.toLocation(),
                            arrivalTime = it.tripLeg.arrivalTime ?: 0L,
                            path = it.tripLeg.path,
                            distance = it.tripLeg.distance
                        )
                    }
                },
                fares = null,
                capacity = trip.capacity,
                changes = trip.changes,
            )
        }
    }

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun getTripByIdWithLegs(tripId: String): TripWithLegs

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun _addLocation(location: GenericLocation): Long

    @Query("SELECT uid FROM genericLocations WHERE networkId = :networkId AND id = :id")
    fun getLocationId(networkId: NetworkId, id: String): Long

    @Transaction
    fun addLocation(location: GenericLocation): Long {
        val rslt = _addLocation(location)
        val result = if(rslt == -1L)
            getLocationId(location.networkId, location.id!!)
        else
            rslt

        return result
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addStop(stop: StopEntity): Long

    @Query("SELECT uid FROM lines WHERE id = :id")
    fun findLineIdByNameAndLabel(id: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun _addLine(line: LineEntity): Long

    @Transaction
    fun addLine(line: LineEntity): Long {
        val rslt = _addLine(line)
        val result = if (rslt == -1L)
            findLineIdByNameAndLabel(line.id)
        else
            rslt

        return result
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTripLeg(tripLeg: TripLegEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTripEntity(location: TripEntity): Long

    @Transaction
    suspend fun addTrip(trip: Trip, network: NetworkId) {


        val fromId = addLocation(trip.from.toGenericLocation(network))
        val toId = addLocation(trip.to.toGenericLocation(network))

        val tripId = addTripEntity(
            TripEntity(
                uid = 0,
                id = trip.id,
                fromId = fromId,
                toId = toId,
                capacity = trip.capacity ?: listOf(),
                changes = trip.numChanges,
                networkId = network
            )
        )

        trip.legs.forEachIndexed { index, leg ->
            if(leg.isPublicLeg) {
                val stops: MutableList<Long> = mutableListOf()

                val departureStopId = leg.departureStop?.let {
                    val stLoc = it.location.toGenericLocation(network)

                    val stopLoc = addLocation(
                        stLoc
                    )

                    addStop(it.toStopEntity(
                            locationId = stopLoc
                    ))
                }

                val intermediateStops = leg.intermediateStops?.map {
                    val stopLocId = addLocation(it.location.toGenericLocation(network))

                    addStop(it.toStopEntity(
                            locationId = stopLocId
                    ))
                }


                val arrivalStopId = leg.arrivalStop?.let {
                    val arrivalLocId = addLocation(it.location.toGenericLocation(network))

                    addStop(it.toStopEntity(
                            locationId = arrivalLocId
                    ))
                }

                val lineId = leg.line?.let { addLine(it.toLineEntity(network)) }
                val destinationId = leg.destination?.let { addLocation(it.toGenericLocation(network)) }

                val departureId = addLocation(leg.departure.toGenericLocation(network))
                val arrivalId = addLocation(leg.arrival.toGenericLocation(network))


                addTripLeg(TripLegEntity(
                    uid = 0,
                    tripId = tripId,
                    lineId = lineId,
                    destinationId = destinationId,
                    departureStopId = departureStopId,
                    arrivalStopId = arrivalStopId,
                    intermediateStops = intermediateStops,
                    message = leg.message,
                    isPublicLeg = true,
//                    departureId = addLocation(leg.departure.toGenericLocation(network)),
//                    arrivalId = addLocation(leg.arrival.toGenericLocation(network)),
                    departureId = departureId,
                    arrivalId = arrivalId,
                    path = leg.path,
                    departureTime = leg.departureTime,
                    arrivalTime = leg.arrivalTime,
                    legNumber = index
                ))
            }
            else {
                addTripLeg(
                    TripLegEntity(
                        uid = 0,
                        isPublicLeg = false,
                        tripId = tripId,

                        individualType = leg.individualType!!,
                        departureId = addLocation(leg.departure.toGenericLocation(network)),
                        departureTime = leg.departureTime,
                        arrivalId = addLocation(leg.arrival.toGenericLocation(network)),
                        arrivalTime = leg.arrivalTime,
                        path = leg.path,
                        min = leg.min,
                        distance = leg.distance,

                        legNumber = index
                    )
                )
            }
        }
    }

    @Delete
    fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    fun deleteTripById(tripId: String)

    @Query("DELETE FROM tripLegs WHERE tripId = :tripId")
    fun deleteTripLegsByTripId(tripId: String)

    @Query("DELETE FROM tripLegToStopsCrossRef WHERE tripLegId = :tripLegId")
    fun deleteTripLegToStopsCrossRefByTripLegId(tripLegId: Long)

    @Query("DELETE FROM stops WHERE uid NOT IN (SELECT stopId FROM tripLegToStopsCrossRef) AND uid NOT IN (SELECT departureStopId FROM tripLegs) AND uid NOT IN (SELECT arrivalStopId FROM tripLegs)")
    fun cleanupStops()

    @Query("DELETE FROM lines WHERE id NOT IN (SELECT lineId FROM tripLegs)")
    fun cleanupLines()

    @Query("DELETE FROM genericLocations WHERE uid NOT IN (SELECT fromId FROM trips) AND uid NOT IN (SELECT toId FROM trips) AND uid NOT IN (SELECT departureId FROM tripLegs) AND uid NOT IN (SELECT arrivalId FROM tripLegs) AND uid NOT IN (SELECT locationId FROM stops)")
    fun cleanupLocations()

    @Query("DELETE FROM tripLegs WHERE tripId NOT IN (SELECT id FROM trips)")
    fun cleanupTripLegs()

    @Query("DELETE FROM trips WHERE id IN (SELECT tripId FROM tripLegs WHERE arrivalTime < :limitTime)")
    fun deleteTripsArrivingBefore(limitTime: Long)

    @Transaction
    suspend fun deleteTripsArrivingBeforeAndCleanup(limitTime: Long) {
        deleteTripsArrivingBefore(limitTime)
        cleanup()
    }

    @Transaction
    fun cleanup() {
        cleanupStops()
        cleanupLines()
        cleanupLocations()
        cleanupTripLegs()
    }

    @Transaction
    fun deleteTripAndCleanup(tripId: String) {
        deleteTripById(tripId)
        cleanup()
    }
}

private fun Line.toLineEntity(network: NetworkId): LineEntity {
    return LineEntity(
        id = this.id,
        networkId = network,
        product = product,
        label = label,
        name = name,
        style = style,
        attributes = attributes,
        message = message,
        altName = altName,
        uid = 0
    )
}

private fun Location.toGenericLocation(network: NetworkId): GenericLocation {
    return GenericLocation(
        networkId = network,
        l = this
    )

}
