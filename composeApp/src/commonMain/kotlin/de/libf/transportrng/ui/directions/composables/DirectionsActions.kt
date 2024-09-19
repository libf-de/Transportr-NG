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

package de.libf.transportrng.ui.directions.composables

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_fav_trip
import transportr_ng.composeapp.generated.resources.action_swap_locations
import transportr_ng.composeapp.generated.resources.ic_action_navigation_unfold_less
import transportr_ng.composeapp.generated.resources.ic_action_navigation_unfold_more
import transportr_ng.composeapp.generated.resources.ic_action_star
import transportr_ng.composeapp.generated.resources.ic_action_star_empty
import transportr_ng.composeapp.generated.resources.ic_menu_swap_location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.DirectionsActions(
    isFavTrip: Boolean,
    onFavTripClicked: () -> Unit,
    onSwapLocationsClicked: () -> Unit,
    viaToggleScale: Float,
    viaVisible: Boolean,
    onViaToggleClicked: () -> Unit
) {
    IconButton(onClick = {
        onFavTripClicked()
    }) {
        Icon(
            painter = if(isFavTrip)
                painterResource(Res.drawable.ic_action_star)
            else
                painterResource(Res.drawable.ic_action_star_empty),
            contentDescription = stringResource(Res.string.action_fav_trip)
        )
    }

    IconButton(onClick = {
        //TODO: Animate
        onSwapLocationsClicked()
    }) {
        Icon(
            painterResource(Res.drawable.ic_menu_swap_location),
            contentDescription = stringResource(Res.string.action_swap_locations)
        )
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        IconButton(
            onClick = { onViaToggleClicked() },
            enabled = viaToggleScale == 1f,
            modifier = Modifier
                .scale(scaleX = viaToggleScale, scaleY = 1f)
                .width(48.dp * viaToggleScale)
        ) {
            Icon(
                if (viaVisible)
                    painterResource(Res.drawable.ic_action_navigation_unfold_less)
                else
                    painterResource(Res.drawable.ic_action_navigation_unfold_more),
                contentDescription = stringResource(Res.string.action_swap_locations)
            )
        }
    }
}