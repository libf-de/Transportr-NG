package de.libf.transportrng.ui.directions

import de.libf.ptek.dto.Departure
import de.libf.ptek.dto.LineDestination
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.favorites.FavoriteTripItem

sealed class DirectionsState {
    object Loading : DirectionsState()
    data class ShowFavorites(
        val favorites: List<FavoriteTripItem>) : DirectionsState()
    data class ShowDepartures(
        val departures: List<Departure>,
        val lines: List<LineDestination>) : DirectionsState()
    data class ShowTrips(
        val trips: Set<Trip>) : DirectionsState()
    data class Error(val message: String) : DirectionsState()
}