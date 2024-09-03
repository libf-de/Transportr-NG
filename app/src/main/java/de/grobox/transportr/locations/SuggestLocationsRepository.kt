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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuggestLocationsRepository(
    private val manager: TransportNetworkManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val autoCompletionDelay: Long = 300L
) {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    private val _suggestedLocations = MediatorLiveData<Set<WrapLocation>>()
    val suggestedLocations: LiveData<Set<WrapLocation>> = _suggestedLocations

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var suggestLocationsJob: Job? = null
    private var suggestLocationsTask: SuggestLocationsTask? = null
    private var transportNetwork: TransportNetwork? = null

    init {
        _suggestedLocations.addSource(manager.transportNetwork) {
            transportNetwork = it
        }
    }

    fun suggestLocations(query: String) {
        suggestLocationsJob?.cancel()
        suggestLocationsTask?.cancel(true)

        if(query.isEmpty()) {
            _suggestedLocations.postValue(emptySet())
            _isLoading.postValue(false)
            return
        }

        suggestLocationsJob = scope.launch {
            delay(autoCompletionDelay)

            transportNetwork?.let {
                withContext(Dispatchers.Main) {
                    _suggestLocations(query)
                }
            }
        }
    }

    private fun _suggestLocations(query: String) {
        _isLoading.postValue(true)
        transportNetwork?.let {
            if (suggestLocationsTask != null && suggestLocationsTask?.isCancelled == false) return@let null

            suggestLocationsTask = SuggestLocationsTask(it) { suggestLocationsResult ->
                _suggestedLocations.postValue(
                    suggestLocationsResult?.suggestedLocations?.map { sl ->
                        WrapLocation(sl.location)
                    }?.toSet() ?: emptySet()
                )
            }
            suggestLocationsTask?.execute(query)
        }.let {
            if(it == null) _isLoading.postValue(false)
        }
    }

    fun cancelSuggestLocations() {
        suggestLocationsTask?.cancel(true)
        _isLoading.postValue(false)
    }

    fun reset() {
        suggestLocationsJob?.cancel()
        suggestLocationsTask?.cancel(true)
        _isLoading.postValue(false)
        _suggestedLocations.postValue(emptySet())
    }
}