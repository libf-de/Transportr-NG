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

package de.grobox.transportr.ui.transportnetworkselector

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.grobox.transportr.R
import de.grobox.transportr.networks.networks
import de.grobox.transportr.ui.settings.SettingsViewModel
import de.grobox.transportr.ui.transportnetworkselector.composables.ContinentComposable
import de.grobox.transportr.ui.transportnetworkselector.composables.CountryComposable
import de.grobox.transportr.ui.transportnetworkselector.composables.NetworkComposable

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportNetworkSelectorScreen(
    viewModel: SettingsViewModel,
    navController: NavController
) {
    val expandedIds = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            SmallFloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(start = 12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    null
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_globe),
                        contentDescription = null
                    )

                    Text(
                        text = stringResource(R.string.pick_network_activity),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = stringResource(R.string.pick_network_first_run),
                        style = MaterialTheme.typography.bodyMedium,
                        softWrap = true,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            ) {
                networks.forEach { continent ->
                    item {
                        ContinentComposable(
                            continent = continent,
                            expanded = expandedIds.contains(continent.contour),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            expandedIds.toggle(continent.contour)
                        }
                    }

                    item {
                        HorizontalDivider()
                    }

                    continent.countries.forEach { country ->
                        item {
                            AnimatedVisibility(expandedIds.contains(continent.contour)) {
                                CountryComposable(
                                    country = country,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    expanded = expandedIds.contains(country.name)
                                ) {
                                    expandedIds.toggle(country.name)
                                }
                            }
                        }

                        items(country.networks) {
                            AnimatedVisibility(expandedIds.contains(country.name) && expandedIds.contains(continent.contour)) {
                                NetworkComposable(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp),
                                    network = it,
                                ) {
                                    viewModel.setTransportNetwork(it)
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun <T> SnapshotStateList<T>.toggle(name: T) {
    if (this.contains(name)) {
        this.remove(name)
    } else {
        this.add(name)
    }
}
