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

package de.libf.transportrng.ui.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.libf.transportrng.Routes
import de.libf.transportrng.data.gps.GpsState
import de.libf.transportrng.data.gps.enabled
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.composables.BaseLocationGpsInput
import de.libf.transportrng.ui.favorites.SavedSearchesActions
import de.libf.transportrng.ui.favorites.SavedSearchesComponent
import de.libf.transportrng.ui.map.composables.GpsFabComposable
import de.libf.transportrng.ui.map.composables.LocationComponent
import de.libf.transportrng.ui.map.composables.MapNavDrawerContent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.directions
import transportr_ng.composeapp.generated.resources.ic_gps
import transportr_ng.composeapp.generated.resources.ic_menu_black
import transportr_ng.composeapp.generated.resources.ic_menu_directions
import transportr_ng.composeapp.generated.resources.material_drawer_open
import transportr_ng.composeapp.generated.resources.search_hint
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    navController: NavController,
    geoUri: String? = null,
    location: WrapLocation? = null
) {
    val searchSuggestions by viewModel.locationSuggestions.collectAsStateWithLifecycle(emptySet())
    val focusManager = LocalFocusManager.current
    val mapState = remember { provideMapState() }

    val scope = rememberCoroutineScope()
    val sheetContentState by viewModel.sheetContentState.collectAsStateWithLifecycle()
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

    val favoriteTrips by viewModel.favoriteTrips.collectAsStateWithLifecycle(emptyList())
    val specialTrips by viewModel.specialLocations.collectAsStateWithLifecycle(emptyList())

    val transportNetworks by viewModel.transportNetworks.collectAsStateWithLifecycle(emptyList())

    val locationState by viewModel.gpsRepository.getGpsStateFlow().collectAsStateWithLifecycle(
        GpsState.Disabled
    )

    LaunchedEffect(locationState) {
        mapState.showUserLocation(locationState.enabled)
    }


    val screenHeight = remember { mutableStateOf(300.dp) }

    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
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
                },
                modifier = Modifier.fillMaxSize()
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
                                            .heightIn(max = screenHeight.value / 2)
                                    )
                                }

                                is BottomSheetContentState.SavedSearches -> {
                                    SavedSearchesComponent(
                                        items = favoriteTrips,
                                        specialLocations = specialTrips,
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                                        modifier = Modifier
                                            .navigationBarsPadding()
                                            .heightIn(max = (screenHeight.value / 2)),
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
                                    painter = painterResource(Res.drawable.ic_menu_black),
                                    contentDescription = stringResource(Res.string.material_drawer_open),
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
                                placeholder = stringResource(Res.string.search_hint)
                            )

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        GpsFabComposable(
                            gpsState = locationState,
                            onLongClick = {
                                viewModel.gpsRepository.setEnabled(!viewModel.gpsRepository.isEnabled)
                                          },
                            onClick = {
                                println(locationState)

                                scope.launch {
                                mapState.animateTo(it, 14)
                            } },
                        )

                        FloatingActionButton(
                            onClick = {
                                navController.navigate(
                                    route = Routes.Directions(
                                        from = WrapLocation(WrapLocation.WrapType.GPS)
                                    )
                                )
                            },
                            modifier = Modifier

                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_menu_directions),
                                contentDescription = stringResource(Res.string.directions),
                            )
                        }
                    }


                }
            }
        },
        measurePolicy = { measurables, constraints ->
            // Use the max width and height from the constraints
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            screenHeight.value = height.toDp()

            // Measure and place children composables
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            layout(width, height) {
                var yPosition = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(x = 0, y = yPosition)
                    yPosition += placeable.height
                }
            }
        }
    )
}