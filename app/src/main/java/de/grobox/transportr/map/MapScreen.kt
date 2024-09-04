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

package de.grobox.transportr.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.google.common.base.Strings
import de.grobox.transportr.R
import de.grobox.transportr.composables.BaseLocationGpsInput
import de.grobox.transportr.composables.CompassMargins
import de.grobox.transportr.composables.MapViewComposable
import de.grobox.transportr.composables.MapViewState
import de.grobox.transportr.data.searches.StoredSearch
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.trips.detail.TripDetailViewModel
import de.grobox.transportr.trips.search.ProductViewComposable
import de.grobox.transportr.trips.search.WalkView
import de.grobox.transportr.utils.TransportrUtils
import de.grobox.transportr.utils.TransportrUtils.getCoordName
import de.grobox.transportr.utils.hasLocation
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.Trip.Public
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    val searchSuggestions by viewModel.locationSuggestions.observeAsState()
    val focusManager = LocalFocusManager.current
    val mapState = remember { MapViewState() }

    val sheetContentState by viewModel.sheetContentState.observeAsState()

    val sheetState = rememberStandardBottomSheetState(
//        confirmValueChange = { it != SheetValue.Expanded },
        initialValue = SheetValue.Expanded,
//        skipHiddenState = false
    )
    val scafState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    var bottomSheetVisible by remember { mutableStateOf(true) }

    val favoriteTrips by viewModel.favoriteTrips.observeAsState(emptyList())

    Box(Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            sheetContent = {
                when(val state = sheetContentState) {
                    is BottomSheetContentState.Location -> {
                        LocationComponent(
                            location = state.loc,
                            lines = state.lines,
                            modifier = Modifier
                                .heightIn(max = (LocalConfiguration.current.screenHeightDp / 2).dp)
                        )
                    }

                    is BottomSheetContentState.SavedSearches -> {
                        SavedSearchesComponent(
                            items = favoriteTrips,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                                .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
                                .heightIn(max = (LocalConfiguration.current.screenHeightDp / 2).dp)
                        )
                    }

                    else -> {}
                }

                Spacer(modifier = Modifier.height(200.dp))

                //            Spacer(modifier = Modifier.weight(1f))
                // TODO
            },
            scaffoldState = scafState,
        ) { contentPadding ->
            MapViewComposable(
                mapViewState = mapState,
                compassMargins = CompassMargins(top = 72.dp),
                showAttribution = false,
                showLogo = false,
                rotateGestures = true
            )
        }

        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp).height(48.dp).fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_black),
                        contentDescription = stringResource(R.string.material_drawer_open),
                    )
                }

                BaseLocationGpsInput(
                    location = null,
                    onAcceptSuggestion = {
                        /* TODO */
                    },
                    onValueChange = {
                        viewModel.suggestLocations(it)
                    },
                    suggestions = searchSuggestions,
                    modifier = Modifier.height(48.dp),
                    placeholder = stringResource(R.string.search_hint)
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        FloatingActionButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val sheetOffset = scafState.bottomSheetState.requireOffset()
                    val fabSize = 56.dp // Assuming standard FAB size

                    //println(-(sheetOffset.roundToInt() + fabSize.roundToPx() / 2))

                    IntOffset(
                        x = -16.dp.roundToPx(),
                        y = (sheetOffset.roundToInt() - (fabSize.roundToPx() / 2))
                    )
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu_directions),
                contentDescription = stringResource(R.string.directions),
            )
        }
    }
}

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
        location?.location.takeIf { it?.hasCoord() == true }?.let {
            if (locationInfoStr.isNotEmpty()) locationInfoStr.append(", ")
            locationInfoStr.append(getCoordName(it))
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
                painter = painterResource(location?.drawable ?: R.drawable.ic_location),
                contentDescription = null
            )

            Text(
                text = location?.getName() ?: "",
                style = MaterialTheme.typography.displayMedium
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
        ) {
            lines?.forEach {
                ProductViewComposable(
                    line = it,
                    drawableForProductGetter = TransportrUtils::getDrawableForProduct
                )
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
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.more),
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    properties = PopupProperties(),
                    onDismissRequest = { showMoreMenu = false}
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.from_here))
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu_directions),
                                contentDescription = null,
                            )
                        },
                        onClick = { fromHereClicked() }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.action_copy))
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_action_content_content_copy),
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
                    painter = painterResource(R.drawable.ic_action_departures),
                    contentDescription = stringResource(R.string.drawer_departures),
                )

                Text(
                    text = stringResource(R.string.drawer_departures)
                )
            }

            FilledTonalButton(
                onClick = { nearbyStationsClicked() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nearby_stations),
                    contentDescription = stringResource(R.string.drawer_nearby_stations),
                )

                Text(
                    text = stringResource(R.string.drawer_nearby_stations)
                )
            }

            FilledTonalButton(
                onClick = { shareClicked() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_action_social_share),
                    contentDescription = stringResource(R.string.action_share),
                )

                Text(
                    text = stringResource(R.string.action_share)
                )
            }
        }
    }
}

@Composable
fun SavedSearchesComponent(
    items: List<FavoriteTripItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(items) {
            SavedSearchItem(
                itm = it,
                onItemClicked = { },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_swap_locations)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu_swap_location),
                            contentDescription = null,
                        )
                    },
                    onClick = { }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_set_locations)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_mode_edit),
                            contentDescription = null,
                        )
                    },
                    onClick = { }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_fav_trip)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_action_star),
                            contentDescription = null,
                        )
                    },
                    onClick = { }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_add_shortcut)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_shortcut),
                            contentDescription = null,
                        )
                    },
                    onClick = { }
                )

                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_trip_delete)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = null,
                        )
                    },
                    onClick = { }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSearchItem(
    itm: FavoriteTripItem,
    modifier: Modifier = Modifier,
    onItemClicked: () -> Unit = {},
    menuContent: @Composable ColumnScope.() -> Unit
) {
    val sketchColor = MaterialTheme.colorScheme.onSurfaceVariant
    val circleRadius = 4.dp
    val topMargin = 3.dp

    val hasVia = itm.via != null
    val totalHeight = (topMargin + (topMargin + circleRadius) * 4) + if(hasVia) (topMargin + (topMargin + circleRadius) * 2) else 0.dp

    var menuOpen by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.clickable {
            onItemClicked()
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp).height(IntrinsicSize.Min)
        ) {
            Icon(
                painter = painterResource(if(itm.isFavorite) R.drawable.ic_action_star else R.drawable.ic_time),
//                painter = painterResource(R.drawable.ic_time),
                contentDescription = null,
                modifier = Modifier.width((topMargin + circleRadius) * 3).fillMaxHeight().wrapContentHeight(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier.padding(start = 3.dp).weight(1f)
            ) {

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier
                            .padding(top = topMargin)
                            .width(20.dp)
                            .fillMaxHeight()
                    ) {
                        drawCircle(
                            color = sketchColor,
                            radius = circleRadius.toPx(),
                            center = Offset(size.width / 2, circleRadius.toPx())
                        )

                        drawLine(
                            color = sketchColor,
                            start = Offset(size.width / 2, circleRadius.toPx()),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = circleRadius.toPx() / 3
                        )
                    }

                    Text(
                        text = itm.from?.getName() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(bottom = topMargin / 2)
                            .height((topMargin + circleRadius) * 2)
                            .wrapContentHeight(Alignment.CenterVertically)
                            .weight(1f)
                    )
                }

                if(hasVia) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight()
                        ) {
                            drawCircle(
                                color = sketchColor,
                                radius = circleRadius.toPx(),
                                center = Offset(size.width / 2, size.height / 2)
                            )

                            drawLine(
                                color = sketchColor,
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = circleRadius.toPx() / 3
                            )
                        }

                        Text(
                            text = itm.via?.getName() ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(vertical = topMargin / 2)
                                .height((topMargin + circleRadius) * 2)
                                .wrapContentHeight(Alignment.CenterVertically)
                                .weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier
                            .padding(bottom = topMargin)
                            .width(20.dp)
                            .fillMaxHeight()
                    ) {
                        drawCircle(
                            color = sketchColor,
                            radius = circleRadius.toPx(),
                            center = Offset(size.width / 2, size.height - circleRadius.toPx())
                        )

                        drawLine(
                            color = sketchColor,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height - circleRadius.toPx()),
                            strokeWidth = circleRadius.toPx() / 3
                        )
                    }

                    Text(
                        text = itm.to?.getName() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = topMargin / 2)
                            .height((topMargin + circleRadius) * 2)
                            .wrapContentHeight(Alignment.CenterVertically)
                            .weight(1f)
                    )
                }
            }

            Box {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.height(totalHeight)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.more),
                            modifier = Modifier.size((topMargin + circleRadius) * 4)
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuOpen,
                    properties = PopupProperties(),
                    onDismissRequest = { menuOpen = false }
                ) {
                    menuContent()
                }
            }

        }
    }
}