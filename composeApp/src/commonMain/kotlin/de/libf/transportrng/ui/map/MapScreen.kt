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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
//import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.libf.transportrng.MapRoutes
import de.libf.transportrng.Routes
import de.libf.transportrng.data.favorites.FavoriteTripItem
import de.libf.transportrng.data.gps.GpsState
import de.libf.transportrng.data.gps.enabled
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.protobufNavType
import de.libf.transportrng.ui.composables.AnchoredDraggableDefaults
import de.libf.transportrng.ui.composables.AnchoredDraggableState
import de.libf.transportrng.ui.composables.BaseLocationGpsInput
import de.libf.transportrng.ui.composables.CustomBottomSheetScaffold
import de.libf.transportrng.ui.composables.rememberCustomBottomSheetScaffoldState
import de.libf.transportrng.ui.composables.rememberStandardBottomSheetState
import de.libf.transportrng.ui.favorites.SavedSearchesActions
import de.libf.transportrng.ui.favorites.SavedSearchesComponent
import de.libf.transportrng.ui.favorites.composables.SpecialLocationItem
import de.libf.transportrng.ui.favorites.composables.SpecialLocationItemActions
import de.libf.transportrng.ui.map.composables.GpsFabComposable
import de.libf.transportrng.ui.map.composables.LocationComponent
import de.libf.transportrng.ui.map.composables.MapNavDrawerContent
import de.libf.transportrng.ui.map.sheets.LocationDetailSheetContent
import de.libf.transportrng.ui.map.sheets.SavedSearchesSheetComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.directions
import transportr_ng.composeapp.generated.resources.ic_menu_black
import transportr_ng.composeapp.generated.resources.ic_menu_directions
import transportr_ng.composeapp.generated.resources.material_drawer_open
import transportr_ng.composeapp.generated.resources.search_hint
import kotlin.math.roundToInt
import kotlin.reflect.typeOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    navController: NavHostController,
    geoUri: String? = null,
    location: WrapLocation? = null
) {
    val mapNavController = rememberNavController()


    val searchSuggestions by viewModel.locationSuggestions.collectAsStateWithLifecycle(emptySet())
    val focusManager = LocalFocusManager.current
    val mapState = remember { provideMapState() }

    val scope = rememberCoroutineScope()
    val sheetContentState by viewModel.sheetContentState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scafState = rememberCustomBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
        )
    )

//    val scafState = rememberBottomSheetScaffoldState(
//        bottomSheetState = androidx.compose.material3.rememberStandardBottomSheetState(
//            initialValue = SheetValue.Expanded,
//        )
//    )

    var bottomSheetVisible by remember { mutableStateOf(true) }

    val barPadding = WindowInsets.systemBars.asPaddingValues()

    val favoriteTrips by viewModel.favoriteTrips.collectAsStateWithLifecycle(emptyList())
    val specialTrips by viewModel.specialLocations.collectAsStateWithLifecycle(emptyList())

    val transportNetworks by viewModel.transportNetworks.collectAsStateWithLifecycle(emptyList())

    val locationState by viewModel.gpsRepository.getGpsStateFlow().collectAsStateWithLifecycle(
        GpsState.Disabled
    )

    val nearbyStations by viewModel.nearbyStations.collectAsStateWithLifecycle(NearbyLocationsState.Initial)
    val mapCenter by mapState.currentMapCenter.collectAsStateWithLifecycle(null)

    fun updateNearbyStationsByMapPosition() {
        mapCenter?.let {
            viewModel.findNearbyStations(
                WrapLocation(
                    LatLng(
                        it.latitude,
                        it.longitude
                    )
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        mapState.onLocationClicked = { loc ->
            mapNavController.navigate(
                route = MapRoutes.LocationDetail(loc)
            )
        }
    }

    LaunchedEffect(locationState) {
        val loc = when(val locationState = locationState) {
            is GpsState.Enabled -> locationState.location
            else -> null
        }
        mapState.showUserLocation(locationState.enabled, loc)

//        when(val locationState = locationState) {
//            is GpsState.Enabled -> viewModel.findNearbyStations(
//                WrapLocation(
//                    LatLng(
//                        locationState.location.lat,
//                        locationState.location.lon
//                    )
//                )
//            )
//            else -> {}
//        }
    }

    LaunchedEffect(mapCenter) {
        updateNearbyStationsByMapPosition()
    }

    LaunchedEffect(nearbyStations) {
        if(nearbyStations is NearbyLocationsState.Success) {
            scope.launch {
                mapState.drawNearbyStations((nearbyStations as NearbyLocationsState.Success).locations)
            }
        }
    }


    val screenHeight = remember { mutableStateOf(300.dp) }

    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                drawerContent = {
                    SpecialLocationItem(
                        location = FavoriteTripItem(
                            uid = 11L,
                            from = WrapLocation(
                                LatLng(11.0, 12.0)
                            ),
                            to = WrapLocation(
                                LatLng(13.0, 14.0)
                            )
                        ),
                        actions = SpecialLocationItemActions()
                    )


                    MapNavDrawerContent(
                        transportNetworks = transportNetworks,
                        onNetworkClick = {
                            viewModel.setTransportNetwork(it)
                            scope.launch {
                                mapState.clearNearbyStations()
                                updateNearbyStationsByMapPosition()
                            }
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
                    CustomBottomSheetScaffold(
//                    BottomSheetScaffold(
                        scaffoldState = scafState,
                        sheetContent = {
                            NavHost(
                                navController = mapNavController,
                                startDestination = if(location == null) MapRoutes.SavedSearches else MapRoutes.LocationDetail(location),
                                modifier = Modifier.animateContentSize()
                                /*route = Routes.Map::class,
                                typeMap = mapOf(typeOf<WrapLocation?>() to protobufNavType<WrapLocation?>())*/
                            ) {
                                composable<MapRoutes.SavedSearches> {
                                    SavedSearchesSheetComponent(
                                        viewModel = koinViewModel(),
                                        rootNavController = navController,
                                        mapNavController = mapNavController,
                                        modifier = Modifier.navigationBarsPadding()
                                            .heightIn(max = screenHeight.value / 2)
                                    )
                                }

                                composable<MapRoutes.LocationDetail>(
                                    typeMap = mapOf(typeOf<WrapLocation>() to protobufNavType<WrapLocation>())
                                ) {
                                    val params = it.toRoute<MapRoutes.LocationDetail>()

                                    LocationDetailSheetContent(
                                        viewModel = koinViewModel(),
                                        rootNavController = navController,
                                        mapNavController = mapNavController,
                                        location = params.location,
                                        modifier = Modifier.navigationBarsPadding()
                                            .heightIn(max = screenHeight.value / 2)
                                    )
                                }
                            }


                            MapScreenSheetContent(
                                sheetContentState = sheetContentState,
                                maxContentHeight = screenHeight.value / 2,
                                navController = navController,
                                viewModel = viewModel
                            )
                        },
                        modifier = Modifier.animateContentSize()
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
                                    navController.navigate(
                                        Routes.Directions(
                                            from = WrapLocation(WrapLocation.WrapType.GPS),
                                            to = it
                                        )
                                    )
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
                        val haptics = LocalHapticFeedback.current

                        GpsFabComposable(
                            gpsState = locationState,
                            onLongClick = {
                                viewModel.gpsRepository.setEnabled(!viewModel.gpsRepository.isEnabled)
                            },
                            onClick = {
                                if(!viewModel.gpsRepository.isEnabled)
                                    viewModel.gpsRepository.setEnabled(true)

                                locationState.takeIf {
                                    it is GpsState.Enabled
                                }?.let {
                                    val loc = (it as GpsState.Enabled).location.let {
                                        LatLng(
                                            it.lat,
                                            it.lon
                                        )
                                    }

                                    scope.launch {
                                        mapState.animateTo(loc, 14)
                                    }
                                } ?: run {
                                    scope.launch {
                                        repeat((0 until 8).count()) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            delay(50)
                                        }
                                    }
                                }
                            },
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

@Composable
fun MapScreenSheetContent(
    sheetContentState: BottomSheetContentState,
    maxContentHeight: Dp,
    navController: NavController,
    viewModel: MapViewModel
) {
    AnimatedContent(
        targetState = sheetContentState,
        transitionSpec = {
            if (initialState is BottomSheetContentState.Loading) {
                fadeIn() togetherWith fadeOut()
            } else if (targetState is BottomSheetContentState.Location) {
                slideInHorizontally { height -> height } + fadeIn() togetherWith
                        slideOutHorizontally { height -> height } + fadeOut()
            } else if(targetState is BottomSheetContentState.SavedSearches) {
                slideInHorizontally { height -> -height } + fadeIn() togetherWith
                        slideOutHorizontally { height -> -height } + fadeOut()
            } else {
                fadeIn(tween(durationMillis = 0)) togetherWith
                        fadeOut(tween(durationMillis = 0))
            }.using(
                SizeTransform(clip = false)
            )
        }, label = "animated content"
    ) { targetCount ->
        when(val state = sheetContentState) {
            is BottomSheetContentState.Location -> {

            }

            is BottomSheetContentState.SavedSearches -> {

            }
            else -> { }
        }
    }
}