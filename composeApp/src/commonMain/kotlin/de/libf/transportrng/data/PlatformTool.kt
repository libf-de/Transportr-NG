package de.libf.transportrng.data

import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.locations.WrapLocation

interface PlatformTool {
    fun showLocationOnMap(loc: WrapLocation)
    fun shareText(text: String)

    suspend fun addToCalendar(trip: Trip): String?
}