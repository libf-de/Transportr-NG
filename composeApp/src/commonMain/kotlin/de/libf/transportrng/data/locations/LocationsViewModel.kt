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
package de.libf.transportrngocations

import androidx.lifecycle.viewModelScope
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.libf.transportrng.data.locations.FavoriteLocation
import de.libf.transportrng.data.locations.HomeLocation
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.locations.WorkLocation
import de.libf.transportrng.data.locations.WrapLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class LocationsViewModel protected constructor(
    transportNetworkManager: TransportNetworkManager?,
    private val locationRepository: LocationRepository
) : TransportNetworkViewModel(
    transportNetworkManager!!
) {
    val home: Flow<HomeLocation?> = locationRepository.homeLocation
    val work: Flow<WorkLocation?> = locationRepository.WorkLocation
    val locations: Flow<List<FavoriteLocation>> = locationRepository.favoriteLocations

    fun setHome(wrapLocation: WrapLocation?) {
        viewModelScope.launch {
            locationRepository.setHomeLocation(wrapLocation!!)
        }
    }

    fun setWork(wrapLocation: WrapLocation?) {
        viewModelScope.launch {
            locationRepository.setWorkLocation(wrapLocation!!)
        }
    }
}
