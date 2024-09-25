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

package de.libf.transportrng.data.trips

import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.transportrng.data.settings.SettingsManager
import de.grobox.transportr.ui.trips.TripQuery
import de.libf.ptek.QueryTripsContext
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.QueryTripsResult
import de.libf.ptek.dto.QueryTripsResult.Status.AMBIGUOUS
import de.libf.ptek.dto.QueryTripsResult.Status.INVALID_DATE
import de.libf.ptek.dto.QueryTripsResult.Status.NO_TRIPS
import de.libf.ptek.dto.QueryTripsResult.Status.OK
import de.libf.ptek.dto.QueryTripsResult.Status.SERVICE_DOWN
import de.libf.ptek.dto.QueryTripsResult.Status.TOO_CLOSE
import de.libf.ptek.dto.QueryTripsResult.Status.UNKNOWN_FROM
import de.libf.ptek.dto.QueryTripsResult.Status.UNKNOWN_LOCATION
import de.libf.ptek.dto.QueryTripsResult.Status.UNKNOWN_TO
import de.libf.ptek.dto.QueryTripsResult.Status.UNKNOWN_VIA
import de.libf.ptek.dto.QueryTripsResult.Status.UNRESOLVABLE_ADDRESS
import de.libf.ptek.dto.Trip
import de.libf.ptek.dto.TripOptions
import de.libf.transportrng.data.locations.FavoriteLocation.FavLocationType.FROM
import de.libf.transportrng.data.locations.FavoriteLocation.FavLocationType.TO
import de.libf.transportrng.data.locations.FavoriteLocation.FavLocationType.VIA
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.searches.SearchesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.trip_error_ambiguous
import transportr_ng.composeapp.generated.resources.trip_error_invalid_date
import transportr_ng.composeapp.generated.resources.trip_error_no_trips
import transportr_ng.composeapp.generated.resources.trip_error_service_down
import transportr_ng.composeapp.generated.resources.trip_error_too_close
import transportr_ng.composeapp.generated.resources.trip_error_unknown_from
import transportr_ng.composeapp.generated.resources.trip_error_unknown_to
import transportr_ng.composeapp.generated.resources.trip_error_unknown_via
import transportr_ng.composeapp.generated.resources.trip_error_unresolvable_address
import kotlin.coroutines.cancellation.CancellationException

class TripsRepository(
//    private val ctx: Context,
//    private val networkProvider: NetworkProvider,
    private val networkManager: TransportNetworkManager,
    private val settingsManager: SettingsManager,
    private val locationRepository: LocationRepository,
    private val searchesRepository: SearchesRepository,
    private val tripsDao: TripsDao
) {

    companion object {
        private val TAG = TripsRepository::class.simpleName
    }

    enum class QueryMoreState { EARLIER, LATER, BOTH, NONE }

    //@OptIn(ExperimentalCoroutinesApi::class)
//    private val networkProvider: StateFlow<NetworkProvider> = networkManager.transportNetwork.mapLatest { it?.networkProvider }

//    val trips = MutableLiveData<Set<Trip>>()
    private val _trips: MutableStateFlow<Set<Trip>> = MutableStateFlow(emptySet())
    val trips = _trips.asStateFlow()
//    val queryMoreState = MutableLiveData<QueryMoreState>()
    private val _queryMoreState = MutableStateFlow(QueryMoreState.NONE)
    val queryMoreState = _queryMoreState.asStateFlow()
//    val queryError = SingleLiveEvent<String>()
    private val _queryError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val queryError = _queryError.asSharedFlow()
//    val queryPTEError = SingleLiveEvent<Pair<String, String>>()
    private val _queryPTEError = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val queryPTEError = _queryPTEError.asSharedFlow()
//    val queryMoreError = SingleLiveEvent<String>()
    private val _queryMoreError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val queryMoreError = _queryMoreError.asSharedFlow()
//    val isFavTrip = MutableLiveData<Boolean>()
    private val _isFavTrip = MutableStateFlow(false)
    val isFavTrip = _isFavTrip.asStateFlow()

    //private val networkProvider = MediatorLiveData<NetworkProvider>(getTransportNetwork(NetworkId.DB)!!.networkProvider)

    private var uid: Long = 0L
    private var queryTripsContext: QueryTripsContext? = null
//    private var queryTripsTask = Thread()

    private var queryTripsJob: Job? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            networkManager.transportNetwork.collect {
                println("New TransportNetwork: ${it?.id}")
            }
        }



//        var network = networkManager.transportNetwork.value
//        if (network == null) network = getTransportNetwork(NetworkId.DB)!!
//
//        networkProvider = network.networkProvider

        // Delete trips that arrived more than 2 days ago
        CoroutineScope(Dispatchers.IO).launch {
            tripsDao.deleteTripsArrivingBeforeAndCleanup(
                Clock.System.now().toEpochMilliseconds() - 1000 * 60 * 60 * 24 * 2
            )
        }
    }

    private fun clearState() {
        _trips.value = emptySet()
        _queryMoreState.value = QueryMoreState.NONE
        queryTripsContext = null
        _isFavTrip.value = false
        uid = 0L
    }

    suspend fun findTripById(id: String): Trip? {
        return _trips.value.firstOrNull { it.id == id } ?: tripsDao.getTripByIdWithLegs(id)?.toTrip()
    }

    fun search(query: TripQuery, scope: CoroutineScope) {
        // reset current data
        clearState()

        println("From: " + query.from.location)
        println("Via: " + (if (query.via == null) "null" else query.via.location))
        println("To: " + query.to.location)
        println("Date: " + query.date)
        println("Departure: " + query.departure)
        println("Products: " + query.products)
        println("Optimize for: " + settingsManager.optimize)
        println("Walk Speed: " + settingsManager.walkSpeed)

        queryTripsJob?.cancel()

        queryTripsJob = scope.launch {
            queryTrips(query)
        }
    }

//    @WorkerThread
    private fun queryTrips(query: TripQuery) {
         CoroutineScope(Dispatchers.IO).launch {
             try {
//            val queryTripsResult = networkProvider.last()!!.queryTrips(
                 val queryTripsResult = networkManager.transportNetwork.value!!.networkProvider.queryTrips(
                     query.from.location, if (query.via == null) null else query.via.location, query.to.location,
                     query.date, query.departure,
                     TripOptions(query.products.toSet(), settingsManager.optimize, settingsManager.walkSpeed, null, null)
                 )!!
                 if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                     // deliver result first, so UI can get updated
                     onQueryTripsResultReceived(queryTripsResult)
                     // store locations (needed for references in stored search)
                     val from = locationRepository.addFavoriteLocation(query.from, FROM)
                     val via = query.via?.let { locationRepository.addFavoriteLocation(it, VIA) }
                     val to = locationRepository.addFavoriteLocation(query.to, TO)
                     // store search query
                     uid = searchesRepository.storeSearch(from, via, to)
                     // set fav status
                     _isFavTrip.value = searchesRepository.isFavorite(uid)
                 } else {
                     PTEError(queryTripsResult.status.name, queryTripsResult.getError(), query)
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
                 if (e is CancellationException) {
                     // return, because this thread was interrupted
//                 } else if (!TransportrUtils.hasInternet(ctx)) {
//                     _queryError.emit(getString(R.string.error_no_internet))
//                 } else if (e is SocketTimeoutException) {
//                     _queryError.emit(getString(R.string.error_connection_failure))
                 } else {
//                val errorBuilder = StringBuilder("$e\n${e.stackTrace[0]}\n${e.stackTrace[1]}\n${e.stackTrace[2]}")
//                e.cause?.let { errorBuilder.append("\nCause: ${it.stackTrace[0]}\n${it.stackTrace[1]}\n${it.stackTrace[2]}") }
                     PTEError(e.toString(), "", query)
                 }
             }
         }
    }

    fun searchMore(later: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            if (queryTripsContext == null) throw IllegalStateException("No query context")

            println("QueryTripsContext: " + queryTripsContext!!.toString())
            println("Later: $later")

            if (later && !queryTripsContext!!.canQueryLater) throw IllegalStateException("Can not query later")
            if (!later && !queryTripsContext!!.canQueryEarlier) throw IllegalStateException("Can not query earlier")

            try {
                val queryTripsResult = networkManager.transportNetwork.value!!.networkProvider.queryMoreTrips(queryTripsContext!!, later)
                if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                    onQueryTripsResultReceived(queryTripsResult)
                } else {
                    _queryMoreError.emit(queryTripsResult.getError())
                }
            } catch (e: Exception) {
                _queryMoreError.emit(e.toString())
            }
        }
    }

    private fun onQueryTripsResultReceived(queryTripsResult: QueryTripsResult) {
        queryTripsContext = queryTripsResult.context
        _queryMoreState.value = getQueryMoreStateFromContext(queryTripsContext)

        // Providers like VVO may provide duplicate trips, so remove them
        // Consider trips with the same departure, arrival times and the same lines as duplicates
        val newTrips = (_trips.value + queryTripsResult.trips.toSet()).distinctBy {
            Pair(
                Pair(it.firstDepartureTime, it.lastArrivalTime),
                it.legs.map { if(it is PublicLeg) it.line.name else null }
            )
        }.toSet()

        // Store trips in the repository
        // TODO: Fetch trips from repository if they are already cached, or reconsider whether its
        // TODO :a good idea to store all trips in the database
        CoroutineScope(Dispatchers.IO).launch {
            queryTripsResult.trips.forEach { tripsDao.addTrip(it, networkManager.transportNetwork.value!!.networkProvider.id()!!) }
        }

        _trips.value = newTrips
    }

    private fun getQueryMoreStateFromContext(context: QueryTripsContext?): QueryMoreState = context?.let {
        return if (it.canQueryEarlier && it.canQueryLater) {
            QueryMoreState.BOTH
        } else if (it.canQueryEarlier) {
            QueryMoreState.EARLIER
        } else if (it.canQueryLater) {
            QueryMoreState.LATER
        } else {
            QueryMoreState.NONE
        }
    } ?: QueryMoreState.NONE

    private suspend fun QueryTripsResult.getError(): String = when (status) {
        AMBIGUOUS -> getString(Res.string.trip_error_ambiguous)
        TOO_CLOSE -> getString(Res.string.trip_error_too_close)
        UNKNOWN_FROM -> getString(Res.string.trip_error_unknown_from)
        UNKNOWN_VIA -> getString(Res.string.trip_error_unknown_via)
        UNKNOWN_TO -> getString(Res.string.trip_error_unknown_to)
        UNKNOWN_LOCATION -> getString(Res.string.trip_error_unknown_from)
        UNRESOLVABLE_ADDRESS -> getString(Res.string.trip_error_unresolvable_address)
        NO_TRIPS -> getString(Res.string.trip_error_no_trips)
        INVALID_DATE -> getString(Res.string.trip_error_invalid_date)
        SERVICE_DOWN -> getString(Res.string.trip_error_service_down)
        OK -> throw IllegalArgumentException()
        null -> throw IllegalStateException()
    }

    private suspend fun PTEError(errorShort: String, error: String, query: TripQuery) {
        val title = StringBuilder()
            .append(networkManager.transportNetwork.value!!.networkProvider.id()!!.name)
            .append(": ")
            .append(errorShort)
        val body = StringBuilder()
            .appendLine("### Query")
            .appendLine("- NetworkId: `${networkManager.transportNetwork.value!!.networkProvider.id()!!.name}`")
            .appendLine("- From: `${query.from.location}`")
            .appendLine("- Via: `${if (query.via == null) "null" else query.via.location}`")
            .appendLine("- To: `${query.to.location}`")
            .appendLine("- Date: `${query.date}`")
            .appendLine("- Departure: `${query.departure}`")
            .appendLine("- Products: `${query.products}`")
            .appendLine("- Optimize for: `${settingsManager.optimize}`")
            .appendLine("- Walk Speed: `${settingsManager.walkSpeed}`")
            .appendLine()
            .appendLine("### Error")
            .appendLine("```")
            .appendLine(error)
            .appendLine("```")
            .appendLine()
            .appendLine("### Additional information")
            .appendLine("[Please modify this part]")

//        val uri = Uri.Builder()
//            .scheme("https")
//            .authority("github.com")
//            .appendPath("schildbach")
//            .appendPath("public-transport-enabler")
//            .appendPath("issues")
//            .appendPath("new")
//            .appendQueryParameter("title", title.toString())
//            .appendQueryParameter("body", body.toString())

        println(error)

        _queryPTEError.emit(Pair(error, ""))
    }

    suspend fun toggleFavState() {
        val oldFavState = isFavTrip.value
        if (uid == 0L || oldFavState == null) throw IllegalStateException()
        searchesRepository.updateFavoriteState(uid, !oldFavState)
        _isFavTrip.value = !oldFavState
    }

}
