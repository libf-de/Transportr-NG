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

package de.libf.transportrng.ui.favorites.composables

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.libf.transportrng.data.favorites.FavoriteTripItem
import de.libf.transportrng.data.favorites.FavoriteTripType
import de.libf.transportrng.data.locations.WrapLocation
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_add_shortcut
import transportr_ng.composeapp.generated.resources.action_change
import transportr_ng.composeapp.generated.resources.action_swap_locations
import transportr_ng.composeapp.generated.resources.find_departures
import transportr_ng.composeapp.generated.resources.home
import transportr_ng.composeapp.generated.resources.ic_action_departures
import transportr_ng.composeapp.generated.resources.ic_action_home
import transportr_ng.composeapp.generated.resources.ic_action_settings
import transportr_ng.composeapp.generated.resources.ic_add_shortcut
import transportr_ng.composeapp.generated.resources.ic_location
import transportr_ng.composeapp.generated.resources.ic_menu_swap_location
import transportr_ng.composeapp.generated.resources.ic_more_vert
import transportr_ng.composeapp.generated.resources.ic_work
import transportr_ng.composeapp.generated.resources.more
import transportr_ng.composeapp.generated.resources.tap_to_set
import transportr_ng.composeapp.generated.resources.work

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialLocationItem(
    location: FavoriteTripItem,
    modifier: Modifier = Modifier,
    onItemClicked: () -> Unit = {},
    actions: SpecialLocationItemActions?,
    menuContent: @Composable ColumnScope.() -> Unit = { SpecialLocationItemPopupMenu(location, actions!!) }
) {
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
                painter = when(location.type) {
                    FavoriteTripType.HOME -> painterResource(Res.drawable.ic_action_home)
                    FavoriteTripType.WORK -> painterResource(Res.drawable.ic_work)
                    else -> painterResource(Res.drawable.ic_location)
                },
                contentDescription = null,
                modifier = Modifier.width(21.dp).fillMaxHeight().wrapContentHeight(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            ) {

                Text(
                    text = when(location.type) {
                        FavoriteTripType.HOME -> stringResource(Res.string.home)
                        FavoriteTripType.WORK -> stringResource(Res.string.work)
                        else -> location.from._getName() ?: ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = location.to?._getName() ?: stringResource(Res.string.tap_to_set),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Box {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_more_vert),
                            contentDescription = stringResource(Res.string.more),
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

data class SpecialLocationItemActions(
    val onFindDirectionsRequested: (from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, search: Boolean) -> Unit = { _, _, _, _ -> },
    val onFindDeparturesRequested: (location: WrapLocation) -> Unit = { _ -> },
    val onCreateShortcutRequested: (item: FavoriteTripItem) -> Unit = { _ -> },
    val onChangeSpecialLocationRequested: (item: FavoriteTripItem) -> Unit = { _ -> },
)

@Composable
fun ColumnScope.SpecialLocationItemPopupMenu(
    it: FavoriteTripItem,
    actions: SpecialLocationItemActions
) {
    // Search return trip
    PopupMenuItem(
        text = Res.string.action_swap_locations,
        icon = Res.drawable.ic_menu_swap_location,
    ) {
        actions.onFindDirectionsRequested(it.to, it.via, it.from, true)
    }

    it.to?.let { toLocation ->
        // Find departures
        PopupMenuItem(
            text = Res.string.find_departures,
            icon = Res.drawable.ic_action_departures
        ) {
            actions.onFindDeparturesRequested(toLocation)
        }
    }

    // Add shortcut
    PopupMenuItem(
        text = Res.string.action_add_shortcut,
        icon = Res.drawable.ic_add_shortcut
    ) {
        actions.onCreateShortcutRequested(it)
    }


    // Edit special location
    PopupMenuItem(
        text = Res.string.action_change,
        icon = Res.drawable.ic_action_settings
    ) {
        actions.onChangeSpecialLocationRequested(it)
    }
}
