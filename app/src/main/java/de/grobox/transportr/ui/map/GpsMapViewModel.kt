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

package de.grobox.transportr.ui.map

import de.grobox.transportr.map.PositionController
import de.grobox.transportr.ui.map.GpsMapViewModel.GpsFabState
import de.grobox.transportr.map.PositionController.PositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

interface GpsMapViewModel {

    val positionController: PositionController

    //todo: change to LiveData
    val isCameraTracking: MutableStateFlow<Boolean>
    val isPositionStale: MutableStateFlow<Boolean>
    val gpsFabState: Flow<GpsFabState?>

    enum class GpsFabState {
        DISABLED,
        ENABLED,
        TRACKING
    }
}

class GpsMapViewModelImpl(override val positionController: PositionController) : GpsMapViewModel {
    override val isCameraTracking = MutableStateFlow(false)
    override val isPositionStale = MutableStateFlow(false)
    override val gpsFabState: Flow<GpsFabState?> = combine(
        positionController.positionState,
        isCameraTracking,
        isPositionStale
    ) { positionState, isTracking, isStale ->
        when {
            positionState == PositionState.DENIED || positionState == PositionState.DISABLED || isStale -> GpsFabState.DISABLED
            positionState == PositionState.ENABLED && !isTracking -> GpsFabState.ENABLED
            positionState == PositionState.ENABLED && isTracking -> GpsFabState.TRACKING
            else -> null
        }
    }
}