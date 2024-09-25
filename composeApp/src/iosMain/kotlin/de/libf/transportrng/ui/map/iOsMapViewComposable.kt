package de.libf.transportrng.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
//import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.interop.UIKitView
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
//import androidx.compose.ui.viewinterop.UIKitView
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.gps.filterByDistance
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapRectMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIImage
import platform.darwin.NSObject
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_marker_trip_begin
import transportr_ng.composeapp.generated.resources.ic_marker_trip_change
import transportr_ng.composeapp.generated.resources.ic_marker_trip_end
import transportr_ng.composeapp.generated.resources.ic_marker_trip_stop
import transportr_ng.composeapp.generated.resources.ic_marker_trip_walk
import kotlin.math.pow

@OptIn(ExperimentalForeignApi::class)
class iOsMapViewState : MapViewStateInterface {
    private var mapView: MKMapView? = null

    internal var mapInset: MapPadding = MapPadding()

    private val _currentMapCenter: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    override val currentMapCenter: Flow<LatLng?>
        get() = _currentMapCenter.asStateFlow()
            .debounce(2000)
            .onEach { println("flow: ${it?.latitude}, ${it?.longitude}") }
            .filterByDistance(2000.0)
            .distinctUntilChanged()

    private val mapViewDelegate = object : NSObject(), MKMapViewDelegateProtocol {
        @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
        override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
            println("mapViewDelegateDingsda $rendererForOverlay")
            println("overlays: ${mapView.overlays}")
            if(rendererForOverlay is MKPolyline) {
                val renderer = MKPolylineRenderer(rendererForOverlay)
                renderer.strokeColor = Color.Red.toUIColor()
                renderer.lineWidth = 3.0
                return renderer
            }

//            return super.mapView(mapView, rendererForOverlay = rendererForOverlay)
            return MKOverlayRenderer(rendererForOverlay)
        }

        override fun mapViewDidChangeVisibleRegion(mapView: MKMapView) {
//            super.mapViewDidChangeVisibleRegion(mapView)
            this@iOsMapViewState._currentMapCenter.value = mapView.centerCoordinate.useContents {
                LatLng(
                    this.latitude,
                    this.longitude
                )
            }
            println(mapView.centerCoordinate.useContents { "${this.latitude}, ${this.longitude}" })
        }
    }

    fun setMapView(mapView: MKMapView) {
        println("Map view set")
        this.mapView = mapView
        mapView.delegate = mapViewDelegate
    }

    override var onLocationClicked: (WrapLocation) -> Unit
        get() = TODO("Not yet implemented")
        set(value) {}



    override suspend fun animateTo(latLng: LatLng?, zoom: Int) {
        if(latLng == null || mapView == null) return
        val coordinate = CLLocationCoordinate2DMake(latLng.latitude, latLng.longitude)

        // Ensure zoom level is within 0-20 range
        val clampedZoom = zoom.coerceIn(0, 20)

        // Convert OSM zoom level to MKCoordinateSpan
        // At zoom level 0, we want to show the whole world (360 degrees)
        // Each zoom level divides this by 2
        val latitudeDelta = 360.0 / 2.0.pow(clampedZoom.toDouble())
        val span =  MKCoordinateSpanMake(latitudeDelta, latitudeDelta) // Assuming an equirectangular projection

        val region = MKCoordinateRegionMake(coordinate, span)
        mapView?.setRegion(region, animated = true)
    }

    override suspend fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
        println("zoomToBounds init")

        if(latLngBounds == null) return

        println("zoomToBounds: $latLngBounds")

        withTimeout(5000L) {
            while (mapView == null) {
                delay(500L)
            }
            true
        }

        val mapRect = MKMapRectMake(
            x = latLngBounds.longitudeWest,
            y = latLngBounds.latitudeNorth,
            width = latLngBounds.longitudeEast - latLngBounds.longitudeWest,
            height = latLngBounds.latitudeSouth - latLngBounds.latitudeNorth
        )
        mapView?.setVisibleMapRect(mapRect, animate) ?: run {
            println("no map view :(")
        }
    }

    suspend fun setPadding(halfHeight: Boolean) {
        if(halfHeight) {
            mapView?.let {
                val halfY = it.frame.useContents { this.size.height / 2 }

                it.layoutMargins = UIEdgeInsetsMake(top = 0.0, bottom = halfY, left = 0.0, right = 0.0)

                println("half height is $halfY")
//                setPadding(top = -halfY, bottom = halfY)
            }
        }
    }

    override suspend fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mapView?.let {
            it.setVisibleMapRect(
                it.visibleMapRect,
                edgePadding = mapInset.negative().toUIEdgeInsets(),
                animated = true
            )
            it
        }.also {
            mapInset = MapPadding(left, top, right, bottom)
        }?.let {
            it.setVisibleMapRect(
                it.visibleMapRect,
                edgePadding = mapInset.toUIEdgeInsets(),
                animated = true
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun drawTrip(trip: Trip?, shouldZoom: Boolean): Boolean {
        println("drawTrip")

        if (trip == null) return false

        withTimeout(5000L) {
            while (mapView == null) {
                delay(500L)
            }
            true
        }

        //symbolManager?.removeClickListener(this.onSymbolClickListener)
        //symbolManager?.deleteAll()
        mapView?.annotations?.let {
            mapView?.removeAnnotations(it)
        }

        mapView?.overlays?.let {
            mapView?.removeOverlays(it)
        }

        val boundingCoords: MutableList<Pair<Double, Double>> = mutableListOf()

        var i = 1
        trip.legs.forEachIndexed { j, leg ->
            // get colors
            val backgroundColor = leg.takeIf { it is PublicLeg }
                ?.let { it as PublicLeg }
                ?.line?.style?.backgroundColor ?: Color(0xFFFED21B)
            val foregroundColor = Color.White

            memScoped {
                leg.path?.let { leg ->
                    boundingCoords.addAll(leg.map { Pair(it.lat, it.lon) })

                    println("drawing path")
                    val coordinatesPointer = allocArray<CLLocationCoordinate2D>(leg.size) {
                        this.latitude = leg[it].lat
                        this.longitude = leg[it].lon
                    }

                    val polyline = MKPolyline.polylineWithCoordinates(
                        coordinatesPointer,
                        leg.size.toULong()
                    )

                    mapView?.addOverlay(polyline)
                } ?: leg.let { leg ->
                    println("pseudo-draw")
                    val coordinatesPointer = allocArray<CLLocationCoordinate2D>(2.toInt()) {
                        if(it == 0) {
                            this.latitude = leg.departure.latAsDouble
                            this.longitude = leg.departure.latAsDouble
                        } else {
                            this.latitude = leg.arrival.latAsDouble
                            this.longitude = leg.arrival.lonAsDouble
                        }
                    }

                    val polyline = MKPolyline.polylineWithCoordinates(
                        coordinatesPointer,
                        2.toULong()
                    )

                    mapView?.addOverlay(polyline)
                }
            }

            if(leg is PublicLeg) {
                leg.intermediateStops?.forEach {
                    boundingCoords.add(Pair(it.location.latAsDouble, it.location.lonAsDouble))

                    mapView?.addAnnotation(
                        MKPointAnnotation(
                            CLLocationCoordinate2DMake(it.location.latAsDouble, it.location.lonAsDouble),
                            it.location.uniqueShortName ?: "",
                            ""
                        )
                    )
                }
            }
            i += 1
        }


//        if(shouldZoom) {
            mapView?.let {
                it.showAnnotations(it.annotations, animated = true)

//                delay(2000L)
//
//                val halfY = it.frame.useContents { this.size.height / 2 }
//
//                it.setVisibleMapRect(
//                    it.visibleMapRect,
//                    edgePadding = UIEdgeInsetsMake(top = -halfY, bottom = halfY, left = 0.0, right = 0.0),
//                    animated = true
//                )
            }
        return true
    }

    override suspend fun showUserLocation(enabled: Boolean, userLocation: Point?) {
        mapView?.showsUserLocation = enabled
    }

    override suspend fun drawNearbyStations(nearbyStations: List<Location>) {
        nearbyStations.forEach {
            mapView?.addAnnotation(
                MKPointAnnotation(
                    CLLocationCoordinate2DMake(it.latAsDouble, it.lonAsDouble),
                    it.uniqueShortName ?: "",
                    ""
                )
            )
        }
    }

    override suspend fun clearNearbyStations() {
        mapView?.annotations?.let {
            mapView?.removeAnnotations(it)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun getMarkerIcon(
        type: MarkerType,
        backgroundColor: Color = Color.Unspecified,
        foregroundColor: Color = Color.Unspecified
    ): UIImage {
        return when(type) {
            MarkerType.STOP -> Res.drawable.ic_marker_trip_stop
            MarkerType.GENERIC_STOP -> Res.drawable.ic_marker_trip_stop // TODO
            MarkerType.BEGIN -> Res.drawable.ic_marker_trip_begin
            MarkerType.CHANGE -> Res.drawable.ic_marker_trip_change
            MarkerType.END -> Res.drawable.ic_marker_trip_end
            MarkerType.WALK -> Res.drawable.ic_marker_trip_walk
        }.let {
            val bytes: ByteArray = getDrawableResourceBytes(
                getSystemResourceEnvironment(),
                it
            )

            val nsData = bytes.usePinned { pinnedBytes ->
                NSData.dataWithBytes(pinnedBytes.addressOf(0), bytes.size.toULong())
            }

            UIImage.imageWithData(nsData)
        } ?: throw RuntimeException()
    }

}

private fun MapPadding.negative(): MapPadding {
    return MapPadding(
        left = -this.left,
        top = -this.top,
        right = -this.right,
        bottom = -this.bottom
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun MapPadding.toUIEdgeInsets(): CValue<UIEdgeInsets> {
    return UIEdgeInsetsMake(
        top = this.top.toDouble(),
        left = this.left.toDouble(),
        bottom = this.bottom.toDouble(),
        right = this.right.toDouble()
    )
}


actual fun provideMapState(): MapViewStateInterface = iOsMapViewState()


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
    UIKitMapView(
        mapViewState as iOsMapViewState,
        compassMargins,
        isHalfHeight,
        mapPadding,
        rotateGestures,
        showLogo,
        showAttribution
    )
}


@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UIKitMapView(
    mapViewState: iOsMapViewState,
    compassMargins: CompassMargins,
    isHalfHeight: Boolean,
    mapPadding: MapPadding,
    rotateGestures: Boolean,
    showLogo: Boolean,
    showAttribution: Boolean
) {
    LaunchedEffect(isHalfHeight) {
        mapViewState.setPadding(isHalfHeight)
    }

    val mapView = remember { MKMapView() }
    UIKitView(
        factory = {
            mapView.apply {
                mapViewState.setMapView(this)
            }
        },
        update = {
            val halfY = it.frame.useContents { this.size.height / 2 }

            println("half height is $halfY")

            it.layoutMargins = if(isHalfHeight) {
                UIEdgeInsetsMake(top = 0.0, bottom = halfY, left = 0.0, right = 0.0)
            } else {
                UIEdgeInsetsMake(top = 0.0, bottom = 0.0, left = 0.0, right = 0.0)
            }
        },
        modifier = Modifier.fillMaxSize(),
//        properties = UIKitInteropProperties(
//            // Allows the map to be moved without a delay. As the Map is never placed in a
//            // scrollable container, I (probably) don't need to be cooperative
//            interactionMode = UIKitInteropInteractionMode.Cooperative(
//                delayMillis = 10
//            )
//        )
    )
}

//@OptIn(ExperimentalForeignApi::class)
//fun ComposeEntryPointWithUIViewController(
//    createUIViewController: () -> UIViewController
//): UIViewController =
//    ComposeUIViewController {
//        Column(
//            Modifier
//                .fillMaxSize()
//                .windowInsetsPadding(WindowInsets.systemBars),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("How to use SwiftUI inside Compose Multiplatform")
//            UIKitViewController(
//                factory = createUIViewController,
//                modifier = Modifier.size(300.dp).border(2.dp, Color.Blue),
//            )
//        }
//    }

// Extension function to convert Compose Color to UIColor
fun Color.toUIColor(): UIColor {
    return UIColor(red = this.red.toDouble(), green = this.green.toDouble(), blue = this.blue.toDouble(), alpha = this.alpha.toDouble())
}
