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

package de.grobox.transportr.composables

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.grobox.transportr.R
import de.grobox.transportr.map.BaseMapFragment.MapPadding
import de.grobox.transportr.trips.detail.TripDrawer
import de.schildbach.pte.dto.Trip
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

// Returns the Jawg url depending on the style given (jawg-streets by default)
// taken from https://www.jawg.io/docs/integration/maplibre-gl-android/simple-map/
private fun makeStyleUrl(style: String = "jawg-streets", context: Context) =
    "${context.getString(R.string.jawg_styles_url) + style}.json?access-token=${context.getString(R.string.jawg_access_token)}"

@Composable
fun MapViewComposable(
    mapViewState: MapViewState,
    compassMargins: CompassMargins = CompassMargins(),
    isHalfHeight: Boolean = false,
    mapPadding: MapPadding = MapPadding(),
    mapStyle: String = "jawg-streets"
) {
    val compassMarginsInt = compassMargins.toIntArray()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(
                ContextThemeWrapper(context, R.style.MapStyle),
                MapLibreMapOptions().compassMargins(compassMarginsInt)
            ).apply {
                val mvs = mapViewState.registerMapView(this, context)
                getMapAsync { map ->
                    val styleUrl = makeStyleUrl(mapStyle, context)
                    if(map.style?.uri != styleUrl) map.setStyle(styleUrl) {
                        mvs.onMapStyleLoaded(it)
                    }

                    if(!isHalfHeight) {
                        mvs.mapInset = mapPadding

                        map.moveCamera(
                            CameraUpdateFactory.paddingTo(
                                mapPadding.left.toDouble(),
                                mapPadding.top.toDouble(),
                                mapPadding.right.toDouble(),
                                mapPadding.bottom.toDouble()
                            )
                        )
                    } else {
                        mvs.mapInset = MapPadding(0, 0, 0, this.height / 2)
                        map.moveCamera(
                            CameraUpdateFactory.paddingTo(
                                0.0,
                                0.0,
                                0.0,
                                this.height / 2.0
                            )
                        )

                    }

                }
            }
        }
    )
}

class MapViewState {
    protected var context: Context? = null
    protected var mapView: MapView? = null
    var mapPadding: Int = 0
    internal var mapInset: MapPadding = MapPadding()
    internal var onMapStyleLoaded: (style: Style) -> Unit = {}



    internal fun registerMapView(mapView: MapView, context: Context): MapViewState {
        this.mapView = mapView
        this.context = context
        return this
    }

    fun setOnMapStyleLoaded(onMapStyleLoaded: (style: Style) -> Unit) {
        this.onMapStyleLoaded = onMapStyleLoaded
    }

    fun animateTo(latLng: LatLng?, zoom: Int) {
        if (latLng == null) return
        mapView?.getMapAsync { map ->
            val padding = mapInset + mapPadding
            map.moveCamera(CameraUpdateFactory.paddingTo(padding.left.toDouble(), padding.top.toDouble(), padding.right.toDouble(), padding.bottom.toDouble()))
            val update = if (map.cameraPosition.zoom < zoom) CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoom.toDouble()
            ) else CameraUpdateFactory.newLatLng(latLng)
            map.easeCamera(update, 1500)
        }
    }

    fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
        if (latLngBounds == null) return
        val padding = mapInset + mapPadding
        val update = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding.left, padding.top, padding.right, padding.bottom)

        mapView?.getMapAsync { map ->
            if (animate) {
                map.easeCamera(update)
            } else {
                map.moveCamera(update)
            }
        }
    }

    fun zoomToBounds(latLngBounds: LatLngBounds?) {
        zoomToBounds(latLngBounds, false)
    }

    fun animateToBounds(latLngBounds: LatLngBounds?) {
        zoomToBounds(latLngBounds, true)
    }

    fun setPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        // store map padding to be retained even after CameraBoundsUpdates
        // and update directly for subsequent camera updates in MapDrawer
        mapInset = MapPadding(left, top, right, bottom)
        mapView?.getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.paddingTo(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble()))
        }
    }

    fun drawTrip(trip: Trip?, shouldZoom: Boolean) {
        if (trip == null) return

        context?.let { ctx ->
            mapView?.getMapAsync { map ->
                TripDrawer(ctx).draw(map, trip, shouldZoom)
            }
        }
    }
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