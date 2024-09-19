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
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.ui.trips.TripQuery
import de.grobox.transportr.ui.trips.detail.reload
import de.libf.ptek.dto.Trip
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
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.error_trip_refresh_failed

class TripDetailViewModel internal constructor(
    transportNetworkManager: TransportNetworkManager,
    override val gpsRepository: GpsRepository,
    private val settingsManager: SettingsManager,
    private val tripsRepository: TripsRepository
) : TransportNetworkViewModel(transportNetworkManager), GpsMapViewModel by GpsMapViewModelImpl(gpsRepository)  {

    enum class SheetState {
        BOTTOM, MIDDLE, EXPANDED
    }

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip = _trip.asStateFlow()

    private val _zoomLeg = MutableSharedFlow<LatLngBounds>(extraBufferCapacity = 1)
    val zoomLeg = _zoomLeg.asSharedFlow()
    private val _zoomLocation = MutableSharedFlow<LatLng>(extraBufferCapacity = 1)
    val zoomLocation = _zoomLocation.asSharedFlow()


    private val _tripReloadError = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val tripReloadError = _tripReloadError.asSharedFlow()
//    val tripReloadError = SingleLiveEvent<String>()
    val sheetState = MutableStateFlow<SheetState>(SheetState.MIDDLE)
    val isFreshStart = MutableStateFlow<Boolean>(true)
    var from: WrapLocation? = null
    var via: WrapLocation? = null
    var to: WrapLocation? = null

//    override fun onLocationClick(location: Location) {
//        if (!location.hasLocation()) return
//        val latLng = LatLng(location.latAsDouble, location.lonAsDouble)
//        zoomLocation.value = latLng
//        sheetState.value = MIDDLE
//    }

    fun showWhenLocked(): Boolean {
        return settingsManager.showWhenLocked()
    }

    fun getTripById(id: String) {
        viewModelScope.launch {
            _trip.value = tripsRepository.findTripById(id)
        }
    }

    fun setTrip(trip: Trip) {
        this._trip.value = trip
    }



    fun reloadTrip() {
        viewModelScope.launch {
            val network = transportNetwork.value ?: throw IllegalStateException()

            val oldTrip = _trip.value ?: throw IllegalStateException()

            if (from == null || to == null) throw IllegalStateException()

            val errorString = getString(Res.string.error_trip_refresh_failed)
            val query = TripQuery(from!!, via, to!!, oldTrip.firstDepartureTime!!, true, oldTrip.products)

            val reloadError = _trip.reload(
                networkProvider = network.networkProvider,
                settingsManager = settingsManager,
                query = query,
                errorString = errorString
            )

            _tripReloadError.emit(reloadError)
        }
    }
}


