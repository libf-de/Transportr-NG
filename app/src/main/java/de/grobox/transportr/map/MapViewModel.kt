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

package de.grobox.transportr.map

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.utils.IntentUtils
import de.grobox.transportr.utils.SingleLiveEvent

class MapViewModel internal constructor(
        application: TransportrApplication,
        transportNetworkManager: TransportNetworkManager,
        locationRepository: LocationRepository,
        searchesRepository: SearchesRepository,
        override val positionController: PositionController
    ) : SavedSearchesViewModel(application, transportNetworkManager, locationRepository, searchesRepository), GpsMapViewModel by GpsMapViewModelImpl(positionController) {

    private val peekHeight = MutableLiveData<Int>()
    private val selectedLocationClicked = MutableLiveData<LatLng?>()
    private val updatedLiveBounds = MutableLiveData<LatLngBounds?>()
    private val selectedLocation = MutableLiveData<WrapLocation?>()
    private val findNearbyStations = SingleLiveEvent<WrapLocation>()
    private val nearbyStationsFound = SingleLiveEvent<Boolean>()

    val mapClicked = SingleLiveEvent<Void>()
    val markerClicked = SingleLiveEvent<Void>()
    val liveBounds: LiveData<LatLngBounds?> = locations.switchMap(this::switchMap)
    var transportNetworkWasChanged = false

    fun getPeekHeight(): LiveData<Int> {
        return peekHeight
    }

    fun setPeekHeight(peekHeight: Int) {
        this.peekHeight.value = peekHeight
    }

    fun getSelectedLocationClicked(): LiveData<LatLng?> {
        return selectedLocationClicked
    }

    fun selectedLocationClicked(latLng: LatLng) {
        selectedLocationClicked.value = latLng
        // reset the selected location right away, observers will ignore this update
        selectedLocationClicked.value = null
    }

    fun selectLocation(location: WrapLocation?) {
        selectedLocation.value = location
        // do not reset the selected location right away, will break incoming geo intent
        // the observing fragment will call clearSelectedLocation() instead when it is done
    }

    fun clearSelectedLocation() {
        selectedLocation.postValue(null)
    }

    fun getSelectedLocation(): LiveData<WrapLocation?> {
        return selectedLocation
    }

    fun findNearbyStations(location: WrapLocation) {
        findNearbyStations.value = location
    }

    fun getFindNearbyStations(): LiveData<WrapLocation> {
        return findNearbyStations
    }

    fun setNearbyStationsFound(found: Boolean) {
        nearbyStationsFound.value = found
    }

    fun nearbyStationsFound(): LiveData<Boolean> {
        return nearbyStationsFound
    }

    fun setGeoUri(geoUri: Uri) {
        val location = IntentUtils.getWrapLocation(geoUri.toString())
        if (location != null) {
            selectLocation(location)
        } else {
            Log.w(MapViewModel::class.java.simpleName, "Invalid geo intent: " + geoUri.toString())
            //Toast.makeText(application.applicationContext, R.string.error_geo_intent, Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchMap(input: List<FavoriteLocation>?): MutableLiveData<LatLngBounds?> {
        if (input == null) {
            updatedLiveBounds.setValue(null)
        } else {
            val points = input
                .filter { it.hasLocation() }
                .map { it.latLng as LatLng }
                .toMutableSet()
            home.value?.let { if (it.hasLocation()) points.add(it.latLng) }
            work.value?.let { if (it.hasLocation()) points.add(it.latLng) }
            positionController.position.value?.let { points.add(LatLng(it)) }
            if (points.size < 2) {
                updatedLiveBounds.setValue(null)
            } else {
                updatedLiveBounds.setValue(LatLngBounds.Builder().includes(ArrayList(points)).build())
            }
        }
        return updatedLiveBounds
    }
}
