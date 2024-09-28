package de.libf.transportrng.ui.map.sheets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.libf.transportrng.MapRoutes
import de.libf.transportrng.Routes
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.departures.composables.DepartureComposable
import de.libf.transportrng.ui.favorites.SavedSearchesActions
import de.libf.transportrng.ui.favorites.SavedSearchesComponent
import de.libf.transportrng.ui.map.composables.LocationComponent
import de.libf.transportrng.ui.transport.composables.ProductComposable
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_share
import transportr_ng.composeapp.generated.resources.drawer_departures
import transportr_ng.composeapp.generated.resources.ic_action_departures
import transportr_ng.composeapp.generated.resources.ic_action_social_share

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationDetailSheetContent(
    viewModel: LocationDetailSheetViewModel,
    location: WrapLocation,
    rootNavController: NavController,
    mapNavController: NavController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(location) {
        viewModel.load(location)
    }


    Crossfade(
        targetState = uiState,
        label = "savedSearchesSheetCrossfade"
    ) { state ->
        when(state) {
            is LocationDetailSheetState.Loading -> {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.fillMaxWidth().height(168.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                LocationComponent(
                    location = location,
                    modifier = modifier
                ) {
                    when(state) {
                        is LocationDetailSheetState.Success -> {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                            ) {
                                state.departures.flatMap { it.lines }.forEach {
                                    ProductComposable(line = it.line)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
//                                            change.consume()
                                            scope.launch {
                                                listState.scrollBy(-dragAmount.y)
                                            }
                                        }
                                    },
                            ) {
                                items(state.departures
                                    .flatMap { it.departures }
                                    .sortedBy { it.predictedTime }
                                ) {
                                    DepartureComposable(
                                        departure = it,
                                        modifier = modifier.fillMaxWidth()
                                    )
                                }
                            }

//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                FilledTonalButton(
//                                    onClick = {  }
//                                ) {
//                                    Icon(
//                                        painter = painterResource(Res.drawable.ic_action_departures),
//                                        contentDescription = stringResource(Res.string.drawer_departures),
//                                    )
//
//                                    Text(
//                                        text = stringResource(Res.string.drawer_departures)
//                                    )
//                                }

//                                FilledTonalButton(
//                                    onClick = {  }
//                                ) {
//                                    Icon(
//                                        painter = painterResource(Res.drawable.ic_nearby_stations),
//                                        contentDescription = stringResource(Res.string.drawer_nearby_stations),
//                                    )
//
//                                    Text(
//                                        text = stringResource(Res.string.drawer_nearby_stations)
//                                    )
//                                }

//                                FilledTonalButton(
//                                    onClick = { /* TODO */ }
//                                ) {
//                                    Icon(
//                                        painter = painterResource(Res.drawable.ic_action_social_share),
//                                        contentDescription = stringResource(Res.string.action_share),
//                                    )
//
//                                    Text(
//                                        text = stringResource(Res.string.action_share)
//                                    )
//                                }
//                            }
                        }
                        is LocationDetailSheetState.DeparturesLoading -> {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is LocationDetailSheetState.Error -> {
                            Text(text = state.message)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

