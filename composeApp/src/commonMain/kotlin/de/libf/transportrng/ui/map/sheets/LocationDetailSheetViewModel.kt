package de.libf.transportrng.ui.map.sheets

import androidx.lifecycle.viewModelScope
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.QueryDeparturesResult
import de.libf.ptek.dto.StationDepartures
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrng.ui.departures.MAX_DEPARTURES
import de.libf.transportrng.ui.map.BottomSheetContentState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.drawer_departures
import transportr_ng.composeapp.generated.resources.query_failed_generic
import transportr_ng.composeapp.generated.resources.trip_error_service_down
import transportr_ng.composeapp.generated.resources.trip_error_unresolvable_address

class LocationDetailSheetViewModel internal constructor(
    transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
) : SavedSearchesViewModel(transportNetworkManager, locationRepository, searchesRepository) {

    private val _uiState: MutableStateFlow<LocationDetailSheetState> = MutableStateFlow(LocationDetailSheetState.Loading)
    val uiState = _uiState.asStateFlow()

    private var selectedLocationJob: Job? = null

    fun load(location: WrapLocation) {
        selectedLocationJob?.cancel()

        if(location.hasId())
            _uiState.value = LocationDetailSheetState.DeparturesLoading(location)
        else {
            _uiState.value = LocationDetailSheetState.LocationWithoutId(location)
            return
        }

        selectedLocationJob = viewModelScope.launch {
            try {
                transportNetwork
                    .value
                    ?.networkProvider
                    ?.queryDepartures(location.id!!,
                        Clock.System.now().toEpochMilliseconds(),
                        MAX_DEPARTURES,
                        false)
                    ?.let {
                        _uiState.value = when(it.status) {
                            QueryDeparturesResult.Status.OK -> LocationDetailSheetState.Success(
                                location,
                                it.stationDepartures
                            )
                            QueryDeparturesResult.Status.SERVICE_DOWN ->
                                LocationDetailSheetState.Error(
                                    location,
                                    getString(Res.string.trip_error_service_down)
                                )
                            QueryDeparturesResult.Status.INVALID_STATION ->
                                LocationDetailSheetState.Error(
                                    location,
                                    getString(Res.string.trip_error_unresolvable_address)
                                )
                        }
                    } ?: run {
                        _uiState.value = LocationDetailSheetState.Error(
                            location,
                            getString(Res.string.query_failed_generic, getString(Res.string.drawer_departures), "queryDepartures returned null")
                        )
                    }
            } catch(e: Exception) {
                _uiState.value = LocationDetailSheetState.Error(
                    location,
                    getString(Res.string.query_failed_generic, getString(Res.string.drawer_departures), e.message ?: "")
                )
            }
        }
    }

}

sealed class LocationDetailSheetState {
    data object Loading : LocationDetailSheetState()
    data class Error(
        val location: WrapLocation,
        val message: String
    ) : LocationDetailSheetState()

    data class LocationWithoutId(val location: WrapLocation) : LocationDetailSheetState()
    data class DeparturesLoading(val location: WrapLocation) : LocationDetailSheetState()
    data class Success(
        val location: WrapLocation,
        val departures: List<StationDepartures>
    ) : LocationDetailSheetState()
}