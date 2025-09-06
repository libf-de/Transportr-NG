package de.libf.transportrng.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
//import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.interop.UIKitView
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
//import androidx.compose.ui.viewinterop.UIKitView
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.gps.filterByDistance
import de.libf.transportrng.data.gps.filterByDistanceIgnoreZoom
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import de.libf.transportrng.data.utils.formatTime
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.getString
import platform.CoreGraphics.CGBlendMode
import platform.CoreGraphics.CGContextClipToMask
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextSetBlendMode
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapRectMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.darwin.NSObject
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.trip_arr
import transportr_ng.composeapp.generated.resources.trip_dep
import kotlin.math.log2
import kotlin.math.pow

@OptIn(ExperimentalForeignApi::class)
class iOsMapViewState : MapViewStateInterface {
    private var mapView: MKMapView? = null

    internal var mapInset: MapPadding = MapPadding()

//    internal val iconMap: MutableMap<MarkerType, UIImage> = mutableMapOf()
    internal val polylineColorMap: MutableMap<MKPolyline, Color> = mutableMapOf()

    private val _currentMapCenter: MutableStateFlow<Pair<LatLng, Double>?> = MutableStateFlow(null)
    @OptIn(FlowPreview::class)
    override val currentMapCenter: Flow<Pair<LatLng, Double>?>
        get() = _currentMapCenter.asStateFlow()
            .debounce(2000)
            .filterByDistanceIgnoreZoom(2000.0)
            .distinctUntilChanged()

    private var departureText: String = ""
    private var arrivalText: String = ""

    private fun tintedImage(
        imageName: String,
        tint: Color,
        mode: CGBlendMode = CGBlendMode.kCGBlendModeSourceAtop,
        clip: Boolean = false
    ): UIImage? {
        val image = UIImage.imageNamed(imageName) ?: return null
        val rect = image.size.useContents {
            CGRectMake(0.0, 0.0, this.width, this.height)
        }

        UIGraphicsBeginImageContextWithOptions(image.size, false, image.scale)
        val context = UIGraphicsGetCurrentContext() ?: return null

        image.drawInRect(rect)

        if(clip) CGContextClipToMask(context, rect, image.CGImage)

        // Correct way to set blend mode in Core Graphics
        CGContextSetBlendMode(context, mode)

        tint.toUIColor().setFill()

        CGContextFillRect(context, rect)

//        CGContextClipToMask(context, rect, image.CGImage)

        val newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage
    }

    private fun tintedFgBgImage(fgName: String, fgTint: Color, bgName: String, bgTint: Color): UIImage? {
        val fgImage = tintedImage(fgName, fgTint) ?: return null
        val bgImage = tintedImage(bgName, bgTint, CGBlendMode.kCGBlendModeMultiply, true) ?: return null

        val rect = bgImage.size.useContents {
            CGRectMake(0.0, 0.0, this.width, this.height)
        }

        UIGraphicsBeginImageContextWithOptions(bgImage.size, false, bgImage.scale)
        val context = UIGraphicsGetCurrentContext() ?: return null

        bgImage.drawInRect(rect)
        fgImage.drawInRect(rect)

        val newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage
    }

    private val mapViewDelegate = object : NSObject(), MKMapViewDelegateProtocol {
        @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
        override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
            println("mapViewDelegateDingsda $rendererForOverlay")
            println("overlays: ${mapView.overlays}")
            if(rendererForOverlay is MKPolyline) {
                val renderer = MKPolylineRenderer(rendererForOverlay)
                val color = polylineColorMap[rendererForOverlay] ?: Color.Red
                renderer.strokeColor = color.toUIColor()
                renderer.lineWidth = 3.0
                return renderer
            }

//            return super.mapView(mapView, rendererForOverlay = rendererForOverlay)
            return MKOverlayRenderer(rendererForOverlay)
        }

        @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
        override fun mapView(
            mapView: MKMapView,
            viewForAnnotation: MKAnnotationProtocol
        ): MKAnnotationView? {
            return when(viewForAnnotation) {
                is GenericStopAnnotation -> {
                    (mapView.dequeueReusableAnnotationViewWithIdentifier("GenericStop")?.also {
                        it.annotation = viewForAnnotation
                    } ?: MKAnnotationView(viewForAnnotation, "GenericStop").also {
                        it.canShowCallout = true
                    }).also {
                        it.image = UIImage.imageNamed("haltestelle")
                            ?.imageWithTintColor(Color.Magenta.toUIColor())
                    }
                }

                is TintableAnnotation -> {
                    MKAnnotationView(viewForAnnotation, viewForAnnotation.hashCode().toString()).also {
                        it.canShowCallout = true
                    }.also {
                        if(viewForAnnotation.fgColor == null) {
                            it.image = tintedImage(
                                viewForAnnotation.iconName,
                                viewForAnnotation.bgColor ?: Color.Gray
                            )
                        } else {
                            it.image = tintedFgBgImage(
                                viewForAnnotation.iconName,
                                viewForAnnotation.fgColor,
                                TintableAnotationIcons.TRIP_BACKGROUND,
                                viewForAnnotation.bgColor ?: Color.Gray
                            )
                        }

//                        it.image = UIImage.imageNamed(viewForAnnotation.iconName).let {
//                            if(viewForAnnotation.bgColor != null) it?.imageWithTintColor(viewForAnnotation.bgColor.toUIColor())
//                            else it
//                        }
                    }
                }

                else -> null
            }
        }

        override fun mapViewDidChangeVisibleRegion(mapView: MKMapView) {
            if(mapView.getOSMZoomLevel() >= 12) {
                this@iOsMapViewState._currentMapCenter.value = Pair(
                    mapView.centerCoordinate.useContents {
                        LatLng(
                            this.latitude,
                            this.longitude
                        )
                    },
                    mapView.getOSMZoomLevel()
                )
            }
        }
    }

    fun setMapView(mapView: MKMapView) {
        println("Map view set")

        CoroutineScope(Dispatchers.IO).launch {
            departureText = getString(Res.string.trip_dep)
            arrivalText = getString(Res.string.trip_arr)
        }

        this.mapView = mapView
        mapView.delegate = mapViewDelegate
    }

    override var onLocationClicked: (WrapLocation) -> Unit
        get() = TODO("Not yet implemented")
        set(value) {}



    override suspend fun animateTo(latLng: LatLng?, zoom: Int, animate: Boolean) {
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
        mapView?.setRegion(region, animated = animate)
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
                ?.line?.style?.backgroundColor?.let(::Color) ?: Color(0xFFFED21B)
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

                    polylineColorMap[polyline] = backgroundColor

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

                    polylineColorMap[polyline] = backgroundColor

                    mapView?.addOverlay(polyline)
                }
            }

            if(leg is PublicLeg) {
                leg.intermediateStops?.forEach {
                    boundingCoords.add(Pair(it.location.latAsDouble, it.location.lonAsDouble))

                    val text = "${arrivalText}: ${it.getArrivalTime()?.formatTime() ?: "unbekannt"}\n" +
                            "${departureText}: ${it.getDepartureTime()?.formatTime() ?: "unbekannt"}"

                    mapView?.addAnnotation(
                        TintableAnnotation(
                            pLocation = it.location,
                            pTitle = it.location.uniqueShortName,
                            pSubtitle = text,
                            fgColor = null,
                            bgColor = backgroundColor,
                            iconName = TintableAnotationIcons.TRIP_STOP
                        )
                    )
                }

                val icon: String
                val text: String
                if (i == 1 || i == 2 && trip.legs[0] !is PublicLeg) {
                    icon = TintableAnotationIcons.TRIP_START
                    text = "${departureText}: ${leg.departureTime.formatTime()}"
                } else {
                    icon = TintableAnotationIcons.TRIP_CHANGE
                    text ="${arrivalText}: ${trip.legs.getOrNull(i-2)?.arrivalTime?.formatTime() ?: "unbekannt"}\n" +
                        "${departureText}: ${leg.departureTime.formatTime()}"
                }

                mapView?.addAnnotation(
                    TintableAnnotation(
                        pLocation = leg.departure,
                        pTitle = leg.departure.uniqueShortName,
                        pSubtitle = text,
                        iconName = icon,
                        fgColor = foregroundColor,
                        bgColor = backgroundColor
                    )
                )

                if (i == trip.legs.size || i == trip.legs.size - 1 && trip.legs[i] !is PublicLeg) {
                    mapView?.addAnnotation(
                        TintableAnnotation(
                            pLocation = leg.arrival,
                            pTitle = leg.arrival.uniqueShortName,
                            pSubtitle = "${arrivalText}: ${leg.arrivalTime.formatTime()}",
                            iconName = TintableAnotationIcons.TRIP_END,
                            fgColor = foregroundColor,
                            bgColor = backgroundColor
                        )
                    )
                }
            } else if (i > 1 && i < trip.legs.size) {
                // only draw an icon if walk is required in the middle of a trip?
                mapView?.addAnnotation(
                    TintableAnnotation(
                        pLocation = leg.departure,
                        pTitle = leg.departure.uniqueShortName,
                        pSubtitle = "${arrivalText}: ${leg.arrivalTime.formatTime()}",
                        iconName = TintableAnotationIcons.WALK,
                        fgColor = foregroundColor,
                        bgColor = backgroundColor
                    )
                )
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

        animateTo(userLocation?.let { LatLng(it.lat, it.lon) }, 14)
    }

    override suspend fun drawNearbyStations(nearbyStations: List<Location>) {
        nearbyStations.forEach {
            mapView?.addAnnotation(
                GenericStopAnnotation(
                    it,
                    it.uniqueShortName
                )
//                MKPointAnnotation(
//                    CLLocationCoordinate2DMake(it.latAsDouble, it.lonAsDouble),
//                    it.uniqueShortName ?: "",
//                    ""
//                )
            )
        }
    }

    override suspend fun clearNearbyStations() {
        mapView?.annotations?.let {
            mapView?.removeAnnotations(it)
        }
    }

//    @OptIn(ExperimentalResourceApi::class)
//    private suspend fun getMarkerIcon(
//        type: MarkerType,
//        backgroundColor: Color = Color.Unspecified,
//        foregroundColor: Color = Color.Unspecified
//    ): UIImage {
//        return when(type) {
////            MarkerType.STOP -> Res.drawable.ic_marker_trip_stop
////            MarkerType.GENERIC_STOP -> Res.drawable.ic_marker_trip_stop // TODO
////            MarkerType.BEGIN -> Res.drawable.ic_marker_trip_begin
////            MarkerType.CHANGE -> Res.drawable.ic_marker_trip_change
////            MarkerType.END -> Res.drawable.ic_marker_trip_end
////            MarkerType.WALK -> Res.drawable.ic_marker_trip_walk
//            else -> Res.drawable.haltestelle
//        }.let {
//            val bytes: ByteArray = getDrawableResourceBytes(
//                getSystemResourceEnvironment(),
//                it
//            )
//
//            val nsData = bytes.usePinned { pinnedBytes ->
//                NSData.dataWithBytes(pinnedBytes.addressOf(0), bytes.size.toULong())
//            }
//
//            UIImage.imageWithData(nsData)
//        } ?: throw RuntimeException()
//    }

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

@OptIn(ExperimentalForeignApi::class)
fun MKMapView.getOSMZoomLevel(): Double {
    val longitudeDelta = region.useContents { this.span.longitudeDelta }

    // Calculate the OSM zoom level
    val zoomLevel = log2(360.0 / longitudeDelta)

    // Clamp the zoom level between 0 and 18.79543
    return zoomLevel
        .coerceIn(0.0, 18.79543)
}

//fun MKMapView.getOSMZoomLevel(): Float {
//    val camera = camera
//    val altitude = camera.altitude
//
//    // Constants for Earth's radius and OSM zoom level at the equator
//    val earthRadius = 6371000.0 // in meters
//    val osmZoom0Altitude = 2.0 * PI * earthRadius
//
//    // Calculate the OSM zoom level
//    val zoomLevel = log2(osmZoom0Altitude / altitude)
//
//    return zoomLevel.let {
//        zoomLevel * (19.24/20.0)
//    }.coerceIn(0.0, 20.0).toFloat()
//}