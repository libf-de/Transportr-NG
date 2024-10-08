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

package de.libf.transportrng.data.searches

import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.ptek.dto.Location
import de.libf.transportrng.data.locations.FavoriteLocation
import de.libf.transportrng.data.locations.LocationDao
import de.libf.ptek.NetworkId
import de.libf.transportrng.data.favorites.FavoriteTripItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class SearchesRepository constructor(
        private val searchesDao: SearchesDao,
        private val locationDao: LocationDao,
        transportNetworkManager: TransportNetworkManager
) {

    private val networkId: Flow<NetworkId?> = transportNetworkManager.networkId

    private val storedSearches = networkId.flatMapLatest { networkId ->
        searchesDao.getStoredSearchesAsFlow(networkId)
    }

    private val favoriteTripItems: MutableStateFlow<List<FavoriteTripItem>> = MutableStateFlow(emptyList())


    val favoriteTrips: Flow<List<FavoriteTripItem>> = storedSearches.mapLatest { searches ->
        searches.mapNotNull {
            val from = locationDao.getFavoriteLocation(it.fromId)
            val via = it.viaId?.let { locationDao.getFavoriteLocation(it) }
            val to = locationDao.getFavoriteLocation(it.toId)
            if(from == null || to == null) return@mapNotNull null
            return@mapNotNull FavoriteTripItem(it, from, via, to)
        }
    }

    suspend fun storeSearch(from: FavoriteLocation?, via: FavoriteLocation?, to: FavoriteLocation?): Long {
        if (from == null || to == null) return 0L
        if (from.type == Location.Type.COORD || via != null && via.type == Location.Type.COORD || to.type == Location.Type.COORD) throw IllegalStateException("COORD made it through")
        if (from.uid == 0L || to.uid == 0L) throw IllegalStateException("From or To wasn't saved properly :(")

        return networkId.mapLatest { networkId ->
            // try to find existing stored search
            var storedSearch = searchesDao.getStoredSearch(networkId!!, from.uid, via?.uid, to.uid)
            if (storedSearch == null) {
                // no search was found, so create a new one
                storedSearch = StoredSearch(networkId, from, via, to)
            }
            return@mapLatest searchesDao.storeSearch(storedSearch)
        }.first()
    }

//    @WorkerThread
//    fun storeSearch(from: FavoriteLocation?, via: FavoriteLocation?, to: FavoriteLocation?): Long {
//        if (from == null || to == null) return 0L
//        if (from.type == Location.Type.COORD || via != null && via.type == Location.Type.COORD || to.type == Location.Type.COORD) throw IllegalStateException("COORD made it through")
//        if (from.uid == 0L || to.uid == 0L) throw IllegalStateException("From or To wasn't saved properly :(")
//
//        // try to find existing stored search
//        var storedSearch = searchesDao.getStoredSearch(networkId.value!!, from.uid, via?.uid, to.uid)
//        if (storedSearch == null) {
//            // no search was found, so create a new one
//            storedSearch = StoredSearch(networkId.value!!, from, via, to)
//        }
//        return searchesDao.storeSearch(storedSearch)
//    }

    suspend fun isFavorite(uid: Long): Boolean {
        return searchesDao.isFavorite(uid)
    }

    suspend fun updateFavoriteState(item: FavoriteTripItem) {
        updateFavoriteState(item.uid, item.favorite)
    }

    suspend fun updateFavoriteState(uid: Long, isFavorite: Boolean) {
        if (uid == 0L) throw IllegalArgumentException()
        searchesDao.setFavorite(uid, isFavorite)
    }

    suspend fun removeSearch(item: FavoriteTripItem) {
        if (item.uid == 0L) throw IllegalArgumentException()
        searchesDao.delete(item)
    }

}
