@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package de.libf.transportrng.ui.map

import androidx.compose.ui.graphics.Color
import de.libf.ptek.dto.Location
import de.libf.transportrng.data.maplibrecompat.LatLng
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectMeta
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.darwin.NSObject
import platform.objc.objc_getRequiredClass

class TripBeginAnnotation(
    private val pLocation: Location,
    val foregroundColor: Color,
    val backgroundColor: Color,
    private val pTitle: String? = null,
    private val pSubtitle: String? = null,
) : NSObject(), MKAnnotationProtocol {
    override fun coordinate(): CValue<CLLocationCoordinate2D> {
        return CLLocationCoordinate2DMake(
            pLocation.latAsDouble,
            pLocation.lonAsDouble
        )
    }

    override fun title(): String? { return pTitle }
    override fun subtitle(): String? { return pSubtitle }

    private fun test(): String { return "hi" }
}