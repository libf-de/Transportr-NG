package de.libf.transportrng.ui.trips

import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds

sealed class TripDetailState(
    open val refreshing: Boolean,
) {
    data object Loading : TripDetailState(false)

    data class DisplayingWholeTrip(
        val trip: Trip,
        override val refreshing: Boolean = false) : TripDetailState(refreshing)
    data class DisplayingLeg(
        val trip: Trip,
        val area: LatLngBounds,
        override val refreshing: Boolean = false) : TripDetailState(refreshing)
    data class DisplayingLocation(
        val trip: Trip,
        val point: LatLng,
        override val refreshing: Boolean = false) : TripDetailState(refreshing)
    data class ReloadFailed(
        val trip: Trip,
        val error: Throwable,
        override val refreshing: Boolean = false): TripDetailState(refreshing)
    data class Error(
        val error: Throwable) : TripDetailState(false)

    fun updateReload(refreshing: Boolean = this.refreshing): TripDetailState =
        when(this) {
            is Loading -> Loading
            is DisplayingWholeTrip -> DisplayingWholeTrip(trip, refreshing)
            is DisplayingLeg -> DisplayingLeg(trip, area, refreshing)
            is DisplayingLocation -> DisplayingLocation(trip, point, refreshing)
            is ReloadFailed -> ReloadFailed(trip, error, refreshing)
            is Error -> Error(error)
        }

    fun updateTrip(trip: Trip, refreshing: Boolean = false): TripDetailState =
        when(this) {
            is DisplayingLeg -> DisplayingLeg(trip, area, refreshing)
            is DisplayingLocation -> DisplayingLocation(trip, point, refreshing)
            else -> DisplayingWholeTrip(trip, refreshing)
        }

    fun getTripOrNull(): Trip? = when(this) {
        is DisplayingWholeTrip -> trip
        is DisplayingLeg -> trip
        is DisplayingLocation -> trip
        is ReloadFailed -> trip
        else -> null
    }

    /**
     * Returns the ReloadFailed state if we were in a state that contained a trip,
     * or the Error state if there is no trip to be displayed.
     */
    fun getReloadFailedState(exception: Throwable): TripDetailState =
        this.getTripOrNull()?.let {
            ReloadFailed(
                it, exception
            )
        } ?: Error(exception)
}