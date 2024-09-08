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

package de.grobox.transportr.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.grobox.transportr.R
import de.grobox.transportr.Routes
import de.grobox.transportr.composables.BaseLocationGpsInput
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.favorites.SavedSearchesActions
import de.grobox.transportr.ui.favorites.SavedSearchesComponent
import de.grobox.transportr.ui.map.composables.LocationComponent
import de.grobox.transportr.ui.map.composables.MapNavDrawerContent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    navController: NavController,
    geoUri: String? = null,
    location: WrapLocation? = null
) {
    val searchSuggestions by viewModel.locationSuggestions.observeAsState()
    val focusManager = LocalFocusManager.current
    val mapState = remember { MapViewState() }

    val scope = rememberCoroutineScope()
    val sheetContentState by viewModel.sheetContentState.observeAsState()
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scafState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
        )
    )
    var bottomSheetVisible by remember { mutableStateOf(true) }

    val barPadding = WindowInsets.systemBars.asPaddingValues()

    val favoriteTrips by viewModel.favoriteTrips.observeAsState(emptyList())
    val specialTrips by viewModel.specialLocations.observeAsState(emptyList())

    val transportNetworks by viewModel.transportNetworks.observeAsState(emptyList())

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
                MapNavDrawerContent(
                    transportNetworks = transportNetworks,
                    onNetworkClick = {
                        viewModel.setTransportNetwork(it)
                    },
                    onSettingsClick = {
                        navController.navigate(
                            route = Routes.Settings
                        )
                    },
                    onChangelogClick = {
//                        navController.navigate(
//                            route = Routes.Changelog
//                        )
                    },
                    onContributorsClick = {
//                        navController.navigate(
//                            route = Routes.Contributors
//                        )
                    },
                    onReportAProblemClick = {
//                        navController.navigate(
//                            route = Routes.ReportAProblem
//                        )
                    },
                    onAboutClick = {
//                        navController.navigate(
//                            route = Routes.About
//                        )
                    },
                    onNetworkCardClick = {
                        navController.navigate(route = Routes.TransportNetworkSelector)
                    }
                )
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            BottomSheetScaffold(
                sheetContent = {
                    when(val state = sheetContentState) {
                        is BottomSheetContentState.Location -> {
                            LocationComponent(
                                location = state.loc,
                                lines = state.lines,
                                modifier = Modifier
                                    .heightIn(max = (LocalConfiguration.current.screenHeightDp / 2).dp)
                            )
                        }

                        is BottomSheetContentState.SavedSearches -> {
                            SavedSearchesComponent(
                                items = favoriteTrips,
                                specialLocations = specialTrips,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .heightIn(max = (LocalConfiguration.current.screenHeightDp / 2).dp),
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
                        else -> { }
                    }
                },
                scaffoldState = scafState,
            ) { contentPadding ->
                MapViewComposable(
                    mapViewState = mapState,
                    compassMargins = CompassMargins(top = 72.dp),
                    showAttribution = false,
                    showLogo = false,
                    rotateGestures = true
                )
            }

            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .height(48.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
//                            navController.navigate(
//                                route = Routes.Settings
//                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu_black),
                            contentDescription = stringResource(R.string.material_drawer_open),
                        )
                    }

                    BaseLocationGpsInput(
                        location = null,
                        onAcceptSuggestion = {
                            /* TODO */
                        },
                        onValueChange = {
                            viewModel.suggestLocations(it)
                        },
                        suggestions = searchSuggestions,
                        modifier = Modifier.height(48.dp),
                        placeholder = stringResource(R.string.search_hint)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        route = Routes.Directions(
                            from = WrapLocation(WrapLocation.WrapType.GPS)
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        val sheetOffset = scafState.bottomSheetState.requireOffset()
                        val fabSize = 56.dp // Assuming standard FAB size

                        //println(-(sheetOffset.roundToInt() + fabSize.roundToPx() / 2))

                        IntOffset(
                            x = -16.dp.roundToPx(),
                            y = (sheetOffset.roundToInt() - (fabSize.roundToPx() / 2))
                        )
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu_directions),
                    contentDescription = stringResource(R.string.directions),
                )
            }
        }
    }
}




