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

package de.libf.transportrng.ui.trips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.libf.ptek.dto.Stop
import de.libf.transportrng.ui.composables.CustomSmallTopAppBar
import de.libf.transportrng.data.utils.formatDuration
import de.libf.transportrng.data.utils.getName
import de.libf.transportrng.ui.map.CompassMargins
import de.libf.transportrng.ui.map.MapViewComposable
import de.libf.transportrng.ui.map.provideMapState
import de.libf.transportrng.ui.trips.composables.LegListComposable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_refresh
import transportr_ng.composeapp.generated.resources.action_share
import transportr_ng.composeapp.generated.resources.action_show_on_external_map
import transportr_ng.composeapp.generated.resources.action_trip_calendar
import transportr_ng.composeapp.generated.resources.connections_by_stop
import transportr_ng.composeapp.generated.resources.continue_journey_later
import transportr_ng.composeapp.generated.resources.find_departures_from_stop
import transportr_ng.composeapp.generated.resources.ic_action_calendar
import transportr_ng.composeapp.generated.resources.ic_action_departures
import transportr_ng.composeapp.generated.resources.ic_action_external_map
import transportr_ng.composeapp.generated.resources.ic_action_navigation_refresh
import transportr_ng.composeapp.generated.resources.ic_action_social_share
import transportr_ng.composeapp.generated.resources.ic_menu_directions
import transportr_ng.composeapp.generated.resources.ic_nearby_stations
import transportr_ng.composeapp.generated.resources.ic_stop
import transportr_ng.composeapp.generated.resources.total_time

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    tripId: String,
    navController: NavController,
    setBarColor: (statusBar: Color, navBar: Color) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )

    val mapState = remember { provideMapState() }

    val expanding by animateFloatAsState(
        animationSpec = tween(300),
        targetValue = if(scaffoldState.bottomSheetState.targetValue == SheetValue.PartiallyExpanded) 0f else 1f
    )

    val fabScale by animateFloatAsState(
        animationSpec = tween(300),
        targetValue = if(scaffoldState.bottomSheetState.isVisible) 0f else 1f
    )

    val appBarColor by animateColorAsState(
        animationSpec = tween(300),
        targetValue = MaterialTheme.colorScheme.let {
            if(scaffoldState.bottomSheetState.targetValue == SheetValue.PartiallyExpanded)
                it.surface
            else
                it.primaryContainer
        }
    )

    var primaryColor = MaterialTheme.colorScheme.primaryContainer
    var surfaceColor = MaterialTheme.colorScheme.surface
    LaunchedEffect(scaffoldState.bottomSheetState.targetValue) {
        if(scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
            setBarColor(primaryColor, surfaceColor)
        } else {
            setBarColor(Color.Transparent, Color.Transparent)
        }
    }

    val trip by viewModel.trip.collectAsStateWithLifecycle()
    val zoomLocation by viewModel.zoomLocation.collectAsStateWithLifecycle(null)
    val zoomLeg by viewModel.zoomLeg.collectAsStateWithLifecycle(null)
    val showLineNames by viewModel.transportNetwork.map { it?.hasGoodLineNames() ?: false }.collectAsStateWithLifecycle(false)

    // TODO: Show reload error as Snackbar
    val tripReloadError by viewModel.tripReloadError.collectAsStateWithLifecycle(null)

    LaunchedEffect(tripId) {
        viewModel.getTripById(tripId)
    }

    LaunchedEffect(trip) {
        if(mapState.drawTrip(trip, viewModel.isFreshStart.value))
            viewModel.isFreshStart.value = false
    }

    LaunchedEffect(zoomLocation) {
        zoomLocation?.let {
            mapState.animateTo(it, 16)
        }
    }

    LaunchedEffect(zoomLeg) {
        zoomLeg?.let {
            mapState.animateToBounds(it)
        }
    }

    val screenHeight = remember { mutableStateOf(300.dp) }

    var showStationActions by remember { mutableStateOf(false) }
    val stationAction = remember { mutableStateOf<Stop?>(null) }

    AnimatedVisibility(showStationActions) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_stop),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stationAction.value?.location.getName() ?: "Haltestelle",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_action_external_map),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(Res.string.action_show_on_external_map))
                    }

                    FilledTonalButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_action_departures),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(Res.string.find_departures_from_stop))
                    }

                    FilledTonalButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_menu_directions),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(Res.string.connections_by_stop))
                    }

                    FilledTonalButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(Res.string.continue_journey_later))
                    }
                }
            }
        )
    }


    Layout(
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        Column(Modifier.fillMaxSize().padding(vertical = 8.dp)) {
                            LegListComposable(
                                legs = trip?.legs ?: emptyList(),
                                showLineNames = showLineNames,
                                modifier = Modifier.fillMaxSize(),
                                onLegClick = { leg ->

                                    scope.launch {
                                        viewModel.setZoomLeg(leg)
                                        scaffoldState.bottomSheetState.partialExpand()
                                    }

                                },

                                onStopLongClick = {
                                    stationAction.value = it
                                    showStationActions = true
                                },

                                onLegLongClick = {}
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    },
                    sheetShape = RoundedCornerShape(
                        expanding.invert().mapRange(0f, 28f).dp
                    ),
                    sheetDragHandle = {
                        CustomSmallTopAppBar(
                            navigationIcon = {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                    IconButton(
                                        onClick = { scope.launch { scaffoldState.bottomSheetState.partialExpand() } },
                                        modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                    ) {
                                        Icon(
                                            Icons.Rounded.KeyboardArrowDown,
                                            null
                                        )
                                    }
                                }
                            },
                            title = {
                                Text(
                                    text = trip?.duration?.let { stringResource(Res.string.total_time, formatDuration(it) ?: "??:??") } ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().alpha(expanding.invert())
                                )
                            },
                            height = expanding.mapRange(48f, 64f).dp,
                            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false }),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = appBarColor,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            actions = {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                    IconButton(
                                        onClick = { viewModel.reloadTrip() },
                                        modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_action_navigation_refresh),
                                            contentDescription = stringResource(Res.string.action_refresh)
                                        )
                                    }

                                    IconButton(
                                        onClick = { /* TODO */ },
                                        modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_action_social_share),
                                            contentDescription = stringResource(Res.string.action_share)
                                        )
                                    }

                                    IconButton(
                                        onClick = { /* TODO */ },
                                        modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_action_calendar),
                                            contentDescription = stringResource(Res.string.action_trip_calendar)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                        )
                    },
                    sheetPeekHeight = screenHeight.value / 2
                ) {
                    MapViewComposable(
                        mapViewState = mapState,
                        compassMargins = CompassMargins(top = 24.dp),
                        isHalfHeight = scaffoldState.bottomSheetState.targetValue != SheetValue.Hidden
                    )
                }

                SmallFloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(start = 12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        null
                    )
                }

                if(!scaffoldState.bottomSheetState.isVisible || fabScale != 0f) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .scale(fabScale)
                    ) {
                        Icon(
                            Icons.Rounded.KeyboardArrowUp,
                            null
                        )
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

fun Float.invert(): Float = 1 - this

fun Float.mapRange(min: Float, max: Float): Float = (this * (max - min)) + min
