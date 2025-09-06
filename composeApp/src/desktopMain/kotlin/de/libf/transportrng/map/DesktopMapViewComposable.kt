package de.libf.transportrng.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DesktopMapViewState : MapViewStateInterface {
    private val _currentMapCenter: MutableStateFlow<Pair<LatLng, Double>?> = MutableStateFlow(null)

    override val currentMapCenter: Flow<Pair<LatLng, Double>?>
        get() = _currentMapCenter.asStateFlow()

    override var onLocationClicked: (WrapLocation) -> Unit
        get() = {}
        set(value) {}



    override suspend fun animateTo(latLng: LatLng?, zoom: Int, animate: Boolean) {

    }

    override suspend fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {

    }

    suspend fun setPadding(halfHeight: Boolean) {

    }

    override suspend fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {

    }


    override suspend fun drawTrip(trip: Trip?, shouldZoom: Boolean): Boolean {
        return true
    }

    override suspend fun showUserLocation(enabled: Boolean, userLocation: Point?) {

    }

    override suspend fun drawNearbyStations(nearbyStations: List<Location>) {

    }

    override suspend fun clearNearbyStations() {

    }
}

actual fun provideMapState(): MapViewStateInterface = DesktopMapViewState()

@Composable
actual fun <T : MapViewStateInterface> MapViewComposable(
    mapViewState: T,
    compassMargins: CompassMargins,
    isHalfHeight: Boolean,
    mapPadding: MapPadding,
    rotateGestures: Boolean,
    showLogo: Boolean,
    showAttribution: Boolean,
    isDark: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {}
}