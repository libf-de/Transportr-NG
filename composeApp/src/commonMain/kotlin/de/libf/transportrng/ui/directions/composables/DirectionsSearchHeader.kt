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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.ui.composables.CompactLocationGpsInput
import de.libf.transportrng.ui.directions.ProductSelectorIcon
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.from
import transportr_ng.composeapp.generated.resources.to
import transportr_ng.composeapp.generated.resources.via

@Composable
fun DirectionsSearchHeader(
    fromLocation: WrapLocation?,
    setFromLocation: (WrapLocation?) -> Unit,

    viaVisible: Boolean,
    viaLocation: WrapLocation?,
    setViaLocation: (WrapLocation?) -> Unit,

    toLocation: WrapLocation?,
    setToLocation: (WrapLocation?) -> Unit,

    suggestions: Set<WrapLocation>?,
    requestSuggestions: (String) -> Unit,
    requestResetSuggestions: () -> Unit,
    suggestionsLoading: Boolean,
    gpsLoading: Boolean,

    bottomRow: @Composable RowScope.() -> Unit
) {
    val focusManager = LocalFocusManager.current
    val viaHeight = animateDpAsState(
        targetValue = if (viaVisible) 48.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 1000,
        ), label = "viaHeight"
    )

    Column(
        modifier = Modifier.padding(end = 8.dp)
    ) {
        //fromLocation
        CompactLocationGpsInput(
            location = fromLocation,
            suggestions = suggestions,
            onAcceptSuggestion = {
                setFromLocation(it)
                focusManager.clearFocus()
            },
            onFocusChange = {
                if(!it) requestResetSuggestions()
            },
            onValueChange = {
                requestSuggestions(it)
            },
            isLoading = suggestionsLoading,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            label = stringResource(Res.string.from),
            isGpsLoading = gpsLoading
        )

        if(viaVisible || viaHeight.value != 0.dp) {
            // viaLocation
            CompactLocationGpsInput(
                location = viaLocation,
                suggestions = suggestions,
                onAcceptSuggestion = {
                    setViaLocation(it)
                    focusManager.clearFocus()
                },
                onFocusChange = {
                    if(!it) requestResetSuggestions()
                },
                onValueChange = {
                    requestSuggestions(it)
                },
                isLoading = suggestionsLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viaHeight.value)
                    .padding(vertical = 4.dp)
                    .alpha(viaHeight.value / 48.dp),
                label = stringResource(Res.string.via)
            )
        }

        // toLocation
        CompactLocationGpsInput(
            location = toLocation,
            suggestions = suggestions,
            onAcceptSuggestion = {
                setToLocation(it)
                focusManager.clearFocus()
            },
            onFocusChange = {
                if(!it) requestResetSuggestions()
            },
            onValueChange = {
                requestSuggestions(it)
            },
            isLoading = suggestionsLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            label = stringResource(Res.string.to)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .height(42.dp)
                .fillMaxWidth()
        ) {
            bottomRow()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.DirectionsSearchHeaderBtmRow(
    departureCalendar: Instant?,
    isDeparture: Boolean,
    onSelectDepartureClicked: () -> Unit,
    onSelectDepartureLongClicked: () -> Unit,
    showProductSelector: () -> Unit,
    isProductsChanged: Boolean
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

    IconButton(onClick = { showProductSelector() }) {
        ProductSelectorIcon(
            changed = isProductsChanged,
            iconSize = 24.dp
        )
    }
}