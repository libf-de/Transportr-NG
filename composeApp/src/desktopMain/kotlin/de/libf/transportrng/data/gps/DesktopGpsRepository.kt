package de.libf.transportrng.data.gps

import de.libf.ptek.dto.Point
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class DesktopGpsRepository(private val minDeltaMeters: Float = 50f) : GpsRepository {

    private val isEnabledInternal = MutableStateFlow(false)
    override val isEnabled: StateFlow<Boolean> = isEnabledInternal.asStateFlow()

    override fun getGpsStateFlow(): Flow<GpsState> = flow {
        emit(GpsState.Disabled)
    }


    override fun setEnabled(enabled: Boolean) {

    }
}