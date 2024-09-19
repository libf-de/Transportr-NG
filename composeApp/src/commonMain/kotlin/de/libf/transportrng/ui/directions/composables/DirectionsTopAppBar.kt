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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import de.libf.transportrng.ui.composables.CustomLargeTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectionsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit,

    smallHeader: @Composable () -> Unit,
    expandedHeader: @Composable () -> Unit,

    expandedHeight: Dp,
) {
    CustomLargeTopAppBar(
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            IconButton(onClick = { onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = { actions() },
        title = {
            smallHeader()
        },
        expandedTitle = {
            expandedHeader()
        },
        scrollBehavior = scrollBehavior,
        expandedHeight = expandedHeight
    )
}
