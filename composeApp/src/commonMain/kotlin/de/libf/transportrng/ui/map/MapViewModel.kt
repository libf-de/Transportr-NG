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

package de.libf.transportrng.ui.map

import androidx.lifecycle.viewModelScope
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.libf.transportrng.data.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.NearbyLocationsResult
import de.libf.ptek.dto.QueryDeparturesResult
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrng.ui.departures.MAX_DEPARTURES
import de.libf.transportrngocations.CombinedSuggestionRepository
import de.libf.transportrng.data.locations.WrapLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class MapViewModel internal constructor(
    private val transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
    override val gpsRepository: GpsRepository,
    private val combinedSuggestionRepository: CombinedSuggestionRepository,
    ) : SavedSearchesViewModel(transportNetworkManager, locationRepository, searchesRepository), GpsMapViewModel by GpsMapViewModelImpl(gpsRepository) {

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

//    val mapClicked = SingleLiveEvent<Void>()
//    val markerClicked = SingleLiveEvent<Void>()
    val liveBounds: Flow<LatLngBounds?> = locations.map { input ->
        val points = input
            .filter { it.hasLocation() }
            .map { it.latLng }
            .toMutableSet()
        home.lastOrNull()?.let { if (it.hasLocation()) points.add(it.latLng) }
        work.lastOrNull()?.let { if (it.hasLocation()) points.add(it.latLng) }

        gpsRepository.getLocationFlow()
            .filter { it.isSuccess }
            .map { it.getOrNull()!! }
            .lastOrNull()
            ?.let { points.add(LatLng(it.lat, it.lon)) }

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
                                    Clock.System.now().toEpochMilliseconds(),
                                    MAX_DEPARTURES,
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
                        setOf(Location.Type.STATION),
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