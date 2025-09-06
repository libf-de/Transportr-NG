package de.libf.transportrng.ui.trips.composables

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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Stop
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.utils.formatDuration
import de.libf.transportrng.ui.composables.CustomSmallTopAppBar
import de.libf.transportrng.ui.map.CompassMargins
import de.libf.transportrng.ui.map.MapViewComposable
import de.libf.transportrng.ui.map.MapViewStateInterface
import de.libf.transportrng.ui.trips.invert
import de.libf.transportrng.ui.trips.mapRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_refresh
import transportr_ng.composeapp.generated.resources.action_share
import transportr_ng.composeapp.generated.resources.action_trip_calendar
import transportr_ng.composeapp.generated.resources.ic_action_calendar
import transportr_ng.composeapp.generated.resources.ic_action_navigation_refresh
import transportr_ng.composeapp.generated.resources.ic_action_social_share
import transportr_ng.composeapp.generated.resources.total_time

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailViewComposable(
    scaffoldState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    trip: Trip,
    showLineNames: Boolean,
    mapState: MapViewStateInterface,
    onStopLongClick: (Stop) -> Unit,
    onReloadTripClicked: () -> Unit,
    onAddToCalendarClicked: () -> Unit,
    onShareTripClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val screenHeight = remember { mutableStateOf(300.dp) }

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
                                legs = trip.legs,
                                showLineNames = showLineNames,
                                modifier = Modifier.fillMaxSize(),
                                onLegClick = { leg, location ->
                                    scope.launch {
                                        mapState.animateTo(
                                            location.coord?.let { LatLng(it.lat, it.lon) },
                                            14
                                        )
                                    }
                                },

                                onStopLongClick = onStopLongClick
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
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
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
                                    text = stringResource(
                                        resource = Res.string.total_time,
                                        trip
                                            .duration
                                            .formatDuration() ?: "??:??"),
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
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                    IconButton(
                                        onClick = onReloadTripClicked,
                                        modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_action_navigation_refresh),
                                            contentDescription = stringResource(Res.string.action_refresh)
                                        )
                                    }

                                    IconButton(
                                        onClick = onShareTripClicked,
                                        modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_action_social_share),
                                            contentDescription = stringResource(Res.string.action_share)
                                        )
                                    }

                                    IconButton(
                                        onClick = onAddToCalendarClicked,
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
                    onClick = onBackClicked,
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