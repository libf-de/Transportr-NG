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

package de.grobox.transportr.trips.search

import android.text.format.DateFormat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.map
import androidx.navigation.NavController
import de.grobox.transportr.R
import de.grobox.transportr.composables.CompactLocationGpsInput
import de.grobox.transportr.composables.CustomLargeTopAppBar
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.data.dto.KTrip
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.favorites.trips.FavoriteTripType
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.utils.DateUtils.isNow
import de.grobox.transportr.utils.DateUtils.isToday
import de.grobox.transportr.utils.DateUtils.millisToMinutes
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date
import java.util.EnumSet

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DirectionsScreen(
    viewModel: DirectionsViewModel,
    navController: NavController,
    from: WrapLocation?,
    via: WrapLocation?,
    to: WrapLocation?,
    specialLocation: FavoriteTripType?,
    search: Boolean,
    onSelectDepartureClicked: () -> Unit = {},
    onSelectDepartureLongClicked: () -> Unit = {},
    tripClicked: (KTrip) -> Unit = {},
    changeHome: () -> Unit = {},
    changeWork: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        if(specialLocation == FavoriteTripType.WORK || specialLocation == FavoriteTripType.HOME) {
            //TODO: Home/Work location
        } else {
            viewModel.setFromLocation(from)
            viewModel.setViaLocation(via)
            viewModel.setToLocation(to)
        }


        if(search) viewModel.search()
    }

    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var viaVisible by remember { mutableStateOf(false) }
    val isFavorite by viewModel.isFavTrip.observeAsState(false)

    val fromLocation by viewModel.fromLocation.observeAsState()
    val viaLocation by viewModel.viaLocation.observeAsState()
    val toLocation by viewModel.toLocation.observeAsState()

    val suggestions by viewModel.locationSuggestions.observeAsState()
    val suggestionsLoading by viewModel.suggestionsLoading.observeAsState(false)

    val departureCalendar by viewModel.lastQueryCalendar.observeAsState()
    val isDeparture by viewModel.isDeparture.observeAsState(true)

    var showProductSelector by remember { mutableStateOf(false) }
    val isProductsChanged by viewModel.products.map { KProduct.ALL != it }.observeAsState(false)

    val showTrips by viewModel.displayTrips.observeAsState(false)
    val trips by viewModel.trips.observeAsState()
    val favoriteTrips by viewModel.favoriteTrips.observeAsState()

    //160
    val expandedTopBarHeight = animateDpAsState(
        targetValue = if (viaVisible) 280.dp else 228.dp,
        animationSpec = tween(
            durationMillis = 1000,
        ), label = "expandedTopBarHeight"
    )

    val viaHeight = animateDpAsState(
        targetValue = if (viaVisible) 48.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 1000,
        ), label = "viaHeight"
    )

    ProductSelectorDialog(
        show = showProductSelector,
        onConfirmation = {
            viewModel.setProducts(it)
            showProductSelector = false
        },
        onDismissRequest = {
            showProductSelector = false
        },
        selectedProducts = viewModel.products.value?.toList()
    )


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CustomLargeTopAppBar(
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleFavTrip()
                    }) {
                        Icon(
                            painter = if(isFavorite)
                                painterResource(R.drawable.ic_action_star)
                                        else
                                painterResource(R.drawable.ic_action_star_empty),
                            contentDescription = stringResource(R.string.action_fav_trip)
                        )
                    }

                    IconButton(onClick = {
                        //TODO: Animate
                        viewModel.swapFromAndToLocations()
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_menu_swap_location),
                            contentDescription = stringResource(R.string.action_swap_locations)
                        )
                    }

                    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                        IconButton(
                            onClick = { viaVisible = !viaVisible },
                            enabled = scrollBehavior.state.collapsedFraction == 0f,
                            modifier = Modifier
                                .scale(scaleX = 1f - scrollBehavior.state.collapsedFraction, scaleY = 1f)
                                .width(48.dp * (1f - scrollBehavior.state.collapsedFraction))
                        ) {
                            Icon(
                                if (viaVisible)
                                    painterResource(R.drawable.ic_action_navigation_unfold_less)
                                else
                                    painterResource(R.drawable.ic_action_navigation_unfold_more),
                                contentDescription = stringResource(R.string.action_swap_locations)
                            )
                        }
                    }
                },
                title = {
                    Column() {
                        Text(
                            text = fromLocation?.fullName ?: "Kein Start",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = toLocation?.fullName ?: "Kein Ziel",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                expandedTitle = {
                    Column(
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        //fromLocation
                        CompactLocationGpsInput(
                            location = fromLocation,
                            suggestions = suggestions,
                            onAcceptSuggestion = {
                                viewModel.setFromLocation(it)
                                focusManager.clearFocus()
                            },
                            onFocusChange = {
                                if(!it) viewModel.resetSuggestions()
                            },
                            onValueChange = {
                                viewModel.suggestLocations(it)
                            },
                            isLoading = suggestionsLoading,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            label = stringResource(R.string.from)
                        )

                        if(viaVisible || viaHeight.value != 0.dp) {
                            // viaLocation
                            CompactLocationGpsInput(
                                location = viaLocation,
                                suggestions = suggestions,
                                onAcceptSuggestion = {
                                    viewModel.setViaLocation(it)
                                    focusManager.clearFocus()
                                },
                                onFocusChange = {
                                    if(!it) viewModel.resetSuggestions()
                                },
                                onValueChange = {
                                    viewModel.suggestLocations(it)
                                },
                                isLoading = suggestionsLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(viaHeight.value)
                                    .padding(vertical = 4.dp)
                                    .alpha(viaHeight.value / 48.dp),
                                label = stringResource(R.string.via)
                            )
                        }

                        // toLocation
                        CompactLocationGpsInput(
                            location = toLocation,
                            suggestions = suggestions,
                            onAcceptSuggestion = {
                                viewModel.setToLocation(it)
                                focusManager.clearFocus()
                            },
                            onFocusChange = {
                                if(!it) viewModel.resetSuggestions()
                            },
                            onValueChange = {
                                viewModel.suggestLocations(it)
                            },
                            isLoading = suggestionsLoading,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            label = stringResource(R.string.to)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .height(42.dp)
                                .fillMaxWidth()
                        ) {
                            DepartureTimeComposable(
                                departure = departureCalendar,
                                isDeparture = isDeparture,
                                modifier = Modifier
                                    .height(48.dp)
                                    .combinedClickable(
                                    onClick = {
                                        onSelectDepartureClicked()
                                    },
                                    onLongClick = {
                                        onSelectDepartureLongClicked()
                                    }
                                )
                            )

                            IconButton(onClick = { showProductSelector = true }) {
                                ProductSelectorIcon(
                                    changed = isProductsChanged,
                                    iconSize = 24.dp
                                )
                            }

                            IconButton(onClick = { viewModel.search() }) {
                                Icon(
                                    painterResource(R.drawable.ic_action_external_map),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                expandedHeight = expandedTopBarHeight.value
            )
        },
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .padding(pv)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if(showTrips) {
                TripList(
                    trips = trips?.toSet(),
                    tripClicked = { tripClicked(it) }
                )
            } else {
                FavoriteList(
                    favorites = favoriteTrips,
                    onFavoriteClicked = { item ->
                        when(item.type) {
                            FavoriteTripType.HOME -> item.to.let {
                                if(it == null) changeHome()
                                else viewModel.setToLocation(it)
                            }

                            FavoriteTripType.WORK -> item.to.let {
                                if(it == null) changeWork()
                                else viewModel.setToLocation(it)
                            }

                            FavoriteTripType.TRIP -> {
                                viewModel.setFromLocation(item.from)
                                viewModel.setViaLocation(item.via)
                                viewModel.setToLocation(item.to)
                                viewModel.search()
                            }

                            else -> throw IllegalArgumentException("item.type is null")
                        }
                    }
                )
            }

        }

    }
}

fun LazyListScope.TripList(
    trips: Set<KTrip>?,
    tripClicked: (KTrip) -> Unit
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
        items(trips.toList()) {
            TripListItem(
                trip = it
            ) {
                tripClicked(it)
            }
        }
    }
}

fun LazyListScope.FavoriteList(
    favorites: List<FavoriteTripItem>?,
    onFavoriteClicked: (FavoriteTripItem) -> Unit
) {
    items(favorites ?: emptyList()) {
        Text(
            text = it.from.fullName,
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    onFavoriteClicked(it)
                }
        )
    }
}

@Composable
fun ProductSelectorIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    changed: Boolean
) {
    val circleColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.height(IntrinsicSize.Min).width(IntrinsicSize.Min)) {
        Icon(
            painter = painterResource(R.drawable.product_bus),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.secondary
        )

        if(changed) {
            Canvas(
                modifier = Modifier
                    .size(iconSize / 3f)
                    .align(Alignment.TopEnd)
            ) {
                drawCircle(
                    color = circleColor,
                    radius = size.minDimension / 2
                )
            }
        }
    }
}


@Composable
fun ProductSelectorDialog(
    show: Boolean,
    selectedProducts: List<KProduct>?,
    onConfirmation: (EnumSet<KProduct>) -> Unit,
    onDismissRequest: () -> Unit
) {
    if(show) {
        var selected by remember { mutableStateOf(selectedProducts ?: KProduct.ALL) }

        AlertDialog(
            icon = {
                Icon(painterResource(R.drawable.product_bus), contentDescription = "Example Icon")
            },
            title = {
                Text(text = stringResource(R.string.select_products))
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KProduct.ALL.toList()) { product ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp).clickable {
                                selected = if (product in selected) {
                                    selected - product
                                } else {
                                    selected + product
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(product.getDrawableRes()),
                                contentDescription = null
                            )

                            Text(
                                text = stringResource(product.getNameRes()),
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            )

                            Checkbox(
                                checked = product in selected,
                                onCheckedChange = { checked ->
                                    selected = if (checked) {
                                        selected + product
                                    } else {
                                        selected - product
                                    }
                                }
                            )
                        }
                    }
                }
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val temp = EnumSet.noneOf(KProduct::class.java)
                        temp.addAll(selected)
                        onConfirmation(temp)
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun KProduct?.getNameRes(): Int = when(this) {
    KProduct.HIGH_SPEED_TRAIN -> R.string.product_high_speed_train
    KProduct.REGIONAL_TRAIN -> R.string.product_regional_train
    KProduct.SUBURBAN_TRAIN -> R.string.product_suburban_train
    KProduct.SUBWAY -> R.string.product_subway
    KProduct.TRAM -> R.string.product_tram
    KProduct.BUS -> R.string.product_bus
    KProduct.FERRY -> R.string.product_ferry
    KProduct.CABLECAR -> R.string.product_cablecar
    KProduct.ON_DEMAND -> R.string.product_on_demand
    else -> R.string.product_bus
}

private fun KProduct?.getDrawableRes(): Int = when (this) {
    KProduct.HIGH_SPEED_TRAIN -> R.drawable.product_high_speed_train
    KProduct.REGIONAL_TRAIN -> R.drawable.product_regional_train
    KProduct.SUBURBAN_TRAIN -> R.drawable.product_suburban_train
    KProduct.SUBWAY -> R.drawable.product_subway
    KProduct.TRAM -> R.drawable.product_tram
    KProduct.BUS -> R.drawable.product_bus
    KProduct.FERRY -> R.drawable.product_ferry
    KProduct.CABLECAR -> R.drawable.product_cablecar
    KProduct.ON_DEMAND -> R.drawable.product_on_demand
    null -> R.drawable.product_bus
    else -> R.drawable.ic_action_about
}

@Composable
fun DepartureTimeComposable(
    departure: Calendar?,
    isDeparture: Boolean,
    modifier: Modifier = Modifier,
    maxMins: Int = 99
) {
    val dvNow = stringResource(R.string.now_small)
    val dvInMinutes = stringResource(R.string.in_x_minutes)
    val dvAgoMinutes = stringResource(R.string.x_minutes_ago)
    val dvTimeFmt = DateFormat.getTimeFormat(LocalContext.current)
    val dvDateFmt = DateFormat.getDateFormat(LocalContext.current)

    var departureTimeStr by remember { mutableStateOf(dvNow) }

    var lastTime by remember { mutableStateOf(Date().time) }

    fun formatTime(date: Date?, withDate: Boolean): String {
        if (date == null) return ""

        val fTime = dvTimeFmt.format(date).let {
            if (dvTimeFmt.numberFormat.minimumIntegerDigits == 1
                && it.indexOf(':') == 1) {
                "0$it"
            } else it
        }

        val fDate = dvDateFmt.format(date)

        return if (withDate) "$fDate $fTime" else fTime
    }


    LaunchedEffect(lastTime) {
        departure?.let {
            when {
                isNow(it) -> {
                    departureTimeStr = dvNow
                }
                isToday(it) -> {
                    val difference = millisToMinutes(it.time.time - Date().time)
                    departureTimeStr = when {
                            difference !in -maxMins..maxMins -> formatTime(it.time, false)
                            difference == 0L -> dvNow
                            difference > 0 -> String.format(dvInMinutes, difference)
                            else -> String.format(dvAgoMinutes, difference * -1)
                    }
                }
                else -> {
                    departureTimeStr = formatTime(it.time, true)
                }
            }
        }

        lastTime = Date().time
        delay(1000 * 60)
    }


    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_time),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = if (isDeparture) stringResource(R.string.trip_dep) else stringResource(R.string.trip_arr),
            modifier = Modifier.padding(start = 6.dp, end = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = departureTimeStr,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}