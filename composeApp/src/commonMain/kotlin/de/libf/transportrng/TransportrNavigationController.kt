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

package de.libf.transportrng

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.bundle.Bundle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.favorites.FavoriteTripType
import de.libf.transportrng.ui.directions.DirectionsScreen
import de.libf.transportrng.ui.directions.DirectionsViewModel
import de.libf.transportrng.ui.map.MapScreen
import de.libf.transportrng.ui.settings.SettingsScreen
import de.libf.transportrng.ui.settings.SettingsViewModel
import de.libf.transportrng.ui.transportnetworkselector.TransportNetworkSelectorScreen
import de.libf.transportrng.ui.trips.TripDetailScreen
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.departures.DeparturesScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.reflect.typeOf

@OptIn(ExperimentalSerializationApi::class)
class ProtobufNavType<T>(
    private val serializer: KSerializer<T>
) : NavType<T>(isNullableAllowed = true) {
    override fun get(bundle: Bundle, key: String): T? {
        return bundle.getByteArray(key)?.let {
            ProtoBuf.decodeFromByteArray(serializer, it)
        }
    }

    override fun serializeAsValue(value: T): String {
        if(value == null) return ""
        return ProtoBuf.encodeToHexString(serializer, value)
    }

    override fun parseValue(value: String): T {
        return ProtoBuf.decodeFromHexString(serializer, value)
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putByteArray(key, ProtoBuf.encodeToByteArray(serializer, value))
    }
}

inline fun <reified T> protobufNavType(): NavType<T> {
    return ProtobufNavType(serializer())
}

@Serializable
sealed class MapRoutes {
    @Serializable
    data object SavedSearches : MapRoutes()

    @Serializable
    data class LocationDetail(val location: WrapLocation) : MapRoutes()
}

@Serializable
sealed class Routes {
    @Serializable
    data class Map(
        val geoUri: String? = null,
        val location: WrapLocation? = null
    )

    @Serializable
    data class Directions(
        val specialLocation: FavoriteTripType = FavoriteTripType.TRIP,
        val from: WrapLocation? = null,
        val via: WrapLocation? = null,
        val to: WrapLocation? = null,
        val time: Long = -1,
        val search: Boolean = true
    )


    @Serializable
    data class Departures(
        val location: WrapLocation
    )

    @Serializable
    object Settings

    @Serializable
    object TransportNetworkSelector

    @Serializable
    data class TripDetail(val tripId: String)
}

@Composable
fun TransportrNavigationController(
    navController: NavHostController
) {
    val netMgr = koinInject<TransportNetworkManager>()
    val hasTransportNetwork = remember {
        netMgr.transportNetwork.value != null
    }

    NavHost(
        navController = navController,
        startDestination = if(hasTransportNetwork) Routes.Map() else Routes.TransportNetworkSelector,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<Routes.Map>(
            typeMap = mapOf(typeOf<WrapLocation?>() to protobufNavType<WrapLocation?>())
        ) {
            val params: Routes.Map = it.toRoute()

            MapScreen(
                viewModel = koinViewModel(),
                navController = navController,
                geoUri = params.geoUri,
                location = params.location,
            )
        }


        composable<Routes.Directions>(
            typeMap = mapOf(
                typeOf<WrapLocation?>() to protobufNavType<WrapLocation?>(),
                typeOf<FavoriteTripType>() to protobufNavType<FavoriteTripType>())
        ) {
            val params: Routes.Directions = it.toRoute()

            DirectionsScreen(
                viewModel = koinViewModel<DirectionsViewModel>(),
                navController = navController,
                from = params.from,
                via = params.via,
                to = params.to,
                specialLocation = params.specialLocation,
                search = params.search,
                time = params.time.takeIf { it != -1L },
                tripClicked = { trip ->
                    navController.navigate(
                        Routes.TripDetail(
                            tripId = trip.id
                        )
                    )
                }
            )
        }

        composable<Routes.Settings> {
            SettingsScreen(
                onSelectNetworkClicked = {
                    navController.navigate(route = Routes.TransportNetworkSelector)
                },
                navController = navController
            )
        }

        composable<Routes.TripDetail>(
            typeMap = mapOf(typeOf<Trip>() to protobufNavType<Trip>())
        ) {
            val params = it.toRoute<Routes.TripDetail>()
            TripDetailScreen(
                viewModel = koinViewModel(),
                navController = navController,
                tripId = params.tripId,
                setBarColor = { _, _ -> }
            )
        }

        composable<Routes.TransportNetworkSelector> {
            TransportNetworkSelectorScreen(
                viewModel = koinViewModel(),
                navController = navController
            )
        }

        composable<Routes.Departures>(
            typeMap = mapOf(
                typeOf<WrapLocation>() to protobufNavType<WrapLocation>()
            )
        ) {
            val params = it.toRoute<Routes.Departures>()

            DeparturesScreen(
                navController = navController,
                location = params.location,
                viewModel = koinViewModel()
            )
        }
    }
}

fun String?.isRouteToShowWhenLocked(): Boolean {
    if(this == null) return false
    if(Routes.TripDetail::class.qualifiedName?.let { this.startsWith(it) } == true) return true
    return false
}