package de.libf.transportrng.data

import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.locations.WrapLocation

class DesktopPlatformTool : PlatformTool {
    override fun showLocationOnMap(loc: WrapLocation) {

    }

    override fun shareText(text: String) {

    }

    override suspend fun addToCalendar(trip: Trip): String? {
        return null
    }

}