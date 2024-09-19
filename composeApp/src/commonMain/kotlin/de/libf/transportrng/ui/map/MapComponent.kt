package de.libf.transportrng.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds

expect fun provideMapState(): MapViewStateInterface

@Composable
expect fun <T : MapViewStateInterface> MapViewComposable(
    mapViewState: T,
    compassMargins: CompassMargins = CompassMargins(),
    isHalfHeight: Boolean = false,
    mapPadding: MapPadding = MapPadding(),
    rotateGestures: Boolean = false,
    showLogo: Boolean = true,
    showAttribution: Boolean = true,
    mapStyle: String = "jawg-streets"
)

interface MapViewStateInterface {
    fun animateTo(latLng: LatLng?, zoom: Int)

    fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean)

    fun zoomToBounds(latLngBounds: LatLngBounds?) {
        zoomToBounds(latLngBounds, false)
    }

    fun animateToBounds(latLngBounds: LatLngBounds?) {
        zoomToBounds(latLngBounds, true)
    }

    fun setPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0)

    suspend fun drawTrip(trip: Trip?, shouldZoom: Boolean)
}

data class CompassMargins(
    val left: Dp = 10.dp,
    val top: Dp = 10.dp,
    val right: Dp = 10.dp,
    val bottom: Dp = 10.dp
) {
    @Composable
    fun toIntArray(): IntArray {
        with(LocalDensity.current) {
            return intArrayOf(
                left.toPx().toInt(),
                top.toPx().toInt(),
                right.toPx().toInt(),
                bottom.toPx().toInt()
            )
        }
    }
}

data class MapPadding(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) {
    constructor(padding: DoubleArray) : this(padding[0].toInt(), padding[1].toInt(), padding[2].toInt(), padding[3].toInt())

    operator fun plus(other: Int) =
        MapPadding(left + other, top + other, right + other, bottom + other)
}