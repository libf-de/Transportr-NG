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

package de.grobox.transportr.ui.directions

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.map
import androidx.navigation.NavController
import de.grobox.transportr.R
import de.grobox.transportr.Routes
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KTrip
import de.grobox.transportr.favorites.trips.FavoriteTripType
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.directions.composables.DirectionsActions
import de.grobox.transportr.ui.directions.composables.DirectionsSearchHeader
import de.grobox.transportr.ui.directions.composables.DirectionsSearchHeaderBtmRow
import de.grobox.transportr.ui.directions.composables.DirectionsTopAppBar
import de.grobox.transportr.ui.directions.composables.SearchResultComponent
import de.grobox.transportr.ui.favorites.SavedSearchesActions
import de.grobox.transportr.ui.favorites.SavedSearchesComponent
import de.grobox.transportr.ui.productselector.ProductSelectorDialog

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DirectionsScreen(
    viewModel: DirectionsViewModel,
    navController: NavController,
    from: WrapLocation?,
    via: WrapLocation?,
    to: WrapLocation?,
    specialLocation: FavoriteTripType?,
    search: Boolean,
    onSelectDepartureClicked: () -> Unit = {},
    onSelectDepartureLongClicked: () -> Unit = {},
    tripClicked: (KTrip) -> Unit = {},
    changeHome: () -> Unit = {},
    changeWork: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        if(specialLocation == FavoriteTripType.WORK || specialLocation == FavoriteTripType.HOME) {
            //TODO: Home/Work location
        } else {
            viewModel.setFromLocation(from)
            viewModel.setViaLocation(via)
            viewModel.setToLocation(to)
        }

        if(search) viewModel.search()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isFavorite by viewModel.isFavTrip.observeAsState(false)

    var viaVisible by remember { mutableStateOf(false) }
    val expandedTopBarHeight by animateDpAsState(
        targetValue = if (viaVisible) 280.dp else 228.dp,
        animationSpec = tween(
            durationMillis = 1000,
        ), label = "expandedTopBarHeight"
    )

    val gpsLoading by viewModel.gpsLoading.collectAsState(false)
    val fromLocation by viewModel.fromLocation.observeAsState()
    val viaLocation by viewModel.viaLocation.observeAsState()
    val toLocation by viewModel.toLocation.observeAsState()

    val suggestions by viewModel.locationSuggestions.observeAsState()
    val suggestionsLoading by viewModel.suggestionsLoading.observeAsState(false)

    val departureCalendar by viewModel.lastQueryCalendar.observeAsState()
    val isDeparture by viewModel.isDeparture.observeAsState(true)

    var showProductSelector by remember { mutableStateOf(false) }
    val isProductsChanged by viewModel.products.map { KProduct.ALL != it }.observeAsState(false)

    val showTrips by viewModel.displayTrips.observeAsState(false)
    val trips by viewModel.trips.observeAsState()
    val favoriteTrips by viewModel.favoriteTrips.observeAsState(emptyList())
    val specialTrips by viewModel.specialLocations.observeAsState(emptyList())

    ProductSelectorDialog(
        show = showProductSelector,
        onConfirmation = {
            viewModel.setProducts(it)
            showProductSelector = false
        },
        onDismissRequest = {
            showProductSelector = false
        },
        selectedProducts = viewModel.products.value?.toList()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            DirectionsTopAppBar(
                scrollBehavior = scrollBehavior,
                actions = {
                    DirectionsActions(
                        isFavTrip = isFavorite,
                        onFavTripClicked = viewModel::toggleFavTrip,
                        onSwapLocationsClicked = viewModel::swapFromAndToLocations,
                        viaVisible = viaVisible,
                        viaToggleScale = (1f - scrollBehavior.state.collapsedFraction),
                        onViaToggleClicked = { viaVisible = !viaVisible }
                    )
                },

                smallHeader = {
                    Column {
                        Text(
                            text = fromLocation?.fullName ?: "Kein Start",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = toLocation?.fullName ?: "Kein Ziel",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },

                expandedHeader = {
                    DirectionsSearchHeader(
                        fromLocation = fromLocation,
                        setFromLocation = viewModel::setFromLocation,

                        viaVisible = viaVisible,
                        viaLocation = viaLocation,
                        setViaLocation = viewModel::setViaLocation,

                        toLocation = toLocation,
                        setToLocation = viewModel::setToLocation,

                        suggestions = suggestions,
                        requestSuggestions = { viewModel.cancelGps(); viewModel.suggestLocations(it) },
                        requestResetSuggestions = viewModel::resetSuggestions,
                        suggestionsLoading = suggestionsLoading,
                        gpsLoading = gpsLoading,
                    ) {
                        DirectionsSearchHeaderBtmRow(
                            departureCalendar = departureCalendar,
                            isDeparture = isDeparture,
                            onSelectDepartureClicked = onSelectDepartureClicked,
                            onSelectDepartureLongClicked = onSelectDepartureLongClicked,
                            showProductSelector = { showProductSelector = true },
                            isProductsChanged = isProductsChanged
                        )
                    }
                },

                expandedHeight = expandedTopBarHeight,
                onNavigateBack = navController::popBackStack
            )
        },
    ) { pv ->
        if(showTrips) {
            // TODO: Show error states:
            //    val topSwipeEnabled by viewModel.topSwipeEnabled.observeAsState(false)
            //    val queryMoreState by viewModel.queryMoreState.observeAsState()
            //    val trips by viewModel.trips.observeAsState()
            //    val queryError by viewModel.queryError.observeAsState()
            //    val queryPTEError by viewModel.queryPTEError.observeAsState()
            //    val queryMoreError by viewModel.queryMoreError.observeAsState()

            SearchResultComponent(
                modifier = Modifier.padding(pv),
                trips = trips?.toSet(),
                tripClicked = tripClicked
            )
        } else {
            SavedSearchesComponent(
                modifier = Modifier.padding(pv),
                contentPadding = PaddingValues(8.dp),
                items = favoriteTrips,
                specialLocations = specialTrips,
                actions = SavedSearchesActions(
                    requestDirectionsFromViaTo = { from, via, to, search ->
                        navController.navigate(
                            route = Routes.Directions(
                                from = from,
                                via = via,
                                to = to,
                                search = search
                            )
                        )
                    },
                    requestDeparturesFrom = {
                        navController.navigate(
                            route = Routes.Departures(
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





@Composable
fun ProductSelectorIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    changed: Boolean
) {
    val circleColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.height(IntrinsicSize.Min).width(IntrinsicSize.Min)) {
        Icon(
            painter = painterResource(R.drawable.product_bus),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.secondary
        )

        if(changed) {
            Canvas(
                modifier = Modifier
                    .size(iconSize / 3f)
                    .align(Alignment.TopEnd)
            ) {
                drawCircle(
                    color = circleColor,
                    radius = size.minDimension / 2
                )
            }
        }
    }
}
