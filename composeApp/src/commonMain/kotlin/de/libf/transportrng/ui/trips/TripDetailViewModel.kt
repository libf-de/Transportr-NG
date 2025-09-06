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

package de.libf.transportrng.ui.trips


import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.libf.transportrng.data.settings.SettingsManager
import de.grobox.transportr.ui.trips.TripQuery
import de.grobox.transportr.ui.trips.detail.reload
import de.grobox.transportr.ui.trips.detail.reloadTrip
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.PlatformTool
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import de.libf.transportrng.data.trips.TripsRepository
import de.libf.transportrng.ui.map.GpsMapViewModel
import de.libf.transportrng.ui.map.GpsMapViewModelImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.utils.toShareString
import nl.adaptivity.xmlutil.core.impl.multiplatform.assert
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.error_trip_refresh_failed

class TripDetailViewModel internal constructor(
    transportNetworkManager: TransportNetworkManager,
    override val gpsRepository: GpsRepository,
    private val settingsManager: SettingsManager,
    private val tripsRepository: TripsRepository,
    private val platformTool: PlatformTool
) : TransportNetworkViewModel(transportNetworkManager), GpsMapViewModel by GpsMapViewModelImpl(gpsRepository)  {

    enum class SheetState {
        BOTTOM, MIDDLE, EXPANDED
    }

    private val _uiState = MutableStateFlow<TripDetailState>(TripDetailState.Loading)
    val uiState = _uiState.asStateFlow()

//    private val _trip = MutableStateFlow<Trip?>(null)
//    val trip = _trip.asStateFlow()
//
//    private val _zoomLeg = MutableSharedFlow<LatLngBounds>(extraBufferCapacity = 1)
//    val zoomLeg = _zoomLeg.asSharedFlow()
//    private val _zoomLocation = MutableSharedFlow<LatLng>(extraBufferCapacity = 1)
//    val zoomLocation = _zoomLocation.asSharedFlow()

//    private val _tripReloadError = MutableSharedFlow<String?>(extraBufferCapacity = 1)
//    val tripReloadError = _tripReloadError.asSharedFlow()
    val isFreshStart = MutableStateFlow<Boolean>(true)
    var from: WrapLocation? = null
    var via: WrapLocation? = null
    var to: WrapLocation? = null

    fun shareTrip(trip: Trip) {
        viewModelScope.launch {
            platformTool.shareText(
                trip.toShareString()
            )
        }
    }

    fun addTripToCalendar(trip: Trip, callback: (String?) -> Unit) {
        viewModelScope.launch {
            callback(platformTool.addToCalendar(trip))
        }
    }

    fun setZoomLeg(leg: Leg) {
        if(leg.path.size < 2) return

        viewModelScope.launch {
            val latLngs = leg.path.map { LatLng(it.lat, it.lon) }

            _uiState.value.getTripOrNull()?.let { trip ->
                _uiState.value = TripDetailState.DisplayingLeg(
                    trip,
                    LatLngBounds.Builder().includes(latLngs).build()
                )
            }
        }

    }

//    override fun onLocationClick(location: Location) {
//        if (!location.hasLocation()) return
//        val latLng = LatLng(location.latAsDouble, location.lonAsDouble)
//        zoomLocation.value = latLng
//        sheetState.value = MIDDLE
//    }

    fun showWhenLocked(): Boolean {
        return settingsManager.showWhenLocked()
    }

    fun getTripById(id: String, tripQuery: TripQuery) {
        viewModelScope.launch {
            tripsRepository.findTripById(id)?.also {
                from = WrapLocation(it.from)
                it.via?.let { via = WrapLocation(it) }
                to = WrapLocation(it.to)
            }?.let { _uiState.value = TripDetailState.DisplayingWholeTrip(it) }
            ?: run { reloadTrip(tripQuery) }
        }
    }

    fun reloadTrip(tripQuery: TripQuery) {
        viewModelScope.launch {
            transportNetwork.value?.let { network ->
                val errorString = getString(Res.string.error_trip_refresh_failed)

                _uiState.value
                    .getTripOrNull()
                    .reloadTrip(
                        networkProvider = network.networkProvider,
                        settingsManager = settingsManager,
                        query = tripQuery,
                        errorString = errorString
                    ).onFailure { error ->
                        _uiState.value = _uiState.value.getReloadFailedState(error)
                    }.onSuccess {
                        _uiState.value = _uiState.value.updateTrip(it)
                    }
            } ?: run {
                _uiState.value = _uiState.value.getReloadFailedState(
                    Exception("Transport network is null")
                )
            }
        }
    }

    fun showOnExternalMap(loc: WrapLocation) {
        platformTool.showLocationOnMap(loc)
    }
}


