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

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import de.grobox.transportr.favorites.trips.FavoriteTripType
import de.grobox.transportr.locations.WrapLocation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
}