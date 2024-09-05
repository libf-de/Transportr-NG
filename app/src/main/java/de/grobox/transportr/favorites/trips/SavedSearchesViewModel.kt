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

import androidx.lifecycle.LiveData
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.locations.LocationsViewModel
import de.grobox.transportr.networks.TransportNetworkManager

abstract class SavedSearchesViewModel protected constructor(
    val application: TransportrApplication,
    transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    private val searchesRepository: SearchesRepository
) : LocationsViewModel(application, transportNetworkManager, locationRepository) {
    val favoriteTrips: LiveData<List<FavoriteTripItem>> = searchesRepository.favoriteTrips

    fun updateFavoriteState(item: FavoriteTripItem?) {
        searchesRepository.updateFavoriteState(item!!)
    }

    fun setFavoriteTrip(item: FavoriteTripItem, isFavorite: Boolean) {
		searchesRepository.updateFavoriteState(item.uid, isFavorite)
	}

    fun removeFavoriteTrip(item: FavoriteTripItem?) {
        searchesRepository.removeSearch(item!!)
    }
}
