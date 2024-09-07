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

package de.grobox.transportr.ui.directions.composables

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R

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
                painterResource(R.drawable.ic_action_star)
            else
                painterResource(R.drawable.ic_action_star_empty),
            contentDescription = stringResource(R.string.action_fav_trip)
        )
    }

    IconButton(onClick = {
        //TODO: Animate
        onSwapLocationsClicked()
    }) {
        Icon(
            painterResource(R.drawable.ic_menu_swap_location),
            contentDescription = stringResource(R.string.action_swap_locations)
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
                    painterResource(R.drawable.ic_action_navigation_unfold_less)
                else
                    painterResource(R.drawable.ic_action_navigation_unfold_more),
                contentDescription = stringResource(R.string.action_swap_locations)
            )
        }
    }
}