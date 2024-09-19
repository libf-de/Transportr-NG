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

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PopupMenuItem(
    text: StringResource,
    icon: DrawableResource,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(stringResource(text)) },
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
        },
        onClick = {
            onClick()
        }
    )
}