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
package de.libf.transportrng.ui.directions

import androidx.lifecycle.viewModelScope
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.ui.trips.TripQuery
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.gps.ReverseGeocoderV2
import de.libf.transportrng.data.locations.FavoriteLocation.FavLocationType
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrng.data.trips.TripsRepository

import de.libf.ptek.NetworkProvider
import de.libf.ptek.dto.Departure
import de.libf.ptek.dto.QueryDeparturesResult
import de.libf.ptek.dto.StationDepartures
import de.libf.transportrngocations.CombinedSuggestionRepository
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.departures.DeparturesState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.trip_error_service_down
import transportr_ng.composeapp.generated.resources.trip_error_unresolvable_address

private data class DirectionsUserInput(
    val from: WrapLocation?,
    val via: WrapLocation?,
    val to: WrapLocation?,
    val time: Long,
    val timeIsDeparture: Boolean,
    val products: Set<Product>
)

@OptIn(ExperimentalCoroutinesApi::class)
class DirectionsViewModel internal constructor(
    transportNetworkManager: TransportNetworkManager,
    private val  settingsManager: SettingsManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
    private val combinedSuggestionRepository: CombinedSuggestionRepository,
    private val tripsRepository: TripsRepository,
    private val gpsRepository: GpsRepository,
    private val geocoder: ReverseGeocoderV2
) : SavedSearchesViewModel(transportNetworkManager, locationRepository, searchesRepository) {
    private val _viewState: MutableStateFlow<DirectionsState> = MutableStateFlow(DirectionsState.Loading)
    val viewState = _viewState.asStateFlow()

    val queryMoreState = tripsRepository.queryMoreState
    val trips: Flow<Set<Trip>> = tripsRepository.trips
    val queryError = tripsRepository.queryError
    val queryPTEError = tripsRepository.queryPTEError
    val queryMoreError = tripsRepository.queryMoreError

    private val _fromLocation = MutableStateFlow<WrapLocation?>(null)
    private val _viaLocation = MutableStateFlow<WrapLocation?>(null)
    val viaSupported: Flow<Boolean> = transportNetwork.mapLatest {
        it?.networkProvider?.hasCapabilities(NetworkProvider.Capability.TRIPS_VIA) ?: false
    }
    private val _toLocation = MutableStateFlow<WrapLocation?>(null)


    private val gpsLocationFor = MutableStateFlow<FavLocationType?>(null)
//    val findGpsLocation = MutableLiveData<FavLocationType?>()
//    val timeUpdate = LiveTrigger()

    private val _now = MutableStateFlow(true)

    private val _calendar = MutableStateFlow(Clock.System.now())
    val lastQueryCalendar: StateFlow<Instant?> = _calendar.asStateFlow()

    private val _products = MutableStateFlow(settingsManager.getPreferredProducts())
    val products: StateFlow<Set<Product>> = _products.asStateFlow()

    private val _isDeparture = MutableStateFlow(true)
    val isDeparture: StateFlow<Boolean> = _isDeparture.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded

    private val _displayTrips = MutableStateFlow(false)
    val displayTrips: Flow<Boolean> = _displayTrips.asStateFlow()

    val topSwipeEnabled = MutableStateFlow(false)

    val fromLocation = _fromLocation.asStateFlow()
    val viaLocation = _viaLocation.asStateFlow()
    val toLocation = _toLocation.asStateFlow()

    val locationSuggestions: Flow<Set<WrapLocation>> = combinedSuggestionRepository.suggestions
    val suggestionsLoading: Flow<Boolean> = combinedSuggestionRepository.isLoading

    private var gpsJob: Job? = null
    private val currentLocation = gpsRepository.getLocationFlow()
    private val _gpsLoading = MutableStateFlow(false)
    val gpsLoading = _gpsLoading.asStateFlow()

    private val _departures = MutableStateFlow<Result<List<StationDepartures>>?>(null)

    init {
        val savedLocs = combine(favoriteTrips, specialLocations) { f, s -> listOf(f, s).flatten() }

        combine(
            savedLocs,
            _departures,
            trips,
            fromLocation,
            toLocation
        ) { saved, deps, trips, from, to ->
            if (from == null && to == null)
                DirectionsState.ShowFavorites(saved)
            else if(from != null && to != null)
                DirectionsState.ShowTrips(trips)
            else if (((from != null && from.hasId()) || (to != null && to.hasId())) && deps != null)
                deps.getOrNull()?.let { dep ->
                    DirectionsState.ShowDepartures(
                        dep.flatMap { it.departures },
                        dep.flatMap { it.lines }
                    )
                } ?: DirectionsState.Error(
                    message = deps.exceptionOrNull()?.message ?: "unknown error"
                )
            else null
        }
            .distinctUntilChanged()
            .onEach { it?.let { _viewState.value = it } }
            .launchIn(viewModelScope)

        val locations = combine(fromLocation, viaLocation, toLocation) { from, via, to -> Triple(from, via, to) }

        combine(locations, _calendar.asStateFlow(), isDeparture, products) {
                locs, time, timeIsDep, prods ->
            DirectionsUserInput(
                locs.first, locs.second, locs.third, time.toEpochMilliseconds(), timeIsDep, prods
            )
        }
            .debounce(300)
            .distinctUntilChanged()
            .onEach {
                _viewState.value = DirectionsState.Loading

                if(it.to != null && it.from != null) {
                    val tripQuery = TripQuery(it.from, it.via, it.to, it.time, it.timeIsDeparture, it.products)
                    tripsRepository.search(tripQuery, viewModelScope)
                } else {
                    val candidates = listOfNotNull(it.from, it.to, it.via).filter { it.id != null }

                    if(candidates.isEmpty()) return@onEach

                    val station = candidates.first()
                    transportNetwork.value?.networkProvider?.queryDepartures(
                        stationId = station.id!!,
                        time = Clock.System.now().toEpochMilliseconds(),
                        maxDepartures = 12,
                        equivs = false
                    )?.let {
                        when(it.status) {
                            QueryDeparturesResult.Status.OK ->
                                _departures.value = Result.success(it.stationDepartures)

                            QueryDeparturesResult.Status.SERVICE_DOWN ->
                                _departures.value = Result.failure(Exception(
                                    getString(Res.string.trip_error_service_down)
                                ))

                            QueryDeparturesResult.Status.INVALID_STATION ->
                                _departures.value = Result.failure(Exception(
                                    getString(Res.string.trip_error_unresolvable_address)
                                ))
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    suspend fun fetchLocationFromGps() {
        _gpsLoading.value = true

        currentLocation.first().let { loc ->
            loc.onSuccess {
                geocoder.findLocation(it)
                    .onSuccess { location ->
                        when(gpsLocationFor.value) {
                            FavLocationType.FROM -> _fromLocation.value = location
                            FavLocationType.VIA -> _viaLocation.value = location
                            FavLocationType.TO -> _toLocation.value = location
                            else -> {}
                        }
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
            gpsLocationFor.value = FavLocationType.FROM
            gpsJob = viewModelScope.launch {
                fetchLocationFromGps()
            }
        } else if(gpsLocationFor.value == FavLocationType.FROM) {
            gpsLocationFor.value = null
        }

//        maybeSearch()
    }

    fun setViaLocation(location: WrapLocation?) {
        _viaLocation.value = location
    }

    fun setToLocation(location: WrapLocation?) {
        _toLocation.value = location
    }
//
//    fun maybeSearch() {
//        if (_fromLocation.value != null && _toLocation.value != null) {
//            search()
//        }
//    }

    fun swapFromAndToLocations() {
        val tmp = _toLocation.value
        if (_fromLocation.value?.wrapType == WrapLocation.WrapType.GPS) {
            gpsLocationFor.value = null
            // TODO: GPS currently only supports from location, so don't swap it for now
            _toLocation.value = null
        } else {
            _toLocation.value = _fromLocation.value
        }
        _fromLocation.value = tmp
    }



//    override fun onTimeAndDateSet(calendar: Calendar) {
//        setCalendar(calendar)
//        search()
//    }
//
//    override fun onDepartureOrArrivalSet(departure: Boolean) {
//        setIsDeparture(departure)
//        search()
//    }

    fun resetCalender() {
        _now.value = true
        _calendar.value = Clock.System.now()
//        search()
    }

//    private fun setCalendar(calendar: Calendar) {
//        _calendar.value = calendar
//        _now.value = DateUtils.isNow(calendar)
//    }



    fun setProducts(newProducts: Set<Product>) {
        _products.value = newProducts
        settingsManager.setPreferredProducts(newProducts)
    }



    fun setIsDeparture(departure: Boolean) {
        _isDeparture.value = departure
    }



    fun setIsExpanded(expanded: Boolean) {
        _isExpanded.value = expanded
    }

    fun toggleIsExpanded() {
        _isExpanded.value = !_isExpanded.value!!
    }

    val isFavTrip = tripsRepository.isFavTrip

    fun toggleFavTrip() {
        viewModelScope.launch {
            tripsRepository.toggleFavState()
        }
    }

//    override fun onLocationItemClick(loc: WrapLocation, type: FavLocationType) {
//        when (type) {
//            FavLocationType.FROM -> setFromLocation(loc)
//            FavLocationType.VIA -> setViaLocation(loc)
//            FavLocationType.TO -> setToLocation(loc)
//        }
//        search()
//        // clear finding GPS location request
//        if (gpsLocationFor.value == type) gpsLocationFor.value
//    }

//    override fun onLocationCleared(type: FavLocationType) {
//        when (type) {
//            FavLocationType.FROM -> setFromLocation(null)
//            FavLocationType.VIA -> {
//                setViaLocation(null)
//                search()
//            }
//            FavLocationType.TO -> setToLocation(null)
//        }
//        // clear finding GPS location request
//        if (gpsLocationFor.value == type) gpsLocationFor.value = null
//    }

    /* Trip Queries */
    fun searchTrips() {
        viewModelScope.launch {
            val from = _fromLocation.value; val to = _toLocation.value
            val via = if (_isExpanded.value != null && _isExpanded.value!!)
                _viaLocation.value else null
            val calendar = if (_now.value != null && _now.value)
                Clock.System.now() else _calendar.value
            if (from == null || to == null || calendar == null) return@launch
            _calendar.value = calendar



            _displayTrips.emit(true)
        }
    }

    fun searchMore(later: Boolean) {
        viewModelScope.launch {
            tripsRepository.searchMore(later)
        }
    }

    fun cancelGps() {
        gpsJob?.cancel()
        _gpsLoading.value = false
    }

    fun reload() {
        TODO("Not yet implemented")
    }
}
