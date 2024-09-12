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

package de.grobox.transportr

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.grobox.transportr.favorites.trips.FavoriteTripType
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.directions.DirectionsScreen
import de.grobox.transportr.ui.map.MapScreen
import de.grobox.transportr.ui.settings.SettingsScreen
import de.grobox.transportr.ui.settings.SettingsViewModel
import de.grobox.transportr.ui.transportnetworkselector.TransportNetworkSelectorScreen
import de.grobox.transportr.ui.trips.TripDetailScreen
import de.libf.ptek.dto.Trip
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.androidx.compose.koinViewModel
import kotlin.reflect.typeOf

val WrapLocationNavType = object : NavType<WrapLocation?>(
    isNullableAllowed = true
) {
    override fun get(bundle: Bundle, key: String): WrapLocation? {
        return Json.decodeFromString(bundle.getString(key) ?: "")
    }

    override fun parseValue(value: String): WrapLocation? {
        return Json.decodeFromString(value)
    }

    override fun serializeAsValue(value: WrapLocation?): String {
        return Json.encodeToString(value)
    }

    override fun put(bundle: Bundle, key: String, value: WrapLocation?) {
        bundle.putString(key, Json.encodeToString(value))
    }
}

class SerializableNavType<T>(
    private val serializer: KSerializer<T>
) : NavType<T>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): T? {
        return bundle.getString(key)?.let { Json.decodeFromString(serializer, it) }
    }

    override fun parseValue(value: String): T {
        return Json.decodeFromString(serializer, value)
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, Json.encodeToString(serializer, value))
    }
}

inline fun <reified T> serializableNavType(): NavType<T> {
    return SerializableNavType(serializer())
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
        val specialLocation: FavoriteTripType? = null,
        val from: WrapLocation? = null,
        val via: WrapLocation? = null,
        val to: WrapLocation? = null,
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
fun TransportrNavigationController() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Map()
    ) {
        composable<Routes.Map>(
            typeMap = mapOf(typeOf<WrapLocation?>() to WrapLocationNavType)
        ) {
            val params: Routes.Map = it.toRoute()

            MapScreen(
                viewModel = koinViewModel(),
                navController = navController,
                geoUri = params.geoUri,
                location = params.location
            )
        }

        composable<Routes.Directions>(
            typeMap = mapOf(typeOf<WrapLocation?>() to WrapLocationNavType)
        ) {
            val params: Routes.Directions = it.toRoute()

            DirectionsScreen(
                viewModel = koinViewModel(),
                navController = navController,
                from = params.from,
                via = params.via,
                to = params.to,
                specialLocation = params.specialLocation,
                search = params.search,
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
//                    val intent = Intent(context, PickTransportNetworkActivity::class.java)
//                    ActivityCompat.startActivity(context, intent, null)
                    navController.navigate(route = Routes.TransportNetworkSelector)
                }
            )
        }

        composable<Routes.TripDetail>(
            typeMap = mapOf(typeOf<Trip>() to serializableNavType<Trip>())
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
            val viewModel = koinViewModel<SettingsViewModel>()
            TransportNetworkSelectorScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}