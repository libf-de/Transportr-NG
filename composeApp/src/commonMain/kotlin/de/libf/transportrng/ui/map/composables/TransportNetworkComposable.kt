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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.libf.transportrng.data.networks.TransportNetwork
import de.libf.transportrng.data.utils.name
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TransportNetworkComposable(
    modifier: Modifier = Modifier,
    networks: List<TransportNetwork>,
    onNetworkClick: (TransportNetwork) -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if(networks.isNotEmpty()) {
                    Image(
                        painter = painterResource(networks.first().logo),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    )
                } else {
                    Spacer(modifier = Modifier.size(60.dp).background(Color.Red))
                }

                Spacer(modifier = Modifier.weight(1f))

                networks.forEachIndexed { index, transportNetwork ->
                    if(index == 0) return@forEachIndexed
                    Icon(
                        painter = painterResource(transportNetwork.logo),
                        contentDescription = transportNetwork.getNameRes().name(),
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).clickable {
                            onNetworkClick(transportNetwork)
                        }
                    )
                }
            }

            Text(
                text = networks.firstOrNull()?.getNameRes()?.name() ?: "",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = networks.firstOrNull()?.getDescriptionRes()?.map { stringResource(it) }?.joinToString { ", " } ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
