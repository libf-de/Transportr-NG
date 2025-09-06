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

package de.libf.transportrng.ui.directions.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.trips.TripsRepository
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.earlier
import transportr_ng.composeapp.generated.resources.later
import transportr_ng.composeapp.generated.resources.time_picker_next_day

@Composable
fun SearchResultComponent(
    modifier: Modifier,
    trips: Set<Trip>?,
    tripClicked: (Trip) -> Unit,
    queryMoreState: TripsRepository.QueryMoreState,
    onLoadMoreRequested: (Boolean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding
    ) {
        if(trips == null) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            if(queryMoreState == TripsRepository.QueryMoreState.BOTH ||
                queryMoreState == TripsRepository.QueryMoreState.EARLIER) {

                item {
                    TextButton(
                        onClick = { onLoadMoreRequested(false) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = null
                        )
                        Text(stringResource(Res.string.earlier))
                        Spacer(Modifier.weight(1f))
                    }
                }

            }

            itemsIndexed(trips.toList(), key = { _, trip -> trip.id }) { _, it ->
                NextTripPreviewComposable(
                    trip = it,
                ) {
                    tripClicked(it)
                }
            }

            if(queryMoreState == TripsRepository.QueryMoreState.BOTH ||
                queryMoreState == TripsRepository.QueryMoreState.LATER) {

                item {
                    TextButton(
                        onClick = { onLoadMoreRequested(true) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(stringResource(Res.string.later))
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = null
                        )
                    }
                }

            }

        }
    }
}