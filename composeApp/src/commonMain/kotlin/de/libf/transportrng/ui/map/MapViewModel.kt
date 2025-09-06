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
import de.libf.transportrng.data.favorites.FavoriteTripItem
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrngocations.CombinedSuggestionRepository
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.settings.SettingsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.reflect.KClass

class MapViewModel internal constructor(
    private val transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
    override val gpsRepository: GpsRepository,
    private val combinedSuggestionRepository: CombinedSuggestionRepository,
    private val settingsMgr: SettingsManager
) : SavedSearchesViewModel(transportNetworkManager, locationRepository, searchesRepository), GpsMapViewModel by GpsMapViewModelImpl(gpsRepository) {

    val lastMapCenter: Pair<LatLng, Double>?
        get() = settingsMgr.lastMapLocation

    fun storeMapCenter(latLng: LatLng, zoom: Double) {
        settingsMgr.lastMapLocation = Pair(latLng, zoom)
    }

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

    val locationSuggestions = combinedSuggestionRepository.suggestions
    val suggestionsLoading = combinedSuggestionRepository.isLoading

    private val _sheetContentTarget = MutableStateFlow<KClass<*>>(BottomSheetContentState.SavedSearches::class)
    private val _sheetContentState = MutableStateFlow<BottomSheetContentState>(BottomSheetContentState.Initial)
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
                                    24,
                                    false)
                ?.takeIf { it.status == QueryDeparturesResult.Status.OK }
                ?.let { dep ->
                    dep.stationDepartures
                        .flatMap { it.lines }
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
                val np = transportNetwork.value?.networkProvider

                val result = np?.queryNearbyLocations(
                    setOf(Location.Type.STATION),
                    location.location,
                    2000,
                    0
                ).also {
                    println(it)
                }

                println(result)

                result?.let {
                    _nearbyStationsState.value = when(it.status) {
                        NearbyLocationsResult.Status.OK -> NearbyLocationsState.Success(it.locations)
                        NearbyLocationsResult.Status.INVALID_ID -> NearbyLocationsState.InvalidId
                        NearbyLocationsResult.Status.SERVICE_DOWN -> NearbyLocationsState.ServiceDown
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
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
    object InvalidId : NearbyLocationsState()
    object ServiceDown : NearbyLocationsState()
    data class Success(val locations: List<Location>) : NearbyLocationsState()
    data class Error(val message: String) : NearbyLocationsState()
}

sealed class BottomSheetContentState {
    object Initial : BottomSheetContentState()
    object Empty : BottomSheetContentState()
    object Loading : BottomSheetContentState()
    data class Location(val loc: WrapLocation?, val lines: List<Line>?) : BottomSheetContentState()
    data class SavedSearches(
        val favorites: List<FavoriteTripItem>,
        val specials: List<FavoriteTripItem>
    ) : BottomSheetContentState()
}