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

import kotlinx.serialization.Serializable

@Serializable
data class KLeg private constructor(
    val isPublicLeg: Boolean,

    val departure: KLocation,
    val arrival: KLocation,
    val path: List<KPoint>,

    val line: KLine? = null,
    val destination: KLocation? = null,
    val departureStop: KStop? = null,
    val arrivalStop: KStop? = null,
    val intermediateStops: List<KStop>? = null,
    val message: String? = null,

    val individualType: IndividualType? = null,
    val departureTime: Long?,
    val arrivalTime: Long?,
    val distance: Int? = null
) {
    companion object {
        internal fun calculatePath(departure: KLocation?, intermediateStops: List<KStop>?, arrival: KLocation?): List<KPoint> {
            val legPath = mutableListOf<KPoint>()

            if (departure != null && departure.hasLocation) {
                legPath.add(KPoint.fromDouble(departure.latAsDouble, departure.lonAsDouble))
            }

            intermediateStops?.filter {
                it.location.hasLocation
            }?.forEach {
                legPath.add(KPoint.fromDouble(it.location.latAsDouble, it.location.lonAsDouble))
            }


            if (arrival != null && arrival.hasLocation) {
                legPath.add(KPoint.fromDouble(arrival.latAsDouble, arrival.lonAsDouble))
            }

            return legPath.toList()
        }
    }

    constructor(
        line: KLine,
        destination: KLocation?,
        departureStop: KStop,
        arrivalStop: KStop,
        intermediateStops: List<KStop>,
        path: List<KPoint>?,
        message: String?
    ) : this(
        isPublicLeg = true,

        departure = departureStop.location,
        arrival = arrivalStop.location,
        path = path ?: calculatePath(departureStop.location, intermediateStops, arrivalStop.location),

        line = line,
        destination = destination,
        departureStop = departureStop,
        arrivalStop = arrivalStop,
        intermediateStops = intermediateStops,
        message = message,

        departureTime = departureStop.getDepartureTime(false),
        arrivalTime = arrivalStop.getArrivalTime(false)
    )

    constructor(
        type: IndividualType,
        departure: KLocation,
        departureTime: Long,
        arrival: KLocation,
        arrivalTime: Long,
        path: List<KPoint>?,
        distance: Int?
    ) : this(
        isPublicLeg = false,

        departure = departure,
        arrival = arrival,
        path = path ?: calculatePath(departure, null, arrival),

        individualType = type,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        distance = distance
    )

    enum class IndividualType {
        WALK, BIKE, CAR, TRANSFER, CHECK_IN, CHECK_OUT
    }

    fun getDepartureTime(preferPlanTime: Boolean = false): Long? {
        return departureStop?.getDepartureTime(preferPlanTime) ?: departureTime
    }

    fun isDepartureTimePredicted(preferPlanTime: Boolean = false): Boolean {
        return departureStop?.isDepartureTimePredicted(preferPlanTime) ?: false
    }

    val departureDelay: Long
        get() = departureStop?.departureDelay ?: 0L

    val departurePosition: KPosition?
        get() = departureStop?.departurePosition

    val isDeparturePositionPredicted: Boolean
        get() = departureStop?.isDeparturePositionPredicted ?: false

    fun getArrivalTime(preferPlanTime: Boolean = false): Long? {
        return arrivalStop?.getArrivalTime(preferPlanTime) ?: arrivalTime
    }

    fun isArrivalTimePredicted(preferPlanTime: Boolean = false): Boolean {
        return arrivalStop?.isArrivalTimePredicted(preferPlanTime) ?: false
    }

    val arrivalDelay: Long
        get() = arrivalStop?.arrivalDelay ?: 0L

    val arrivalPosition: KPosition?
        get() = arrivalStop?.arrivalPosition

    val isArrivalPositionPredicted: Boolean
        get() = arrivalStop?.isArrivalPositionPredicted ?: false

    val minTime: Long?
        get() = departureStop?.minTime ?: departureTime

    val maxTime: Long?
        get() = arrivalStop?.maxTime ?: arrivalTime

    fun movedCopy(newDepartureTime: Long): KLeg {
        this.arrivalTime?.let {
            this.departureTime?.let {
                val newArrivalTime = newDepartureTime + this.arrivalTime - this.departureTime
                return this.copy(
                    departureTime = newDepartureTime,
                    arrivalTime = newArrivalTime
                )
            }
        }

        return this.copy(
            departureTime = newDepartureTime,
            arrivalTime = newDepartureTime
        )
    }

    val min: Long
        get() = arrivalTime?.let { arr ->
            departureTime?.let { dep ->
                ((arr - dep) / 1000 / 60)
            }
        } ?: Long.MIN_VALUE
}