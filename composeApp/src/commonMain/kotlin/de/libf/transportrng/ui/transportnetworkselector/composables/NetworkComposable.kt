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

package de.libf.transportrng.ui.transportnetworkselector.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.libf.transportrng.data.networks.TransportNetwork
import de.libf.transportrng.data.utils.name
import org.jetbrains.compose.resources.painterResource

@Composable
fun NetworkComposable(
    modifier: Modifier = Modifier,
    network: TransportNetwork,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(network.logo),
                contentDescription = null,
                modifier = Modifier.width(48.dp)
            )

            Text(
                text = network.getNameRes().name(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
        }

        HorizontalDivider()
    }
}