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

package de.grobox.transportr.ui.map

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.data.dto.toKLine
import de.grobox.transportr.data.dto.toLocation
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.departures.DeparturesActivity
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.locations.CombinedSuggestionRepository
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.PositionController
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.utils.IntentUtils
import de.grobox.transportr.utils.IntentUtils.presetDirections
import de.grobox.transportr.utils.SingleLiveEvent
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.NearbyLocationsResult
import de.schildbach.pte.dto.QueryDeparturesResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.util.Date
import java.util.EnumSet

class MapViewModel internal constructor(
    application: TransportrApplication,
    private val transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
    override val positionController: PositionController,
    private val combinedSuggestionRepository: CombinedSuggestionRepository,
    ) : SavedSearchesViewModel(application, transportNetworkManager, locationRepository, searchesRepository), GpsMapViewModel by GpsMapViewModelImpl(positionController) {

    private val peekHeight = MutableLiveData<Int>()
    private val selectedLocationClicked = MutableLiveData<LatLng?>()
    private val updatedLiveBounds = MutableLiveData<LatLngBounds?>()

    private var selectedLocationJob: Job? = null
    private val selectedLocation = MutableLiveData<WrapLocation?>()
    private val findNearbyStations = SingleLiveEvent<WrapLocation>()
    private val nearbyStationsFound = SingleLiveEvent<Boolean>()

    private var nearbyStationsJob: Job? = null
    private val _nearbyStationsState = MutableLiveData<NearbyLocationsState>()
    val nearbyStations: LiveData<NearbyLocationsState> = _nearbyStationsState

    val mapClicked = SingleLiveEvent<Void>()
    val markerClicked = SingleLiveEvent<Void>()
    val liveBounds: LiveData<LatLngBounds?> = locations.switchMap(this::switchMap)
    var transportNetworkWasChanged = false

    val locationSuggestions: LiveData<Set<WrapLocation>> = combinedSuggestionRepository.suggestions
    val suggestionsLoading: LiveData<Boolean> = combinedSuggestionRepository.isLoading

    private val _sheetContentState = MutableLiveData<BottomSheetContentState>(BottomSheetContentState.SavedSearches)
    val sheetContentState: LiveData<BottomSheetContentState> = _sheetContentState

    fun suggestLocations(query: String) {
        combinedSuggestionRepository.updateSuggestions(query)
    }

    fun cancelSuggestLocations() {
        combinedSuggestionRepository.cancelSuggestions()
    }

    fun resetSuggestions() {
        combinedSuggestionRepository.reset()
    }

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

        selectedLocationJob?.cancel()

        if(location == null) return

        _sheetContentState.value = BottomSheetContentState.Location(location, emptyList())

        if(!location.hasId()) return

        selectedLocationJob = viewModelScope.launch {
            val deps = transportNetwork
                .value
                ?.networkProvider
                ?.queryDepartures(location.id,
                                    Date(),
                                    DeparturesActivity.MAX_DEPARTURES,
                                    false)
                ?.takeIf { it.status == QueryDeparturesResult.Status.OK }
                ?.let { dep ->
                    dep.stationDepartures
                        .flatMap { it.lines ?: emptyList() }
                        .map { it.line }
                }

            _sheetContentState.value = BottomSheetContentState.Location(
                loc = location,
                lines = deps?.map { it.toKLine() }
            )
        }

        // do not reset the selected location right away, will break incoming geo intent
        // the observing fragment will call clearSelectedLocation() instead when it is done
    }

    fun clearSelectedLocation() {
        selectedLocation.postValue(null)

        _sheetContentState.value = BottomSheetContentState.Empty
    }

    fun getSelectedLocation(): LiveData<WrapLocation?> {
        return selectedLocation
    }

    fun findNearbyStations(location: WrapLocation) {
        //findNearbyStations.value = location

        if(nearbyStationsJob?.isActive == true)
            nearbyStationsJob?.cancel()


        nearbyStationsJob = viewModelScope.launch {
            _nearbyStationsState.value = NearbyLocationsState.Loading

            try {
                val result = withContext(Dispatchers.IO) {
                    transportNetwork.value?.networkProvider?.queryNearbyLocations(
                        EnumSet.of(LocationType.STATION),
                        location.location.toLocation(),
                        1000,
                        0
                    )
                }

                _nearbyStationsState.value = NearbyLocationsState.Success(result!!)
            } catch(e: Exception) {
                _nearbyStationsState.value = NearbyLocationsState.Error(e.message ?: "Unknown error")
            }

        }
    }

    @Deprecated("Use findNearbyStations() instead")
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

    fun findDirectionsFromGpsToLocation(to: WrapLocation?) {
        val from = WrapLocation(WrapLocation.WrapType.GPS)
        IntentUtils.findDirections(application, from, null, to, true, true)
    }

    fun findDirectionsFromLocationViaLocationToLocation(from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, search: Boolean = true) {
        IntentUtils.findDirections(application, from, via, to, search, true)
    }

    fun findDeparturesOfLocation(of: WrapLocation) {
        IntentUtils.findDepartures(application, of)
    }

    fun presetDirectionsFromLocationViaLocationToLocation(from: WrapLocation?, via: WrapLocation?, to: WrapLocation?) {
        presetDirections(application, from, via, to, true)
    }

    fun setTransportNetwork(it: TransportNetwork) {
        transportNetworkManager.setTransportNetwork(it)
    }

}

sealed class NearbyLocationsState {
    object Initial : NearbyLocationsState()
    object Loading : NearbyLocationsState()
    data class Success(val result: NearbyLocationsResult) : NearbyLocationsState()
    data class Error(val message: String) : NearbyLocationsState()
}

sealed class BottomSheetContentState {
    object Initial : BottomSheetContentState()
    object Empty : BottomSheetContentState()
    object Loading : BottomSheetContentState()
    data class Location(val loc: WrapLocation?, val lines: List<KLine>?) : BottomSheetContentState()
    object SavedSearches : BottomSheetContentState()
}