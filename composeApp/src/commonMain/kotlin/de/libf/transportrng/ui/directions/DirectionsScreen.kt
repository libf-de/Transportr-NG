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

package de.libf.transportrng.ui.directions

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.libf.transportrng.Routes
import de.libf.transportrng.ui.directions.composables.DirectionsActions
import de.libf.transportrng.ui.directions.composables.DirectionsSearchHeader
import de.libf.transportrng.ui.directions.composables.DirectionsSearchHeaderBtmRow
import de.libf.transportrng.ui.directions.composables.DirectionsTopAppBar
import de.libf.transportrng.ui.directions.composables.SearchResultComponent
import de.libf.transportrng.ui.favorites.SavedSearchesActions
import de.libf.transportrng.ui.favorites.SavedSearchesComponent
import de.libf.transportrng.ui.productselector.ProductSelectorDialog
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.favorites.FavoriteTripType
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.departures.composables.DepartureComposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.error
import transportr_ng.composeapp.generated.resources.product_bus
import transportr_ng.composeapp.generated.resources.try_again

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
fun DirectionsScreen(
    viewModel: DirectionsViewModel,
    navController: NavController,
    from: WrapLocation?,
    via: WrapLocation?,
    to: WrapLocation?,
    specialLocation: FavoriteTripType?,
    search: Boolean,
    time: Long?,
    onSelectDepartureClicked: () -> Unit = {},
    onSelectDepartureLongClicked: () -> Unit = {},
    tripClicked: (Trip) -> Unit = {},
    changeHome: () -> Unit = {},
    changeWork: () -> Unit = {}
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        if(specialLocation == FavoriteTripType.WORK || specialLocation == FavoriteTripType.HOME) {
            //TODO: Home/Work location
        } else {
            if(viewModel.fromLocation.value == null)
                viewModel.setFromLocation(from)
            if(viewModel.viaLocation.value == null)
                viewModel.setViaLocation(via)
            if(viewModel.toLocation.value == null)
                viewModel.setToLocation(to)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isFavorite by viewModel.isFavTrip.collectAsStateWithLifecycle(false)

    var viaVisible by remember { mutableStateOf(false) }
    val expandedTopBarHeight by animateDpAsState(
        targetValue = if (viaVisible) 280.dp else 228.dp,
        animationSpec = tween(
            durationMillis = 1000,
        ), label = "expandedTopBarHeight"
    )

    val gpsLoading by viewModel.gpsLoading.collectAsStateWithLifecycle(false)
    val fromLocation by viewModel.fromLocation.collectAsStateWithLifecycle(null)
    val viaLocation by viewModel.viaLocation.collectAsStateWithLifecycle(null)
    val toLocation by viewModel.toLocation.collectAsStateWithLifecycle(null)

    val suggestions by viewModel.locationSuggestions.collectAsStateWithLifecycle(null)
    val suggestionsLoading by viewModel.suggestionsLoading.collectAsStateWithLifecycle(false)

    val departureCalendar by viewModel.lastQueryCalendar.collectAsStateWithLifecycle(
        time?.let(Instant::fromEpochMilliseconds)
    )
    val isDeparture by viewModel.isDeparture.collectAsStateWithLifecycle(true)

    val products by viewModel.products.collectAsStateWithLifecycle()
    var showProductSelector by remember { mutableStateOf(false) }
    val isProductsChanged by viewModel.products.map { Product.ALL != it }.collectAsStateWithLifecycle(false)

    val showTrips by viewModel.displayTrips.collectAsStateWithLifecycle(false)
    val trips by viewModel.trips.mapLatest {
        it.sortedBy { trip -> trip.firstDepartureTime }
    }.collectAsStateWithLifecycle(null)
    val favoriteTrips by viewModel.favoriteTrips.collectAsStateWithLifecycle(emptyList())
    val specialTrips by viewModel.specialLocations.collectAsStateWithLifecycle(emptyList())

    val queryError by viewModel.queryError.collectAsStateWithLifecycle(null)
    val queryPTEError by viewModel.queryPTEError.collectAsStateWithLifecycle(null)
    val queryMoreError by viewModel.queryMoreError.collectAsStateWithLifecycle(null)

    ProductSelectorDialog(
        show = showProductSelector,
        onConfirmation = {
            viewModel.setProducts(it)
            showProductSelector = false
        },
        onDismissRequest = {
            showProductSelector = false
        },
        selectedProducts = products.toList()
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
        when(val viewState = viewState) {
            is DirectionsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            is DirectionsState.ShowFavorites -> {
                SavedSearchesComponent(
                    modifier = Modifier.padding(pv),
                    contentPadding = PaddingValues(8.dp),
                    items = viewState.favorites,
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

            is DirectionsState.ShowTrips -> {
                SearchResultComponent(
                    modifier = Modifier.padding(pv),
                    trips = trips?.toSet(),
                    tripClicked = tripClicked,
                    onLoadMoreRequested = viewModel::searchMore
                )
            }

            is DirectionsState.ShowDepartures -> {
                LazyColumn(modifier = Modifier.padding(pv)) {
                    items(viewState.departures) {
                        DepartureComposable(
                            departure = it,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            is DirectionsState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.error),
                            style = MaterialTheme.typography.headlineSmall,
                        )

                        Text(text = viewState.message)

                        FilledTonalButton(
                            onClick = {
                                viewModel.reload()
                            }
                        ) {
                            Text(text = stringResource(Res.string.try_again))
                        }
                    }
                }
            }
        }


        if(showTrips) {
            // TODO: Show error states:
            //    val topSwipeEnabled by viewModel.topSwipeEnabled.observeAsState(false)
            //    val queryMoreState by viewModel.queryMoreState.observeAsState()
            //    val trips by viewModel.trips.observeAsState()
            //    val queryError by viewModel.queryError.observeAsState()
            //    val queryPTEError by viewModel.queryPTEError.observeAsState()
            //    val queryMoreError by viewModel.queryMoreError.observeAsState()

            if(queryError != null || queryPTEError != null) {
                Column {
                    if(queryError != null) {
                        Text(queryError ?: "error")
                    }
                    if(queryPTEError != null) {
                        Text(queryPTEError?.let { "${it.first} ${it.second}"} ?: "error")
                    }
                }
            }


        } else {

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
            painter = painterResource(Res.drawable.product_bus),
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
