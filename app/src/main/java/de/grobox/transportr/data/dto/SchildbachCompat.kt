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

import de.grobox.transportr.data.dto.KLine.Attr
import de.grobox.transportr.data.dto.KStyle.Shape
import de.schildbach.pte.dto.Fare
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Point
import de.schildbach.pte.dto.Position
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Style
import de.schildbach.pte.dto.Trip

fun Line.Attr.toKAttr(): Attr {
    return when(this) {
        Line.Attr.CIRCLE_CLOCKWISE -> Attr.CIRCLE_CLOCKWISE
        Line.Attr.CIRCLE_ANTICLOCKWISE -> Attr.CIRCLE_ANTICLOCKWISE
        Line.Attr.SERVICE_REPLACEMENT -> Attr.SERVICE_REPLACEMENT
        Line.Attr.LINE_AIRPORT -> Attr.LINE_AIRPORT
        Line.Attr.WHEEL_CHAIR_ACCESS -> Attr.WHEEL_CHAIR_ACCESS
        Line.Attr.BICYCLE_CARRIAGE -> Attr.BICYCLE_CARRIAGE
        else -> throw IllegalArgumentException("Unknown line attribute: $this")
    }
}

fun Fare.Type.toKFareType(): KFare.Type {
    return when(this) {
        Fare.Type.ADULT -> KFare.Type.ADULT
        Fare.Type.CHILD -> KFare.Type.CHILD
        Fare.Type.YOUTH -> KFare.Type.YOUTH
        Fare.Type.STUDENT -> KFare.Type.STUDENT
        Fare.Type.MILITARY -> KFare.Type.MILITARY
        Fare.Type.SENIOR -> KFare.Type.SENIOR
        Fare.Type.DISABLED -> KFare.Type.DISABLED
        Fare.Type.BIKE -> KFare.Type.BIKE
        else -> throw IllegalArgumentException("Unknown fare type: $this")
    }
}

fun Fare.toKFare(): KFare {
    return KFare(
        name = name,
        type = type.toKFareType(),
        currency = currency.currencyCode,
        fare = fare,
        unitName = unitName,
        units = units
    )
}

fun Trip.Public.toKLeg(): KLeg {
    return KLeg(
        line = line.toKLine(),
        destination = destination?.toKLocation(),
        departureStop = departureStop.toKStop(),
        arrivalStop = arrivalStop.toKStop(),
        intermediateStops = intermediateStops?.map { it.toKStop() } ?: emptyList(),
        path = path?.mapNotNull { it?.toKPoint() },
        message = message
    )
}

fun Trip.Individual.Type.toKIndividualType(): KLeg.IndividualType {
    return when(this) {
        Trip.Individual.Type.WALK -> KLeg.IndividualType.WALK
        Trip.Individual.Type.BIKE -> KLeg.IndividualType.BIKE
        Trip.Individual.Type.CAR -> KLeg.IndividualType.CAR
        Trip.Individual.Type.TRANSFER -> KLeg.IndividualType.TRANSFER
        Trip.Individual.Type.CHECK_IN -> KLeg.IndividualType.CHECK_IN
        Trip.Individual.Type.CHECK_OUT -> KLeg.IndividualType.CHECK_OUT
        else -> throw IllegalArgumentException("Unknown individual leg type: $this")
    }
}

fun Trip.Individual.toKLeg(): KLeg {
    return KLeg(
        type = type.toKIndividualType(),
        departure = departure.toKLocation(),
        departureTime = departureTime.time,
        arrival = arrival.toKLocation(),
        arrivalTime = arrivalTime.time,
        path = path?.mapNotNull { it?.toKPoint() },
        distance = distance
    )
}

@Deprecated("use public/individual toKLeg()")
fun Trip.Leg.toKLeg(): KLeg {
    if(this is Trip.Public) return toKLeg()
    if(this is Trip.Individual) return toKLeg()
    throw IllegalArgumentException("Unknown leg type: $this")

}

fun Line.toKLine(): KLine {
    fun generateId(network: String?, product: Product?, label: String?, name: String?): String {
        val db = StringBuilder()

        if(network != null) db.append("${network}-")
        if(product != null) db.append("${product.code}-")
        if(label != null) db.append("${label}-")
        if(name != null) db.append(name)

        if(db.isBlank()) return "UNKNOWN_LINE"

        return db.toString()
    }

    return KLine(
        id = id ?: generateId(network, product, label, name),
        network = network,
        product = product?.ordinal
            ?.takeIf { it < KProduct.entries.size }
            ?.let { KProduct.entries[it] }
            ?: KProduct.UNKNOWN,
        label = label,
        name = name,
        style = style?.toKStyle(),
        attributes = this.attrs?.map { it.toKAttr() }?.toSet(),
        message = message,
        altName = null
    )
}

fun KLocation.Type.toLocationType(): LocationType {
    return when(this) {
        KLocation.Type.STATION -> LocationType.STATION
        KLocation.Type.POI -> LocationType.POI
        KLocation.Type.ADDRESS -> LocationType.ADDRESS
        KLocation.Type.COORD -> LocationType.COORD
        else -> LocationType.ANY
    }
}

fun LocationType.toKLocationType(): KLocation.Type {
    return when(this) {
        LocationType.STATION -> KLocation.Type.STATION
        LocationType.POI -> KLocation.Type.POI
        LocationType.ADDRESS -> KLocation.Type.ADDRESS
        LocationType.COORD -> KLocation.Type.COORD
        else -> KLocation.Type.ANY
    }
}

fun KLocation.toLocation(): Location {
    return Location(
        type?.toLocationType(),
        id,
        coord?.toPoint(),
        place,
        name,
        products?.map { it.toProduct() }?.toSet()
    )

}

fun Location.toKLocation(): KLocation {
    return KLocation(
        locId = this.id,
        type = type.toKLocationType(),
        coord = this.coord?.toKPoint(),
        place = this.place,
        name = this.name,
        products = products?.map { it.toKProduct() }?.toSet()
    )
}

fun KPoint.toPoint(): Point {
    return Point.fromDouble(lat, lon)
}

fun Point.toKPoint(): KPoint {
    return KPoint.fromDouble(
        lat = latAsDouble,
        lon = lonAsDouble
    )
}

fun Position.toKPosition(): KPosition {
    return KPosition(
        name = name,
        section = section
    )
}

fun Product.toKProduct(): KProduct {
    return when(this) {
        Product.HIGH_SPEED_TRAIN -> KProduct.HIGH_SPEED_TRAIN
        Product.REGIONAL_TRAIN -> KProduct.REGIONAL_TRAIN
        Product.SUBURBAN_TRAIN -> KProduct.SUBURBAN_TRAIN
        Product.SUBWAY -> KProduct.SUBWAY
        Product.TRAM -> KProduct.TRAM
        Product.BUS -> KProduct.BUS
        Product.FERRY -> KProduct.FERRY
        Product.CABLECAR -> KProduct.CABLECAR
        Product.ON_DEMAND -> KProduct.ON_DEMAND
        else -> KProduct.UNKNOWN
    }
}

fun KProduct.toProduct(): Product? {
    return when(this) {
        KProduct.HIGH_SPEED_TRAIN -> Product.HIGH_SPEED_TRAIN
        KProduct.REGIONAL_TRAIN -> Product.REGIONAL_TRAIN
        KProduct.SUBURBAN_TRAIN -> Product.SUBURBAN_TRAIN
        KProduct.SUBWAY -> Product.SUBWAY
        KProduct.TRAM -> Product.TRAM
        KProduct.BUS -> Product.BUS
        KProduct.FERRY -> Product.FERRY
        KProduct.CABLECAR -> Product.CABLECAR
        KProduct.ON_DEMAND -> Product.ON_DEMAND
        else -> null
    }
}

fun Stop.toKStop(): KStop {
    return KStop(
        location = location.toKLocation(),
        plannedArrivalTime = plannedArrivalTime?.time,
        predictedArrivalTime = predictedArrivalTime?.time,
        plannedArrivalPosition = plannedArrivalPosition?.toKPosition(),
        predictedArrivalPosition = predictedArrivalPosition?.toKPosition(),
        arrivalCancelled = arrivalCancelled,
        plannedDepartureTime = plannedDepartureTime?.time,
        predictedDepartureTime = predictedDepartureTime?.time,
        plannedDeparturePosition = plannedDeparturePosition?.toKPosition(),
        predictedDeparturePosition = predictedDeparturePosition?.toKPosition(),
        departureCancelled = departureCancelled
    )
}

fun Style.Shape.toKShape(): Shape {
    return when(this) {
        Style.Shape.RECT -> Shape.RECT
        Style.Shape.ROUNDED -> Shape.ROUNDED
        Style.Shape.CIRCLE -> Shape.CIRCLE
        else -> throw IllegalArgumentException("Unknown shape: $this")
    }
}

fun Style.toKStyle(): KStyle {
    return KStyle(
        shape = shape.toKShape(),
        backgroundColor = backgroundColor,
        backgroundColor2 = backgroundColor2,
        foregroundColor = foregroundColor,
        borderColor = borderColor
    )
}

fun Trip.toKTrip(): KTrip {
    return KTrip(
        from = from.toKLocation(),
        to = to.toKLocation(),
        legs = legs.mapNotNull {
            when (it) {
                is Trip.Public -> it.toKLeg()
                is Trip.Individual -> it.toKLeg()
                else -> null
            }
        },
        fares = fares?.map { it.toKFare() },
        capacity = capacity?.toList(),
        changes = numChanges
    )
}