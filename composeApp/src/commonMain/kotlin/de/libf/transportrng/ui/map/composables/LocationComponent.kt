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

package de.libf.transportrng.ui.map.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.libf.ptek.dto.Line
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.locations.getCoordName
import de.libf.transportrng.ui.transport.composables.ProductComposable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_copy
import transportr_ng.composeapp.generated.resources.action_share
import transportr_ng.composeapp.generated.resources.drawer_departures
import transportr_ng.composeapp.generated.resources.drawer_nearby_stations
import transportr_ng.composeapp.generated.resources.from_here
import transportr_ng.composeapp.generated.resources.ic_action_content_content_copy
import transportr_ng.composeapp.generated.resources.ic_action_departures
import transportr_ng.composeapp.generated.resources.ic_action_social_share
import transportr_ng.composeapp.generated.resources.ic_location
import transportr_ng.composeapp.generated.resources.ic_menu_directions
import transportr_ng.composeapp.generated.resources.ic_more_horiz
import transportr_ng.composeapp.generated.resources.ic_nearby_stations
import transportr_ng.composeapp.generated.resources.more

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationComponent(
    location: WrapLocation?,
    lines: List<Line>?,
    modifier: Modifier = Modifier,
    fromHereClicked: () -> Unit = {},
    copyClicked: () -> Unit = {},
    departuresClicked: () -> Unit = {},
    nearbyStationsClicked: () -> Unit = {},
    shareClicked: () -> Unit = {}

) {
    var locationInfo by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(location) {
        val locationInfoStr = StringBuilder()
        location?.location?.place.takeIf { !it.isNullOrEmpty() }.let {
            locationInfoStr.append(it)
        }
        location?.location.takeIf { it?.hasCoords == true }?.let {
            if (locationInfoStr.isNotEmpty()) locationInfoStr.append(", ")
            locationInfoStr.append(it.getCoordName())
        }
        locationInfo = locationInfoStr.toString()
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(location?.drawableInt ?: Res.drawable.ic_location),
                contentDescription = null
            )

            Text(
                text = location?._getName() ?: "",
                style = MaterialTheme.typography.displayMedium
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
        ) {
            lines?.forEach {
                ProductComposable(line = it)
            }
        }

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = locationInfo,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(
                    onClick = { showMoreMenu = true }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_more_horiz),
                        contentDescription = stringResource(Res.string.more),
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    properties = PopupProperties(),
                    onDismissRequest = { showMoreMenu = false}
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(Res.string.from_here))
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_menu_directions),
                                contentDescription = null,
                            )
                        },
                        onClick = { fromHereClicked() }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(stringResource(Res.string.action_copy))
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_action_content_content_copy),
                                contentDescription = null,
                            )
                        },
                        onClick = { copyClicked() }
                    )

                }
            }

        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { departuresClicked() }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_action_departures),
                    contentDescription = stringResource(Res.string.drawer_departures),
                )

                Text(
                    text = stringResource(Res.string.drawer_departures)
                )
            }

            FilledTonalButton(
                onClick = { nearbyStationsClicked() }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_nearby_stations),
                    contentDescription = stringResource(Res.string.drawer_nearby_stations),
                )

                Text(
                    text = stringResource(Res.string.drawer_nearby_stations)
                )
            }

            FilledTonalButton(
                onClick = { shareClicked() }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_action_social_share),
                    contentDescription = stringResource(Res.string.action_share),
                )

                Text(
                    text = stringResource(Res.string.action_share)
                )
            }
        }
    }
}