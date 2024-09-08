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

package de.grobox.transportr.ui.trips

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.R
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.dto.KTrip
import de.grobox.transportr.data.trips.TripsRepository
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.map.GpsMapViewModel
import de.grobox.transportr.ui.map.GpsMapViewModelImpl
import de.grobox.transportr.map.PositionController
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.ui.trips.detail.TripReloader
import de.grobox.transportr.utils.SingleLiveEvent
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.util.Date

class TripDetailViewModel internal constructor(
    application: TransportrApplication,
    transportNetworkManager: TransportNetworkManager,
    override val positionController: PositionController,
    private val settingsManager: SettingsManager,
    private val tripsRepository: TripsRepository
) : TransportNetworkViewModel(application, transportNetworkManager), GpsMapViewModel by GpsMapViewModelImpl(positionController)  {

    enum class SheetState {
        BOTTOM, MIDDLE, EXPANDED
    }

    private val trip = MutableLiveData<KTrip>()
    private val zoomLeg = SingleLiveEvent<LatLngBounds>()
    private val zoomLocation = SingleLiveEvent<LatLng>()

    val tripReloadError = SingleLiveEvent<String>()
    val sheetState = MutableLiveData<SheetState>()
    val isFreshStart = MutableLiveData<Boolean>()
    var from: WrapLocation? = null
    var via: WrapLocation? = null
    var to: WrapLocation? = null

    init {
        isFreshStart.value = true
    }

    fun showWhenLocked(): Boolean {
        return settingsManager.showWhenLocked()
    }

    fun getTripById(id: String) {
        trip.value = tripsRepository.findTripById(id)
    }

    fun getTrip(): LiveData<KTrip> {
        return trip
    }

    fun setTrip(trip: KTrip) {
        this.trip.value = trip
    }

    fun getZoomLocation(): LiveData<LatLng> {
        return zoomLocation
    }

    fun getZoomLeg(): LiveData<LatLngBounds> {
        return zoomLeg
    }

    fun reloadTrip() {
        val network = transportNetwork.value ?: throw IllegalStateException()

        val oldTrip = trip.value ?: throw IllegalStateException()

        if (from == null || to == null) throw IllegalStateException()

        val errorString = getApplication<Application>().getString(R.string.error_trip_refresh_failed)
        val query = TripQuery(from!!, via, to!!, oldTrip.firstDepartureTime!!.let(::Date), true, oldTrip.products)
        TripReloader(network.networkProvider, settingsManager, query, trip, errorString, tripReloadError)
                .reload()
    }
}
