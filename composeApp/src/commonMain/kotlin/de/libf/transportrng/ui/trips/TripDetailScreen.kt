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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.grobox.transportr.ui.trips.TripQuery
import de.libf.ptek.dto.Stop
import de.libf.ptek.dto.Trip
import de.libf.transportrng.Routes
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.map.provideMapState
import de.libf.transportrng.ui.trips.composables.AddToCalendarDialog
import de.libf.transportrng.ui.trips.composables.StopActions
import de.libf.transportrng.ui.trips.composables.StopActionsDialog
import de.libf.transportrng.ui.trips.composables.TripDetailViewComposable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.cool
import transportr_ng.composeapp.generated.resources.trip_added_to_calendar
import transportr_ng.composeapp.generated.resources.trip_added_to_calendar_failed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    tripId: String,
    tripQuery: TripQuery,
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

    val state: TripDetailState by viewModel.uiState.collectAsStateWithLifecycle()

    var primaryColor = MaterialTheme.colorScheme.primaryContainer
    var surfaceColor = MaterialTheme.colorScheme.surface
    LaunchedEffect(scaffoldState.bottomSheetState.targetValue) {
        if(scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
            setBarColor(primaryColor, surfaceColor)
        } else {
            setBarColor(Color.Transparent, Color.Transparent)
        }
    }

//    val trip by viewModel.trip.collectAsStateWithLifecycle()
//    val zoomLocation by viewModel.zoomLocation.collectAsStateWithLifecycle(null)
//    val zoomLeg by viewModel.zoomLeg.collectAsStateWithLifecycle(null)
    val showLineNames by viewModel.transportNetwork.map { it?.hasGoodLineNames() ?: false }.collectAsStateWithLifecycle(false)

    // TODO: Show reload error as Snackbar
//    val tripReloadError by viewModel.tripReloadError.collectAsStateWithLifecycle(null)

    LaunchedEffect(tripId, tripQuery) {
        viewModel.getTripById(tripId, tripQuery)
    }

    LaunchedEffect(state) {
        when(val state = state) {
            is TripDetailState.DisplayingWholeTrip ->
                mapState.drawTrip(state.trip, true)
            is TripDetailState.DisplayingLeg -> {
                mapState.drawTrip(state.trip, false)
                mapState.animateToBounds(state.area)
            }
            is TripDetailState.DisplayingLocation -> {
                mapState.drawTrip(state.trip, false)
                mapState.animateTo(state.point, 16)
            }

            is TripDetailState.ReloadFailed -> {
                mapState.drawTrip(state.trip, false)
                scaffoldState.snackbarHostState.showSnackbar(
                    message = state.error.message ?: "Failed to reload trip :(",
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }



    var showStationActions by remember { mutableStateOf(false) }
    val stationAction = remember { mutableStateOf<Stop?>(null) }

    val addTripToCalendarState: MutableState<Trip?> = remember { mutableStateOf(null) }

    val hapticFeedback = LocalHapticFeedback.current

    AddToCalendarDialog(
        tripState = addTripToCalendarState,
        onConfirm = {
            viewModel.addTripToCalendar(it) {
                scope.launch {
                    if(it == null)
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = getString(Res.string.trip_added_to_calendar),
                            actionLabel = getString(Res.string.cool),
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    else
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = getString(Res.string.trip_added_to_calendar_failed, it),
                            duration = SnackbarDuration.Long
                        )
                }

            }
        }
    )

//    AnimatedVisibility(showStationActions) {
    StopActionsDialog(
        selectedStop = stationAction,
        actions = StopActions(
            showStationOnExternalMap = { stop ->
                viewModel.showOnExternalMap(
                    WrapLocation(stop.location)
                )
            },
            findDepartures = { stop -> navController.navigate(Routes.Departures(
                WrapLocation(stop.location)
            ))},
            findConnections = { stop -> navController.navigate(Routes.Directions(
                from = WrapLocation(stop.location),
            ))},
            continueJourneyLater = { stop -> navController.navigate(Routes.Directions(
                from = WrapLocation(stop.location),
                to = state.getTripOrNull()?.to?.let(::WrapLocation),
                time = (stop.predictedArrivalTime ?: stop.plannedArrivalTime)?.plus(300000) ?: -1L
            ))}

        )
    )

    Crossfade(
        targetState = state,
        modifier = Modifier.fillMaxSize()
    ) { tgt ->
        when (tgt) {
            is TripDetailState.Error -> {
                Text(tgt.error.message ?: "Error")
            }

            is TripDetailState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is TripDetailState.DisplayingWholeTrip,
            is TripDetailState.DisplayingLeg,
            is TripDetailState.DisplayingLocation,
            is TripDetailState.ReloadFailed -> {
                state.getTripOrNull()!!.let { trip ->
                    TripDetailViewComposable(
                        scaffoldState = scaffoldState,
                        mapState = mapState,
                        onAddToCalendarClicked = { addTripToCalendarState.value = trip },
                        onBackClicked = { navController.popBackStack() },
                        onReloadTripClicked = { viewModel.reloadTrip(tripQuery) },
                        onShareTripClicked = { viewModel.shareTrip(trip) },
                        onStopLongClick = {
                            stationAction.value = it
                            showStationActions = true
                        },
                        scope = scope,
                        showLineNames = showLineNames,
                        trip = trip,
                    )
                }

            }
        }
    }
}

fun Float.invert(): Float = 1 - this

fun Float.mapRange(min: Float, max: Float): Float = (this * (max - min)) + min
