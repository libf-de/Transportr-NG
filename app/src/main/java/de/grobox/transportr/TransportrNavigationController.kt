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

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.grobox.transportr.favorites.trips.FavoriteTripType
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.MapScreen
import de.grobox.transportr.map.MapViewModel
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.settings.SettingsComposeFragment
import de.grobox.transportr.settings.SettingsScreen
import de.grobox.transportr.trips.search.DirectionsScreen
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import kotlin.reflect.typeOf
import org.koin.androidx.viewmodel.ext.android.viewModel

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

@Composable
fun TransportrNavigationController() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Map
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
                search = params.search
            )
        }

        composable<Routes.Settings> {
            val context = LocalContext.current
            SettingsScreen(
                onSelectNetworkClicked = {
                    val intent = Intent(context, PickTransportNetworkActivity::class.java)
                    ActivityCompat.startActivity(context, intent, null)
                }
            )
        }
    }
}