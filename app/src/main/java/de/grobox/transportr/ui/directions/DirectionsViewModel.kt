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
package de.grobox.transportr.ui.directions

import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KTrip
import de.grobox.transportr.data.gps.GpsRepository
import de.grobox.transportr.data.gps.ReverseGeocoderV2
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.data.trips.TripsRepository
import de.grobox.transportr.data.trips.TripsRepository.QueryMoreState
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.locations.CombinedSuggestionRepository
import de.grobox.transportr.locations.LocationView.LocationViewListener
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.PositionController
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.getTransportNetwork
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.ui.TimeDateFragment.TimeDateListener
import de.grobox.transportr.ui.trips.TripQuery
import de.grobox.transportr.utils.DateUtils
import de.grobox.transportr.utils.LiveTrigger
import de.grobox.transportr.utils.SingleLiveEvent
import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.EnumSet

class DirectionsViewModel internal constructor(
    application: TransportrApplication,
    transportNetworkManager: TransportNetworkManager,
    private val  settingsManager: SettingsManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
    positionController: PositionController,
    private val combinedSuggestionRepository: CombinedSuggestionRepository,
    private val tripsRepository: TripsRepository,
    private val gpsRepository: GpsRepository,
    private val geocoder: ReverseGeocoderV2
) : SavedSearchesViewModel(application, transportNetworkManager, locationRepository, searchesRepository), TimeDateListener, LocationViewListener {

    private val _fromLocation = MutableLiveData<WrapLocation?>()
    private val _viaLocation = MutableLiveData<WrapLocation?>()
    val viaSupported: LiveData<Boolean>
    private val _toLocation = MutableLiveData<WrapLocation?>()
    val locationLiveData = positionController.positionName
    val findGpsLocation = MutableLiveData<FavLocationType?>()
    val timeUpdate = LiveTrigger()
    private val _now = MutableLiveData(true)
    private val _calendar = MutableLiveData(Calendar.getInstance())
    private val _products = MutableLiveData<EnumSet<KProduct>>(EnumSet.copyOf(settingsManager.getPreferredProducts()))
    private val _isDeparture = MutableLiveData(true)
    private val _isExpanded = MutableLiveData(false)

    private val _displayTrips = MutableLiveData(false)
    val displayTrips: LiveData<Boolean> = _displayTrips

    @Deprecated("use displayTrips")
    val showTrips = SingleLiveEvent<Void>()
    val topSwipeEnabled = MutableLiveData(false)

    val fromLocation: LiveData<WrapLocation?> = _fromLocation
    val viaLocation: LiveData<WrapLocation?> = _viaLocation
    val toLocation: LiveData<WrapLocation?> = _toLocation

    val locationSuggestions: LiveData<Set<WrapLocation>> = combinedSuggestionRepository.suggestions
    val suggestionsLoading: LiveData<Boolean> = combinedSuggestionRepository.isLoading

    private var gpsJob: Job? = null
    private val currentLocation = gpsRepository.getLocationFlow()
    private val _gpsLoading = MutableStateFlow(false)
    val gpsLoading = _gpsLoading.asStateFlow()

    suspend fun fetchFromLocationFromGps() {
        _gpsLoading.value = true

        currentLocation.first().let { loc ->
            loc.onSuccess {
                geocoder.findLocation(it)
                    .onSuccess { location ->
                        _fromLocation.value = location
                        _gpsLoading.value = false
                    }
                    .onFailure { fail ->
                        _gpsLoading.value = false
                        println("Failed to find location: ${fail.message}")
                    }
            }

            loc.onFailure {
                _gpsLoading.value = false
                println("Failed to get location: ${it.message}")
            }

        }
    }

    override fun onCleared() {
        cancelGps()
        super.onCleared()
    }

    fun suggestLocations(query: String) {
        combinedSuggestionRepository.updateSuggestions(query)
    }

    fun cancelSuggestLocations() {
        combinedSuggestionRepository.cancelSuggestions()
    }

    fun resetSuggestions() {
        combinedSuggestionRepository.reset()
    }

    fun setFromLocation(location: WrapLocation?) {
        _fromLocation.value = location

        if(location?.wrapType == WrapLocation.WrapType.GPS) {
            gpsJob = viewModelScope.launch {
                fetchFromLocationFromGps()
            }
        }


        maybeSearch()
    }

    fun setViaLocation(location: WrapLocation?) {
        _viaLocation.value = location
        maybeSearch()
    }

    fun setToLocation(location: WrapLocation?) {
        _toLocation.value = location
        maybeSearch()
    }

    fun maybeSearch() {
        if (_fromLocation.value != null && _toLocation.value != null) {
            search()
        }
    }

    fun swapFromAndToLocations() {
        val tmp = _toLocation.value
        if (_fromLocation.value?.wrapType == WrapLocation.WrapType.GPS) {
            findGpsLocation.value = null
            // TODO: GPS currently only supports from location, so don't swap it for now
            _toLocation.value = null
        } else {
            _toLocation.value = _fromLocation.value
        }
        _fromLocation.value = tmp
    }

    val lastQueryCalendar: LiveData<Calendar?> = _calendar

    override fun onTimeAndDateSet(calendar: Calendar) {
        setCalendar(calendar)
        search()
    }

    override fun onDepartureOrArrivalSet(departure: Boolean) {
        setIsDeparture(departure)
        search()
    }

    fun resetCalender() {
        _now.value = true
        search()
    }

    private fun setCalendar(calendar: Calendar) {
        _calendar.value = calendar
        _now.value = DateUtils.isNow(calendar)
    }

    val products: LiveData<EnumSet<KProduct>> = _products

    fun setProducts(newProducts: EnumSet<KProduct>) {
        _products.value = newProducts
        search()
        settingsManager.setPreferredProducts(newProducts)
    }

    val isDeparture: LiveData<Boolean> = _isDeparture

    fun setIsDeparture(departure: Boolean) {
        _isDeparture.value = departure
        search()
    }

    val isExpanded: LiveData<Boolean> = _isExpanded

    fun setIsExpanded(expanded: Boolean) {
        _isExpanded.value = expanded
    }

    fun toggleIsExpanded() {
        _isExpanded.value = !_isExpanded.value!!
    }

    val isFavTrip: MutableLiveData<Boolean>
        get() = tripsRepository.isFavTrip

    fun toggleFavTrip() {
        tripsRepository.toggleFavState()
    }

    override fun onLocationItemClick(loc: WrapLocation, type: FavLocationType) {
        when (type) {
            FavLocationType.FROM -> setFromLocation(loc)
            FavLocationType.VIA -> setViaLocation(loc)
            FavLocationType.TO -> setToLocation(loc)
        }
        search()
        // clear finding GPS location request
        if (findGpsLocation.value == type) findGpsLocation.value = null
    }

    override fun onLocationCleared(type: FavLocationType) {
        when (type) {
            FavLocationType.FROM -> setFromLocation(null)
            FavLocationType.VIA -> {
                setViaLocation(null)
                search()
            }
            FavLocationType.TO -> setToLocation(null)
        }
        // clear finding GPS location request
        if (findGpsLocation.value == type) findGpsLocation.value = null
    }

    /* Trip Queries */
    fun search() {
        val from = _fromLocation.value; val to = _toLocation.value
        val via = if (_isExpanded.value != null && _isExpanded.value!!)
            _viaLocation.value else null
        val calendar = if (_now.value != null && _now.value!!)
            Calendar.getInstance() else _calendar.value
        if (from == null || to == null || calendar == null) return
        _calendar.value = calendar

        val tripQuery = TripQuery(from, via, to, calendar.time, _isDeparture.value, _products.value)
        tripsRepository.search(tripQuery)

        _displayTrips.postValue(true)
        showTrips.call()
    }

    fun searchMore(later: Boolean) {
        tripsRepository.searchMore(later)
    }

    fun cancelGps() {
        gpsJob?.cancel()
        _gpsLoading.value = false
    }

    val queryMoreState: LiveData<QueryMoreState>
        get() = tripsRepository.queryMoreState
    val trips: LiveData<Set<KTrip>>
        get() = tripsRepository.trips
    val queryError: LiveData<String>
        get() = tripsRepository.queryError
    val queryPTEError: LiveData<Pair<String, String>>
        get() = tripsRepository.queryPTEError
    val queryMoreError: LiveData<String>
        get() = tripsRepository.queryMoreError

    init {
        var network = transportNetwork.value
        if (network == null) network = getTransportNetwork(NetworkId.DB)!!
        viaSupported = MutableLiveData(network.networkProvider.hasCapabilities(NetworkProvider.Capability.TRIPS_VIA))
    }
}
