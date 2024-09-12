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

package de.grobox.transportr.locations

import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.ptek.dto.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuggestLocationsRepository(
    private val manager: TransportNetworkManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val autoCompletionDelay: Long = 300L
) {
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    private val _suggestedLocations = MutableStateFlow<Set<WrapLocation>>(emptySet())
    val suggestedLocations: StateFlow<Set<WrapLocation>> = _suggestedLocations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var suggestLocationsJob: Job? = null
    private var transportNetwork: TransportNetwork? = null

    init {
        scope.launch {
            manager.transportNetwork.collect {
                transportNetwork = it
            }
        }
    }

    fun suggestLocations(query: String) {
        suggestLocationsJob?.cancel()

        if(query.isEmpty()) {
            _suggestedLocations.value = emptySet()
            _isLoading.value = false
            return
        }

        suggestLocationsJob = scope.launch {
            delay(autoCompletionDelay)

            transportNetwork?.let {
                _suggestedLocations.value = (it.networkProvider
                    .suggestLocations(constraint = query, types = setOf(Location.Type.STATION), maxLocations = 99)
                    .suggestedLocations
                    ?.map { sl -> WrapLocation(sl.location) }
                    ?.toSet() ?: emptySet())
                    .also { _isLoading.value = false }
            }
                .also { if(it == null) _isLoading.value = false }
        }
    }

    fun cancelSuggestLocations() {
        suggestLocationsJob?.cancel()
        _isLoading.value = false
    }

    fun reset() {
        suggestLocationsJob?.cancel()
        _isLoading.value = false
        _suggestedLocations.value = emptySet()
    }
}