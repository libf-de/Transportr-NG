/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.networks

import androidx.lifecycle.ViewModel
import de.libf.transportrng.data.networks.TransportNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
abstract class TransportNetworkViewModel protected constructor(
    manager: TransportNetworkManager
) : ViewModel() {
    val transportNetwork: StateFlow<TransportNetwork?> = manager.transportNetwork
    val transportNetworks: Flow<List<TransportNetwork>> = transportNetwork.mapLatest {
        listOfNotNull(
            manager.getTransportNetwork(1),
            manager.getTransportNetwork(2),
            manager.getTransportNetwork(3)
        )
    }
}
