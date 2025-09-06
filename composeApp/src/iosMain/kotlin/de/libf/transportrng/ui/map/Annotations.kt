package de.libf.transportrng.ui.map

import androidx.compose.ui.graphics.Color
import de.libf.ptek.dto.Location
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKMapView
import platform.UIKit.UIImage
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class GenericStopAnnotation(
    private val pLocation: Location,
    private val pTitle: String? = null,
    private val pSubtitle: String? = null
) : NSObject(), MKAnnotationProtocol {
    override fun coordinate(): CValue<CLLocationCoordinate2D> {
        return CLLocationCoordinate2DMake(
            pLocation.latAsDouble,
            pLocation.lonAsDouble
        )
    }
    override fun title(): String? { return this.pTitle }
    override fun subtitle(): String? { return this.pSubtitle }
}

@OptIn(ExperimentalForeignApi::class)
class TintableAnnotation(
    private val pLocation: Location,
    private val pTitle: String? = null,
    private val pSubtitle: String? = null,
    val fgColor: Color? = null,
    val bgColor: Color? = null,
    val iconName: String
) : NSObject(), MKAnnotationProtocol {
    override fun coordinate(): CValue<CLLocationCoordinate2D> {
        return CLLocationCoordinate2DMake(
            pLocation.latAsDouble,
            pLocation.lonAsDouble
        )
    }
    override fun title(): String? { return this.pTitle }
    override fun subtitle(): String? { return this.pSubtitle }
}

object TintableAnotationIcons {
    val TRIP_STOP = "trip_stop"
    val TRIP_START = "trip_begin"
    val TRIP_CHANGE = "trip_change"
    val TRIP_END = "trip_end"
    val WALK = "trip_walk"
    val TRIP_BACKGROUND = "trip_bg"
}