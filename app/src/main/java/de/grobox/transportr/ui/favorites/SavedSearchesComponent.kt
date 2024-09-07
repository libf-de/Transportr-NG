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

package de.grobox.transportr.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.favorites.composables.SavedSearchItem
import de.grobox.transportr.ui.favorites.composables.SavedSearchItemActions
import de.grobox.transportr.ui.favorites.composables.SpecialLocationItem
import de.grobox.transportr.ui.favorites.composables.SpecialLocationItemActions

data class SavedSearchesActions(
    val onFindDirectionsRequested: (from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, search: Boolean) -> Unit,
    val onFindDeparturesRequested: (location: WrapLocation) -> Unit,
    val onCreateShortcutRequested: (item: FavoriteTripItem) -> Unit,
    val onChangeSpecialLocationRequested: (item: FavoriteTripItem) -> Unit,
    val onPresetDirectionsRequested: (from: WrapLocation?, via: WrapLocation?, to: WrapLocation?) -> Unit,
    val onSetTripFavoriteRequested: (item: FavoriteTripItem, isFavorite: Boolean) -> Unit,
    val onDeleteRequested: (item: FavoriteTripItem) -> Unit,
    val onItemClicked: (item: FavoriteTripItem) -> Unit,
    val onSpecialItemClicked: (item: FavoriteTripItem) -> Unit
) {
    constructor(
        requestDirectionsFromViaTo: (from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, search: Boolean) -> Unit,
        requestDeparturesFrom: (location: WrapLocation) -> Unit,
        requestSetTripFavorite: (item: FavoriteTripItem, isFavorite: Boolean) -> Unit,
        onChangeSpecialLocationRequested: (item: FavoriteTripItem) -> Unit,
        onCreateShortcutRequested: (item: FavoriteTripItem) -> Unit,
        requestDelete: (item: FavoriteTripItem) -> Unit
    ) : this(
        onFindDirectionsRequested = requestDirectionsFromViaTo,
        onFindDeparturesRequested = requestDeparturesFrom,
        onSetTripFavoriteRequested = requestSetTripFavorite,
        onDeleteRequested = requestDelete,
        onChangeSpecialLocationRequested = onChangeSpecialLocationRequested,
        onCreateShortcutRequested = onCreateShortcutRequested,
        onPresetDirectionsRequested = { from, via, to ->
            requestDirectionsFromViaTo(from, via, to, false)
        },
        onItemClicked = {
            requestDirectionsFromViaTo(it.from, it.via, it.to, true)
        },
        onSpecialItemClicked = {
            requestDirectionsFromViaTo(WrapLocation(WrapLocation.WrapType.GPS), null, it.to,true)
        }
    )
}

@Composable
fun SavedSearchesComponent(
    items: List<FavoriteTripItem>,
    specialLocations: List<FavoriteTripItem>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    actions: SavedSearchesActions
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding
    ) {
        items(specialLocations) {
            SpecialLocationItem(
                location = it,
                onItemClicked = {
                    actions.onSpecialItemClicked(it)
                },
                actions = SpecialLocationItemActions(
                    onFindDirectionsRequested = actions.onFindDirectionsRequested,
                    onFindDeparturesRequested = actions.onFindDeparturesRequested,
                    onCreateShortcutRequested = actions.onCreateShortcutRequested,
                    onChangeSpecialLocationRequested = actions.onChangeSpecialLocationRequested
                )
            )
        }


        items(items) {
            SavedSearchItem(
                itm = it,
                onItemClicked = {
                    actions.onItemClicked(it)
                },
                actions = SavedSearchItemActions(
                    onFindDirectionsRequested = actions.onFindDirectionsRequested,
                    onCreateShortcutRequested = actions.onCreateShortcutRequested,
                    onPresetDirectionsRequested = actions.onPresetDirectionsRequested,
                    onSetTripFavoriteRequested = actions.onSetTripFavoriteRequested,
                    onDeleteRequested = actions.onDeleteRequested
                )
            )
        }
    }
}