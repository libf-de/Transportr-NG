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

package de.libf.transportrng.ui.productselector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Product
import de.libf.transportrng.ui.transport.composables.getDrawableRes
import de.libf.transportrng.ui.transport.composables.getNameRes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductOptionComposable(
    product: Product,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp).clickable {
            onSelectedChange(!selected)
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
            checked = selected,
            onCheckedChange = { onSelectedChange(!selected) }
        )
    }
}