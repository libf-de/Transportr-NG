package de.libf.transportrng.data

import de.libf.transportrng.data.locations.WrapLocation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark

@OptIn(ExperimentalForeignApi::class)
class iOsPlatformTool : PlatformTool {
    override fun showLocationOnMap(loc: WrapLocation) {
        val coordinate = CLLocationCoordinate2DMake(
            latitude = loc.latLng.latitude,
            longitude = loc.latLng.longitude
        )

        val mapItem = MKMapItem(
            placemark = MKPlacemark(
                coordinate = coordinate
            )
        )

        mapItem.name = loc._getName()

        mapItem.openInMapsWithLaunchOptions(launchOptions = null)
    }
}