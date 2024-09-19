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

package de.libf.transportrng.data.locations

import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.ptek.NetworkId
import de.libf.ptek.dto.Location
import de.libf.transportrng.data.locations.FavoriteLocation.FavLocationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepository
constructor(private val locationDao: LocationDao, transportNetworkManager: TransportNetworkManager) {

    private val networkId: Flow<NetworkId?> = transportNetworkManager.networkId


    val homeLocation: Flow<HomeLocation?> = networkId.flatMapLatest { networkId ->
        locationDao.getHomeLocationAsFlow(networkId)
    }
    val WorkLocation: Flow<WorkLocation?> = networkId.flatMapLatest { networkId ->
        locationDao.getWorkLocationAsFlow(networkId)
    }
    val favoriteLocations: Flow<List<FavoriteLocation>> = networkId.flatMapLatest { networkId ->
        locationDao.getFavoriteLocationsAsFlow(networkId)
    }
    val allLocations: Flow<List<GenericLocation>> = networkId.flatMapLatest { networkId ->
        locationDao.getAllLocationsAsFlow(networkId)
    }

    suspend fun setHomeLocation(l: WrapLocation) {
        networkId.collectLatest { networkId ->
            val favLocation = locationDao.getFavoriteLocation(networkId!!, l.type, l.id, l.lat, l.lon, l.place, l.name)
            if(favLocation == null) locationDao.addFavoriteLocation(FavoriteLocation(networkId, l))

            locationDao.addHomeLocation(HomeLocation(networkId, l))
        }
    }

//    fun setHomeLocation(location: WrapLocation) {
//        runOnBackgroundThread {
//            // add also as favorite location if it doesn't exist already
//            val favoriteLocation = getFavoriteLocation(networkId, location)
//            if (favoriteLocation == null) locationDao.addFavoriteLocation(FavoriteLocation(networkId.value, location))
//
//            locationDao.addHomeLocation(HomeLocation(networkId.value!!, location))
//        }
//    }

    suspend fun setWorkLocation(l: WrapLocation) {
        networkId.collectLatest { networkId ->
            val favLocation = locationDao.getFavoriteLocation(networkId!!, l.type, l.id, l.lat, l.lon, l.place, l.name)
            if(favLocation == null) locationDao.addFavoriteLocation(FavoriteLocation(networkId, l))

            locationDao.addWorkLocation(WorkLocation(networkId, l))
        }
    }

//    fun setWorkLocation(location: WrapLocation) {
//        runOnBackgroundThread {
//            // add also as favorite location if it doesn't exist already
//            val favoriteLocation = getFavoriteLocation(networkId.value, location)
//            if (favoriteLocation == null) locationDao.addFavoriteLocation(FavoriteLocation(networkId.value, location))
//
//            locationDao.addWorkLocation(WorkLocation(networkId.value!!, location))
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun addFavoriteLocation(l: WrapLocation, type: FavLocationType): FavoriteLocation? {
        if (l.type == Location.Type.COORD || l.wrapType != WrapLocation.WrapType.NORMAL) return null

        return networkId.mapLatest { networkId ->
            val favoriteLocation = if (l is FavoriteLocation) {
                l
            } else {
                val location = findExistingLocation(l)
                location as? FavoriteLocation ?: FavoriteLocation(
                    networkId ?: return@mapLatest null,
                    l)
            }
            favoriteLocation.add(type)

            return@mapLatest if (favoriteLocation.uid != 0L) {
                locationDao.addFavoriteLocation(favoriteLocation)
                favoriteLocation
            } else {
                val existingLocation = getFavoriteLocation(networkId, favoriteLocation)
                if (existingLocation != null) {
                    existingLocation.add(type)
                    locationDao.addFavoriteLocation(existingLocation)
                    existingLocation
                } else {
                    val uid = locationDao.addFavoriteLocation(favoriteLocation)
                    FavoriteLocation(uid, networkId ?: return@mapLatest null, favoriteLocation)
                }
            }
        }.first()


    }

//    @WorkerThread
//    fun addFavoriteLocation(wrapLocation: WrapLocation, type: FavLocationType): FavoriteLocation? {
//        if (wrapLocation.type == Location.Type.COORD || wrapLocation.wrapType != NORMAL) return null
//
//        val favoriteLocation = if (wrapLocation is FavoriteLocation) {
//            wrapLocation
//        } else {
//            val location = findExistingLocation(wrapLocation)
//            location as? FavoriteLocation ?: FavoriteLocation(networkId.value, wrapLocation)
//        }
//        favoriteLocation.add(type)
//
//        return if (favoriteLocation.uid != 0L) {
//            locationDao.addFavoriteLocation(favoriteLocation)
//            favoriteLocation
//        } else {
//            val existingLocation = getFavoriteLocation(networkId.value, favoriteLocation)
//            if (existingLocation != null) {
//                existingLocation.add(type)
//                locationDao.addFavoriteLocation(existingLocation)
//                existingLocation
//            } else {
//                val uid = locationDao.addFavoriteLocation(favoriteLocation)
//                FavoriteLocation(uid, networkId.value, favoriteLocation)
//            }
//        }
//    }

    private suspend fun getFavoriteLocation(networkId: NetworkId?, l: WrapLocation?): FavoriteLocation? {
        return if (l == null) null else locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name)
    }

    /**
     * This checks existing ADDRESS Locations for geographic proximity
     * before adding the given location as a favorite.
     * The idea is too prevent duplicates of addresses with slightly different coordinates.
     *
     * @return The given {@link WrapLocation} or if found, the existing one
     */
    private suspend fun findExistingLocation(location: WrapLocation): WrapLocation {
        return favoriteLocations.mapLatest { favoriteLocations ->
            favoriteLocations.filter {
                it.type == Location.Type.ADDRESS && it.name != null && it.name == location.name && it.isSamePlace(location)
            }?.forEach { return@mapLatest it }

            return@mapLatest location
        }.first()
    }

//    private fun findExistingLocation(location: WrapLocation): WrapLocation {
//        favoriteLocations.value?.filter {
//            it.type == Location.Type.ADDRESS && it.name != null && it.name == location.name && it.isSamePlace(location)
//        }?.forEach { return it }
//        return location
//    }

}
