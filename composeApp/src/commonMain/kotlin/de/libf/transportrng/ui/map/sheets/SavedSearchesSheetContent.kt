package de.libf.transportrng.ui.map.sheets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.libf.transportrng.MapRoutes
import de.libf.transportrng.Routes
import de.libf.transportrng.data.favorites.FavoriteTripItem
import de.libf.transportrng.ui.favorites.SavedSearchesActions
import de.libf.transportrng.ui.favorites.SavedSearchesComponent
import de.libf.transportrng.ui.map.MapViewModel



@Composable
fun SavedSearchesSheetComponent(
    viewModel: SavedSearchesSheetViewModel,
    rootNavController: NavController,
    mapNavController: NavController,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(
        targetState = uiState,
        label = "savedSearchesSheetCrossfade"
    ) { state ->
        when(state) {
            is SavedSearchesSheetState.Loading -> {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.fillMaxWidth().height(168.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
            is SavedSearchesSheetState.Success -> {
                SavedSearchesComponent(
                    items = state.favorites,
                    specialLocations = state.specials,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    modifier = modifier,
                    actions = SavedSearchesActions(
                        requestDirectionsFromViaTo = { from, via, to, search ->
                            rootNavController.navigate(
                                route = Routes.Directions(
                                    from = from,
                                    via = via,
                                    to = to,
                                    search = search
                                )
                            )
                        },
                        requestDeparturesFrom = {
                            mapNavController.navigate(
                                route = MapRoutes.LocationDetail(
                                    location = it
                                )
                            )
                        },
                        requestSetTripFavorite = viewModel::setFavoriteTrip,
                        onChangeSpecialLocationRequested = {},
                        onCreateShortcutRequested = {},
                        requestDelete = viewModel::removeFavoriteTrip
                    )
                )
            }

        }
    }



}