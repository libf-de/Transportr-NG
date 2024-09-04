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

package de.grobox.transportr.trips.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.composables.CompassMargins
import de.grobox.transportr.composables.CustomSmallTopAppBar
import de.grobox.transportr.composables.MapViewComposable
import de.grobox.transportr.composables.MapViewState
import de.grobox.transportr.utils.DateUtils.formatDuration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailComposable(
    viewModel: TripDetailViewModel,
    setBarColor: (statusBar: Color, navBar: Color) -> Unit,
    onBackPressed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(true) }
    val mapState = remember { MapViewState() }

    val expanding by animateFloatAsState(
        animationSpec = tween(300),
        targetValue = if(sheetState.targetValue == SheetValue.PartiallyExpanded) 0f else 1f
    )

    val fabScale by animateFloatAsState(
        animationSpec = tween(300),
        targetValue = if(sheetState.isVisible) 0f else 1f
    )

    val appBarColor by animateColorAsState(
        animationSpec = tween(300),
        targetValue = MaterialTheme.colorScheme.let {
            if(sheetState.targetValue == SheetValue.PartiallyExpanded)
                it.surface
            else
                it.primaryContainer
        }
    )

    var primaryColor = MaterialTheme.colorScheme.primaryContainer
    var surfaceColor = MaterialTheme.colorScheme.surface
    LaunchedEffect(sheetState.targetValue) {
        if(sheetState.targetValue == SheetValue.Expanded) {
            setBarColor(primaryColor, surfaceColor)
        } else {
            setBarColor(Color.Transparent, Color.Transparent)
        }
    }

    val trip by viewModel.getTrip().observeAsState()
    val zoomLocation by viewModel.getZoomLocation().observeAsState()
    val zoomLeg by viewModel.getZoomLeg().observeAsState()

    LaunchedEffect(trip) {
        mapState.drawTrip(trip, viewModel.isFreshStart.value ?: true)
        viewModel.isFreshStart.value = false
    }

    LaunchedEffect(zoomLocation) {
        mapState.animateTo(zoomLocation, 16)
    }

    LaunchedEffect(zoomLeg) {
        mapState.animateToBounds(zoomLeg)
    }

    Scaffold(
        topBar = {
            SmallFloatingActionButton(
                onClick = { onBackPressed() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars).padding(start = 12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    null
                )
            }
        },
        floatingActionButton = {
            if(!sheetState.isVisible || fabScale != 0f) {
                FloatingActionButton(
                    onClick = {
                        showBottomSheet = true
                        //scope.launch {
                            //sheetState.partialExpand()
                        //}
                    },
                    modifier = Modifier.scale(fabScale)
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowUp,
                        null
                    )
                }
            }
        }
    ) { contentPadding ->
        MapViewComposable(
            mapViewState = mapState,
            compassMargins = CompassMargins(top = 24.dp),
            isHalfHeight = true
        )


        if(showBottomSheet) {
            ModalBottomSheet(
                windowInsets = WindowInsets.systemBars,
                //modifier = Modifier.padding(contentPadding),
                onDismissRequest = {
                    showBottomSheet = false
                },
                scrimColor = Color.Transparent,
                sheetState = sheetState,
                shape = RoundedCornerShape(
                    expanding.invert().mapRange(0f, 28f).dp
                ),
                dragHandle = {
                    CustomSmallTopAppBar(
                        navigationIcon = {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                IconButton(
                                    onClick = { scope.launch { sheetState.partialExpand() } },
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
                                text = trip?.duration?.let { stringResource(R.string.total_time, formatDuration(it)) } ?: "",
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
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_action_navigation_refresh),
                                        contentDescription = stringResource(R.string.action_refresh)
                                    )
                                }

                                IconButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_action_social_share),
                                        contentDescription = stringResource(R.string.action_share)
                                    )
                                }

                                IconButton(
                                    onClick = { /* TODO */ },
                                    modifier = Modifier.size(expanding.mapRange(0f, 40f).dp).scale(expanding)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_action_calendar),
                                        contentDescription = stringResource(R.string.action_trip_calendar)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                    )
                }
            ) {
                Column(Modifier.fillMaxSize().padding(vertical = 8.dp)) {
                    LegListComposable(
                        legs = trip?.legs ?: emptyList(),
                        showLineNames = true,
                        modifier = Modifier.fillMaxSize()
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private fun Float.invert(): Float = 1 - this

private fun Float.mapRange(min: Float, max: Float): Float = (this * (max - min)) + min
