package de.libf.transportrng.data

import de.libf.transportrng.data.locations.WrapLocation

interface PlatformTool {
    fun showLocationOnMap(loc: WrapLocation)
}