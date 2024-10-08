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

package de.grobox.transportr.ui.trips.detail

import de.libf.transportrng.data.settings.SettingsManager
import de.grobox.transportr.ui.trips.TripQuery
import de.libf.ptek.NetworkProvider
import de.libf.ptek.dto.QueryTripsResult.Status.OK
import de.libf.ptek.dto.Trip
import de.libf.ptek.dto.TripOptions
import kotlinx.coroutines.flow.MutableStateFlow

suspend fun MutableStateFlow<Trip?>.reload(
    networkProvider: NetworkProvider,
    settingsManager: SettingsManager,
    query: TripQuery,
    errorString: String
): String? {
    try {
        val queryTripsResult = networkProvider.queryTrips(
            query.from.location, query.via?.location, query.to.location, query.date - 5000, true,
            TripOptions(query.products.toSet(), settingsManager.optimize, settingsManager.walkSpeed, null, null)
        )!!
        if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
            val oldTrip = this.value ?: throw IllegalStateException()

            for (newTrip in queryTripsResult.trips) {
                if (oldTrip.isTheSame(newTrip)) {
                    this.emit(newTrip)
                    return null
                }
            }

            return errorString
        } else {
            return errorString + "\n" + queryTripsResult.status.name
        }
    } catch (e: Exception) {
        return errorString + "\n" + e.toString()
    }
}

private fun Trip.isTheSame(newTrip: Trip): Boolean {
    // we can not rely on the trip ID as it is too generic with some providers
    if (numChanges != newTrip.numChanges) return false
    if (legs.size != newTrip.legs.size) return false
    if (getPlannedDuration() != newTrip.getPlannedDuration()) return false
    if (firstPublicLeg?.departureTime(true) != newTrip.firstPublicLeg?.departureTime(true)) return false
    if (lastPublicLeg?.arrivalTime(true) != newTrip.lastPublicLeg?.arrivalTime(true)) return false
    if (firstPublicLeg?.line?.label != newTrip.firstPublicLeg?.line?.label) return false
    if (lastPublicLeg?.line?.label != newTrip.lastPublicLeg?.line?.label) return false
    if (firstPublicLeg == null && firstDepartureTime != newTrip.firstDepartureTime) return false
    if (lastPublicLeg == null && lastArrivalTime != newTrip.lastArrivalTime) return false
    return true
}

private fun Trip.getPlannedDuration(): Long {
    val first = firstPublicLeg?.departureTime(true) ?: firstDepartureTime
    val last = lastPublicLeg?.arrivalTime(true) ?: lastArrivalTime

    if (first == null || last == null) return -1

    return last - first
}