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

package de.libf.transportrng.ui.map

import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.gps.GpsState
import de.libf.transportrng.ui.map.GpsMapViewModel.GpsFabState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

interface GpsMapViewModel {

    val gpsRepository: GpsRepository

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

class GpsMapViewModelImpl(override val gpsRepository: GpsRepository) : GpsMapViewModel {
    override val isCameraTracking = MutableStateFlow(false)
    override val isPositionStale = MutableStateFlow(false)
    override val gpsFabState: Flow<GpsFabState?> = combine(
        gpsRepository.getGpsState(),
        isCameraTracking,
        isPositionStale
    ) { positionState, isTracking, isStale ->
        when {
            positionState == GpsState.DENIED || positionState == GpsState.DISABLED || isStale -> GpsFabState.DISABLED
            positionState == GpsState.ENABLED && !isTracking -> GpsFabState.ENABLED
            positionState == GpsState.ENABLED && isTracking -> GpsFabState.TRACKING
            else -> null
        }
    }
}