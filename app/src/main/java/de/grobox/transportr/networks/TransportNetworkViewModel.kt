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

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.grobox.transportr.TransportrApplication

abstract class TransportNetworkViewModel protected constructor(
    application: TransportrApplication,
    manager: TransportNetworkManager
) : AndroidViewModel(application) {
    val transportNetwork: LiveData<TransportNetwork?> = manager.transportNetwork
    val transportNetworks = MediatorLiveData<List<TransportNetwork>>()

    init {
        transportNetworks.addSource(manager.transportNetwork) {
            transportNetworks.value = listOfNotNull(
                manager.getTransportNetwork(1),
                manager.getTransportNetwork(2),
                manager.getTransportNetwork(3)
            )
        }
    }

}
