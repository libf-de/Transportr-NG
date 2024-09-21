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

package de.libf.transportrng.ui.departures

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.utils.formatDuration
import de.libf.transportrng.ui.composables.CustomSmallTopAppBar
import de.libf.transportrng.ui.map.CompassMargins
import de.libf.transportrng.ui.map.MapViewComposable
import de.libf.transportrng.ui.map.provideMapState
import de.libf.transportrng.ui.trips.composables.LegListComposable
import de.libf.transportrng.ui.trips.invert
import de.libf.transportrng.ui.trips.mapRange
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_refresh
import transportr_ng.composeapp.generated.resources.action_share
import transportr_ng.composeapp.generated.resources.action_trip_calendar
import transportr_ng.composeapp.generated.resources.change_time
import transportr_ng.composeapp.generated.resources.drawer_departures
import transportr_ng.composeapp.generated.resources.error
import transportr_ng.composeapp.generated.resources.ic_action_calendar
import transportr_ng.composeapp.generated.resources.ic_action_navigation_refresh
import transportr_ng.composeapp.generated.resources.ic_action_social_share
import transportr_ng.composeapp.generated.resources.total_time
import transportr_ng.composeapp.generated.resources.try_again

const val MAX_DEPARTURES = 12
const val SAFETY_MARGIN = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesScreen(
    viewModel: DeparturesViewModel,
    navController: NavController,
    location: WrapLocation,
) {
    val viewState by viewModel.departuresState.collectAsStateWithLifecycle()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            skipHiddenState = false
        )
    )
    val mapState = remember { provideMapState() }

    val fabScale by animateFloatAsState(
        animationSpec = tween(300),
        targetValue = if(scaffoldState.bottomSheetState.isVisible) 0f else 1f
    )
    val expanding by animateFloatAsState(
        animationSpec = tween(300),
        targetValue = if(scaffoldState.bottomSheetState.targetValue == SheetValue.PartiallyExpanded) 0f else 1f
    )

    val screenHeight = remember { mutableStateOf(300.dp) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(location) {
        viewModel.queryDepartures(location)

        mapState.animateTo(location.latLng, zoom = 14)
    }

    Layout(
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        DeparturesComposable(
                            viewState = viewState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            viewModel.queryDepartures(location)
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
                                Column {
                                    Text(
                                        text = location._getName(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text(
                                        text = stringResource(Res.string.drawer_departures),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                            },
                            height = expanding.mapRange(48f, 64f).dp,
                            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false }),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            actions = {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                    IconButton(
                                        onClick = { viewModel.queryDepartures(location) },
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
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = stringResource(Res.string.change_time)
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

@Composable
private fun DeparturesComposable(
    viewState: DeparturesState,
    modifier: Modifier,
    reloadData: () -> Unit
) {
    when(viewState) {
        is DeparturesState.Initial -> {}
        is DeparturesState.Loading -> {
            Box(
                modifier = modifier,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        is DeparturesState.Error -> {
            Box(modifier = modifier) {
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
                            reloadData()
                        }
                    ) {
                        Text(text = stringResource(Res.string.try_again))
                    }
                }
            }
        }
        is DeparturesState.Success -> {
            LazyColumn(modifier = modifier) {
                items(viewState.departures) {
                    Text(it.toString())
                }
            }
        }
    }
}