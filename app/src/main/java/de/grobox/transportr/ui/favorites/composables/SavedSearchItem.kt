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

package de.grobox.transportr.ui.favorites.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.grobox.transportr.R
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.locations.WrapLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSearchItem(
    itm: FavoriteTripItem,
    modifier: Modifier = Modifier,
    onItemClicked: () -> Unit = {},
    actions: SavedSearchItemActions?,
    menuContent: @Composable ColumnScope.() -> Unit = { SavedSearchItemPopupMenu(itm, actions!!) }
) {
    val sketchColor = MaterialTheme.colorScheme.onSurfaceVariant
    val circleRadius = 5.dp
    val topMargin = 4.dp

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
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min)
        ) {
            Icon(
                painter = painterResource(if(itm.isFavorite) R.drawable.ic_action_star else R.drawable.ic_time),
//                painter = painterResource(R.drawable.ic_time),
                contentDescription = null,
                modifier = Modifier
                    .width(21.dp)
                    .fillMaxHeight()
                    .wrapContentHeight(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .weight(1f)
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
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
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
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
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
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
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
                            modifier = Modifier.size(28.dp)
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

data class SavedSearchItemActions(
    val onFindDirectionsRequested: (from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, search: Boolean) -> Unit = { _, _, _, _ -> },
    val onCreateShortcutRequested: (item: FavoriteTripItem) -> Unit = { _ -> },
    val onPresetDirectionsRequested: (from: WrapLocation?, via: WrapLocation?, to: WrapLocation?) -> Unit = { _, _, _ -> },
    val onSetTripFavoriteRequested: (item: FavoriteTripItem, isFavorite: Boolean) -> Unit = { _, _ -> },
    val onDeleteRequested: (item: FavoriteTripItem) -> Unit = { _ -> },
)


@Composable
fun ColumnScope.SavedSearchItemPopupMenu(
    it: FavoriteTripItem,
    actions: SavedSearchItemActions
) {
    // Find return trip
    PopupMenuItem(
        text = R.string.action_swap_locations,
        icon = R.drawable.ic_menu_swap_location,
    ) {
        actions.onFindDirectionsRequested(it.from, it.via, it.to, true)
    }

    // Edit search
    PopupMenuItem(
        text = R.string.action_set_locations,
        icon = R.drawable.ic_mode_edit
    ) {
        actions.onPresetDirectionsRequested(it.from, it.via, it.to)
    }

    // Mark trip as favorite
    PopupMenuItem(
        text = R.string.action_fav_trip,
        icon = R.drawable.ic_action_star
    ) {
        actions.onSetTripFavoriteRequested(it, !it.isFavorite)
    }

    // Add shortcut
    PopupMenuItem(
        text = R.string.action_add_shortcut,
        icon = R.drawable.ic_add_shortcut
    ) {
        actions.onCreateShortcutRequested(it)
    }

    // Delete
    PopupMenuItem(
        text = R.string.action_trip_delete,
        icon = R.drawable.ic_delete
    ) {
        actions.onDeleteRequested(it)
    }
}