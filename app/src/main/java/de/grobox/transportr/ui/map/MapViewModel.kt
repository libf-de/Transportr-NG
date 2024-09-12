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
import androidx.lifecycle.viewModelScope
import de.grobox.transportr.TransportrApplication
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
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.NearbyLocationsResult
import de.schildbach.pte.dto.QueryDeparturesResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
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

//    private val peekHeight = MutableStateFlow<Int>()
    private val selectedLocationClicked = MutableStateFlow<LatLng?>(null)
    private val updatedLiveBounds = MutableStateFlow<LatLngBounds?>(null)

    private var selectedLocationJob: Job? = null
    private val _selectedLocation = MutableStateFlow<WrapLocation?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()
    private val _findNearbyStations = MutableSharedFlow<WrapLocation>(extraBufferCapacity = 1)
    val findNearbyStations = _findNearbyStations.asSharedFlow()
    private val _nearbyStationsFound = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val nearbyStationsFound = _nearbyStationsFound.asSharedFlow()

    private var nearbyStationsJob: Job? = null
    private val _nearbyStationsState = MutableStateFlow<NearbyLocationsState?>(null)
    val nearbyStations = _nearbyStationsState.asStateFlow()

    val mapClicked = SingleLiveEvent<Void>()
    val markerClicked = SingleLiveEvent<Void>()
    val liveBounds: Flow<LatLngBounds?> = locations.map { input ->
        val points = input
            .filter { it.hasLocation() }
            .map { it.latLng }
            .toMutableSet()
        home.lastOrNull()?.let { if (it.hasLocation()) points.add(it.latLng) }
        work.lastOrNull()?.let { if (it.hasLocation()) points.add(it.latLng) }
        positionController.position.lastOrNull()?.let { points.add(LatLng(it)) }

        if (points.size < 2) {
            null
        } else {
            LatLngBounds.Builder().includes(ArrayList(points)).build()
        }
    }
    var transportNetworkWasChanged = false

    val locationSuggestions = combinedSuggestionRepository.suggestions
    val suggestionsLoading = combinedSuggestionRepository.isLoading

    private val _sheetContentState = MutableStateFlow<BottomSheetContentState>(BottomSheetContentState.SavedSearches)
    val sheetContentState: StateFlow<BottomSheetContentState> = _sheetContentState.asStateFlow()

    fun suggestLocations(query: String) {
        combinedSuggestionRepository.updateSuggestions(query)
    }

    fun cancelSuggestLocations() {
        combinedSuggestionRepository.cancelSuggestions()
    }

    fun resetSuggestions() {
        combinedSuggestionRepository.reset()
    }

    fun selectedLocationClicked(latLng: LatLng) {
        selectedLocationClicked.value = latLng
        // reset the selected location right away, observers will ignore this update
        selectedLocationClicked.value = null
    }

    fun selectLocation(location: WrapLocation?) {
        _selectedLocation.value = location

        selectedLocationJob?.cancel()

        if(location == null) return

        _sheetContentState.value = BottomSheetContentState.Location(location, emptyList())

        if(!location.hasId()) return

        selectedLocationJob = viewModelScope.launch {
            val deps = transportNetwork
                .lastOrNull()
                ?.networkProvider
                ?.queryDepartures(location.id!!,
                                    Date().time,
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
                lines = deps
            )
        }

        // do not reset the selected location right away, will break incoming geo intent
        // the observing fragment will call clearSelectedLocation() instead when it is done
    }

    fun clearSelectedLocation() {
        _selectedLocation.value = null

        _sheetContentState.value = BottomSheetContentState.Empty
    }

    fun findNearbyStations(location: WrapLocation) {
        //findNearbyStations.value = location

        if(nearbyStationsJob?.isActive == true)
            nearbyStationsJob?.cancel()


        nearbyStationsJob = viewModelScope.launch {
            _nearbyStationsState.value = NearbyLocationsState.Loading

            try {
                val result = withContext(Dispatchers.IO) {
                    transportNetwork.lastOrNull()?.networkProvider?.queryNearbyLocations(
                        EnumSet.of(Location.Type.STATION),
                        location.location,
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


    fun setNearbyStationsFound(found: Boolean) {
        viewModelScope.launch {
            _nearbyStationsFound.emit(found)
        }
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
    data class Location(val loc: WrapLocation?, val lines: List<Line>?) : BottomSheetContentState()
    object SavedSearches : BottomSheetContentState()
}