package de.libf.transportrng.data.locations

import de.libf.ptek.dto.Location
import kotlin.math.roundToInt

fun Location.getCoordName(): String {
    return "${this.latAsDouble.roundToThreeDecimals()}/${this.lonAsDouble.roundToThreeDecimals()}"
}

private fun Double.roundToThreeDecimals(): Double {
    return (this * 1000).roundToInt() / 1000.0
}