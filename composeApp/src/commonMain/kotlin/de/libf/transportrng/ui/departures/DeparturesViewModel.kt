package de.libf.transportrng.ui.departures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.libf.ptek.dto.QueryDeparturesResult
import de.libf.transportrng.data.locations.WrapLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.trip_error_service_down
import transportr_ng.composeapp.generated.resources.trip_error_unresolvable_address

class DeparturesViewModel(
    transportManager: TransportNetworkManager
) : TransportNetworkViewModel(transportManager) {

    private val _departuresState: MutableStateFlow<DeparturesState> = MutableStateFlow(DeparturesState.Initial)
    val departuresState = _departuresState.asStateFlow()

    fun queryDepartures(station: WrapLocation, maxDepartures: Int = 12) {
        viewModelScope.launch {
            transportNetwork.value?.networkProvider?.queryDepartures(
                stationId = station.id!!,
                time = Clock.System.now().toEpochMilliseconds(),
                maxDepartures = maxDepartures,
                equivs = false
            )?.let {
                _departuresState.emit(when(it.status) {
                    QueryDeparturesResult.Status.OK ->
                        DeparturesState.Success(
                            departures = it.stationDepartures.flatMap { it.departures },
                            lines = it.stationDepartures.flatMap { it.lines }
                        )

                    QueryDeparturesResult.Status.SERVICE_DOWN ->
                        DeparturesState.Error(getString(Res.string.trip_error_service_down))

                    QueryDeparturesResult.Status.INVALID_STATION ->
                        DeparturesState.Error(getString(Res.string.trip_error_unresolvable_address))
                })
            } ?: _departuresState.emit(DeparturesState.Error("No transport network received :("))
        }
    }
}