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

package de.libf.transportrng.ui.trips.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.libf.ptek.dto.Trip
import de.libf.transportrng.ui.directions.DirectionsViewModel

@Composable
fun TripsComposable(
    viewModel: DirectionsViewModel,
    tripClicked: (trip: Trip) -> Unit
) {
    val topSwipeEnabled by viewModel.topSwipeEnabled.collectAsStateWithLifecycle(false)
    val queryMoreState by viewModel.queryMoreState.collectAsStateWithLifecycle()
    val trips by viewModel.trips.collectAsStateWithLifecycle(null)
    val queryError by viewModel.queryError.collectAsStateWithLifecycle(null)
    val queryPTEError by viewModel.queryPTEError.collectAsStateWithLifecycle(null)
    val queryMoreError by viewModel.queryMoreError.collectAsStateWithLifecycle(null)

    if(!queryError.isNullOrBlank()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = queryError ?: "No error message :(")
        }
    } else if(trips == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trips?.toList() ?: emptyList()) {
                TripPreviewComposable(
                    trip = it
                ) {
                    tripClicked(it)
                }
            }
        }

    }}