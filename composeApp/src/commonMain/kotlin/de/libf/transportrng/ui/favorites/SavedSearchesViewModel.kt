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
package de.grobox.transportr.favorites.trips

import androidx.lifecycle.viewModelScope
import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.transportrng.data.favorites.FavoriteTripItem
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrngocations.LocationsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

abstract class SavedSearchesViewModel protected constructor(
    transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    private val searchesRepository: SearchesRepository
) : LocationsViewModel(transportNetworkManager, locationRepository) {
    val favoriteTrips = searchesRepository.favoriteTrips
    val specialLocations = combine(home, work) { home, work ->
        listOf(FavoriteTripItem(home), FavoriteTripItem(work))
    }

    fun updateFavoriteState(item: FavoriteTripItem?) {
        viewModelScope.launch {
            searchesRepository.updateFavoriteState(item!!)
        }
    }

    fun setFavoriteTrip(item: FavoriteTripItem, isFavorite: Boolean) {
		viewModelScope.launch {
            searchesRepository.updateFavoriteState(item.uid, isFavorite)
        }
	}

    fun removeFavoriteTrip(item: FavoriteTripItem?) {
        viewModelScope.launch {
            searchesRepository.removeSearch(item!!)
        }
    }
}
