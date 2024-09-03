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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import de.grobox.transportr.composables.CompassMargins
import de.grobox.transportr.composables.CustomSmallTopAppBar
import de.grobox.transportr.composables.MapViewComposable
import de.grobox.transportr.composables.MapViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailComposable(
    viewModel: TripDetailViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by remember { mutableStateOf(true) }
    val mapState = remember { MapViewState() }

    val expanding by animateFloatAsState(
        animationSpec = tween(100),
        targetValue = if(sheetState.targetValue == SheetValue.PartiallyExpanded) 1f else 0f
    )

    val fabScale by animateFloatAsState(
        animationSpec = tween(100),
        targetValue = if(sheetState.isVisible) 0f else 1f
    )

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
        floatingActionButton = {
            if(!sheetState.isVisible || fabScale != 0f) {
                FloatingActionButton(
                    onClick = {
                        showBottomSheet = true
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
            compassMargins = CompassMargins(top = 24.dp)
        )


        if(showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                shape = RoundedCornerShape(
                    expanding.mapRange(0f, 28f).dp
                ),
//                dragHandle = {
//                    CustomSmallTopAppBar(
//                        title = {
//                            Text(text = "Hello")
//                        },
//                        height = expanding.invert().mapRange(48f, 64f).dp,
//                        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { false }),
//                        colors = TopAppBarDefaults.topAppBarColors(
//                            containerColor = MaterialTheme.colorScheme.primaryContainer,
//                            titleContentColor = MaterialTheme.colorScheme.primary,
//                        ),
//                        modifier = Modifier
//                    )
//                }
            ) {
                Column(Modifier.fillMaxSize()) {
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
