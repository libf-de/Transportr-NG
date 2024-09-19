package de.libf.transportrng.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
//import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.interop.UIKitView
//import androidx.compose.ui.viewinterop.UIKitView
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.maplibrecompat.LatLng
import de.libf.transportrng.data.maplibrecompat.LatLngBounds
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineMeta
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.UIKit.UIColor
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class iOsMapViewState : MapViewStateInterface {
    private var mapView: MKMapView? = null

    private val mapViewDelegate = object : NSObject(), MKMapViewDelegateProtocol {
        @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
        override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
            if(rendererForOverlay is MKPolylineRenderer) {
                val renderer = MKPolylineRenderer(rendererForOverlay)
                renderer.strokeColor = Color.Red.toUIColor()
                renderer.lineWidth = 3.0
                return renderer
            }

//            return super.mapView(mapView, rendererForOverlay = rendererForOverlay)
            return MKOverlayRenderer()
        }
    }

    fun setMapView(mapView: MKMapView) {
        println("Map view set")
        this.mapView = mapView
        mapView.delegate = mapViewDelegate
    }

    override fun animateTo(latLng: LatLng?, zoom: Int) {
    }

    override fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun drawTrip(trip: Trip?, shouldZoom: Boolean) {
        println("drawTrip")

        if (trip == null) return

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



        var i = 1
        trip.legs.forEachIndexed { j, leg ->
            // get colors
            val backgroundColor = leg.takeIf { it is PublicLeg }
                ?.let { it as PublicLeg }
                ?.line?.style?.backgroundColor ?: Color(0xFFFED21B)
            val foregroundColor = Color.White

            memScoped {
                leg.path?.let { leg ->
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
                    mapView?.addAnnotation(
                        MKPointAnnotation(
                            CLLocationCoordinate2DMake(it.location.latAsDouble, it.location.lonAsDouble),
                            it.location.uniqueShortName ?: "",
                            ""
                        )
                    )
                }
            }

//            if (leg is PublicLeg) {
//                leg.intermediateStops?.forEach { stop ->
//                    val stopIconId = stop.hashCode().toString()
//                    val stopIcon = getMarkerIcon(ctx, MarkerType.STOP, backgroundColor, foregroundColor)
//
//                    addMarker(
//                        map = map,
//                        pos = stop.location.toLatLng(),
//                        title = stop.location.uniqueShortName ?: "",
//                        text = stop.getText(ctx),
//                        icon = stopIcon
//                    )
//                }
//
//                // Draw first station or change station
//                val startId = leg.departure.hashCode().toString()
//                val icon: Drawable
//                var text: String = ""
//                if (i == 1 || i == 2 && trip.legs[0] !is PublicLeg) {
//                    icon = getMarkerIcon(ctx, MarkerType.BEGIN, backgroundColor, foregroundColor)
//                    text = getStationText(ctx, leg, MarkerType.BEGIN)
//                } else {
//                    icon = getMarkerIcon(ctx, MarkerType.CHANGE, backgroundColor, foregroundColor)
//                    text = getStationText(ctx, trip.legs[i - 2], leg)
//                }
//
//                addMarker(
//                    map = map,
//                    pos = leg.departure.toLatLng(),
//                    icon = icon,
//                    text = text,
//                    title = leg.departure.uniqueShortName ?: ""
//                )
//
//                // Draw final station only at the end or if end is walking
//                if (i == trip.legs.size || i == trip.legs.size - 1 && trip.legs[i] !is PublicLeg) {
//                    val endId = leg.arrival.hashCode().toString()
//                    val endIcon = getMarkerIcon(ctx, MarkerType.END, backgroundColor, foregroundColor)
//
//                    addMarker(
//                        map = map,
//                        pos = leg.arrival.toLatLng(),
//                        icon = endIcon,
//                        text = getStationText(ctx, leg, MarkerType.END),
//                        title = leg.arrival.uniqueShortName ?: ""
//                    )
//                }
//
//
//            } else if (i > 1 && i < trip.legs.size) {
//                // only draw an icon if walk is required in the middle of a trip
//                val id = leg.departure.hashCode().toString()
//                val icon = getMarkerIcon(ctx, MarkerType.WALK, backgroundColor, foregroundColor)
//
//                addMarker(
//                    map = map,
//                    pos = leg.departure.toLatLng(),
//                    icon = icon,
//                    text = getStationText(ctx, leg, MarkerType.END),
//                    title = leg.departure.uniqueShortName ?: ""
//                )
//            }

            i += 1
        }

//
//        val builder = LatLngBounds.Builder()
//
//        context?.let { ctx ->
//            mapView?.getMapAsync { map ->
////                TripDrawer(ctx).draw(map, trip, shouldZoom)
//
//                if (this.onSymbolClickListener == null)
//                    this.onSymbolClickListener = { symbol ->
//                        if (symbol.iconImage == speechBubbleId) {
//                            speechBubbleSymbol?.let {
//                                symbolManager?.delete(it)
//                                map.style?.removeImage(speechBubbleId ?: "")
//
//                                speechBubbleId = null
//                                speechBubbleSymbol = null
//                            }
//                        } else {
//                            speechBubbleSymbol?.let { symbolManager?.delete(it) }
//                            speechBubbleId?.let { map.style?.removeImage(it) }
//
//                            symbol.data?.let {
//                                val title = it.asJsonObject["title"].asString
//                                val text = it.asJsonObject["text"].asString
//
//                                val drawable = createSpeechBubbleDrawable(
//                                    context = ctx,
//                                    title = title,
//                                    content = text,
//                                    backgroundColor = Color.White.toArgb(),
//                                    outlineColor = Color.Blue.toArgb(),
//                                    textColor = Color.Black.toArgb()
//                                ).toBitmapDrawable().toBitmap()
//                                //.toBitmap(width = 400, height = 200)
//
//                                val code = drawable.hashCode().toString()
//
//                                speechBubbleId = code
//
//                                map.style?.addImage(code, drawable)
//
//                                val symbolOptions = SymbolOptions()
//                                    .withLatLng(symbol.latLng)
//                                    .withIconImage(speechBubbleId ?: "")
//                                    .withIconSize(1.0f)
//                                    .withSymbolSortKey(1000f)
//
//                                speechBubbleSymbol = symbolManager!!.create(symbolOptions)
//                            }
//                        }
//
//                        true
//                    }
//
//                symbolManager?.addClickListener(this.onSymbolClickListener!!)
//
//
//                var i = 1
//                trip.legs.forEachIndexed { j, leg ->
//                    // get colors
//                    val backgroundColor = leg.getBackgroundColor(ctx).let(::Color).takeIf { leg is PublicLeg } ?: Color(0xFFFED21B)
//                    //val foregroundColor = leg.getForegroundColor(ctx).let(::Color)
//                    val foregroundColor = Color.White
//
//                    lineManager?.let { lineMgr ->
//                        val points = ArrayList<LatLng>(leg.path?.size ?: 0)
//
//                        val colorHex = backgroundColor.toHexString()
//
//                        leg.path?.mapTo(points) { LatLng(it.lat, it.lon) }
//                        val lineOptions = LineOptions()
//                            .withLineJoin("round")
//                            .withLatLngs(points)
//                            .withLineColor(colorHex)
//                            .withLineWidth(5.0f)
//
//                        lines.add(
//                            lineMgr.create(lineOptions)
//                        )
//
//                        builder.includes(points)
//                    }
//
//                    symbolManager?.let { symMgr ->
//                        if (leg is PublicLeg) {
//                            leg.intermediateStops?.forEach { stop ->
//                                val stopIconId = stop.hashCode().toString()
//                                val stopIcon = getMarkerIcon(ctx, MarkerType.STOP, backgroundColor, foregroundColor)
//
//                                addMarker(
//                                    map = map,
//                                    pos = stop.location.toLatLng(),
//                                    title = stop.location.uniqueShortName ?: "",
//                                    text = stop.getText(ctx),
//                                    icon = stopIcon
//                                )
//                            }
//
//                            // Draw first station or change station
//                            val startId = leg.departure.hashCode().toString()
//                            val icon: Drawable
//                            var text: String = ""
//                            if (i == 1 || i == 2 && trip.legs[0] !is PublicLeg) {
//                                icon = getMarkerIcon(ctx, MarkerType.BEGIN, backgroundColor, foregroundColor)
//                                text = getStationText(ctx, leg, MarkerType.BEGIN)
//                            } else {
//                                icon = getMarkerIcon(ctx, MarkerType.CHANGE, backgroundColor, foregroundColor)
//                                text = getStationText(ctx, trip.legs[i - 2], leg)
//                            }
//
//                            addMarker(
//                                map = map,
//                                pos = leg.departure.toLatLng(),
//                                icon = icon,
//                                text = text,
//                                title = leg.departure.uniqueShortName ?: ""
//                            )
//
//                            // Draw final station only at the end or if end is walking
//                            if (i == trip.legs.size || i == trip.legs.size - 1 && trip.legs[i] !is PublicLeg) {
//                                val endId = leg.arrival.hashCode().toString()
//                                val endIcon = getMarkerIcon(ctx, MarkerType.END, backgroundColor, foregroundColor)
//
//                                addMarker(
//                                    map = map,
//                                    pos = leg.arrival.toLatLng(),
//                                    icon = endIcon,
//                                    text = getStationText(ctx, leg, MarkerType.END),
//                                    title = leg.arrival.uniqueShortName ?: ""
//                                )
//                            }
//
//
//                        } else if (i > 1 && i < trip.legs.size) {
//                            // only draw an icon if walk is required in the middle of a trip
//                            val id = leg.departure.hashCode().toString()
//                            val icon = getMarkerIcon(ctx, MarkerType.WALK, backgroundColor, foregroundColor)
//
//                            addMarker(
//                                map = map,
//                                pos = leg.departure.toLatLng(),
//                                icon = icon,
//                                text = getStationText(ctx, leg, MarkerType.END),
//                                title = leg.departure.uniqueShortName ?: ""
//                            )
//                        }
//                    }
//
//                    i += 1
//                }
//
//                if (shouldZoom) {
//                    _zoomToBounds(builder.build(), false)
//                }
//            }
//        }
    }

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
    mapStyle: String
) {
    UIKitMapView(
        mapViewState as iOsMapViewState,
        compassMargins,
        isHalfHeight,
        mapPadding,
        rotateGestures,
        showLogo,
        showAttribution,
        mapStyle
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
    showAttribution: Boolean,
    mapStyle: String
) {
    val mapView = remember { MKMapView() }
    UIKitView(
        factory = {
            mapView.apply {
                println("hi mom")
                mapViewState.setMapView(this)
                this.addAnnotation(
                    MKPointAnnotation(
                        CLLocationCoordinate2DMake(51.0767179, 13.6077777),
                        "Test",
                        "foo"
                    )
                )

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
