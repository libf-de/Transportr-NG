package de.libf.transportrng.ui.departures

import de.libf.ptek.dto.Departure
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.LineDestination
import de.libf.ptek.dto.QueryDeparturesResult
import de.libf.ptek.dto.StationDepartures
import de.libf.transportrng.Routes

//sealed class NearbyLocationsState {
//    object Initial : NearbyLocationsState()
//    object Loading : NearbyLocationsState()
//    data class Success(val result: NearbyLocationsResult) : NearbyLocationsState()
//    data class Error(val message: String) : NearbyLocationsState()
//}

sealed class DeparturesState {
    object Initial : DeparturesState()
    object Loading : DeparturesState()
    data class Success(val departures: List<Departure>, val lines: List<LineDestination>) : DeparturesState()
    data class Error(val message: String) : DeparturesState()
}