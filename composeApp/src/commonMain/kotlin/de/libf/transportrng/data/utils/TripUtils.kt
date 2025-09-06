package de.libf.transportrng.data.utils

import androidx.compose.runtime.Composable
import de.libf.ptek.dto.IndividualLeg
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.Position
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import de.libf.ptek.dto.min
import de.libf.transportrng.ui.trips.search.formatAsLocal
import de.libf.transportrng.ui.trips.search.formatAsLocalDate
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.app_name
import transportr_ng.composeapp.generated.resources.created_by
import transportr_ng.composeapp.generated.resources.for_x_min
import transportr_ng.composeapp.generated.resources.meter
import transportr_ng.composeapp.generated.resources.platform
import transportr_ng.composeapp.generated.resources.times_include_delays
import transportr_ng.composeapp.generated.resources.trip_share_date
import transportr_ng.composeapp.generated.resources.walk
import transportr_ng.composeapp.generated.resources.website

suspend fun Trip.toShareString(): String {
    val sb = StringBuilder()

    val calendar = Instant.fromEpochMilliseconds(this.firstDepartureTime)
    if(!calendar.isToday()) {
        sb.append(getString(Res.string.trip_share_date, calendar.formatAsLocalDate())).append("\n\n")
    }

    this.legs.forEachIndexed { i, it ->
        sb.append(it.toShareString(
            isFirstLeg = i == 0,
            isLastLeg = i == this.legs.size - 1
        )).append("\n\n")
    }

    if (calendar.isToday()) sb.append(getString(Res.string.times_include_delays)).append("\n\n")
    sb.append(getString(Res.string.created_by, getString(Res.string.app_name)))
        .append("\n").append(getString(Res.string.website))
    return sb.toString()
}

suspend fun Leg.toShareString(isFirstLeg: Boolean, isLastLeg: Boolean): String {
    var str = ""

    if (this is PublicLeg) {
        str += "${this.departureTime.formatTime()} ${this.departure.getName()}"
        // show departure position if existing
        if (this.departurePosition != null) {
            str += " " + getString(Res.string.platform, this.departurePosition.toShareString())
        }
        str += "\n  ${this.line.product.getEmoji()} "
        this.line.label?.let {
            str += it
            this.destination?.getName()?.let {
                str += " → ${it}"
            }
        }
        str += "\n${this.arrivalTime.formatTime()} ${this.arrival.getName()}"

        // add arrival position if existing
        if (this.arrivalPosition != null) {
            str += " ${getString(Res.string.platform, this.arrivalPosition.toShareString())}"
        }
    } else if (this is IndividualLeg) {
        if(isFirstLeg)
            str += "${this.departureTime.formatTime()} ${this.departure.getName()}\n"

        str += "  \uD83D\uDEB6 ${getString(Res.string.walk)} "
        if (this.distance > 0) str += getString(Res.string.meter, this.distance)
        if (this.min > 0) str += " ${getString(Res.string.for_x_min, this.min)}"
        str += "\n"

        if(isLastLeg)
            str += "\n${this.arrivalTime.formatTime()} ${this.arrival.getName()}"
    }

    return str
}

private fun Position?.toShareString() = listOfNotNull(this?.name, this?.section)
    .joinToString("")


private fun Product?.getEmoji(): String = when (this) {
    Product.HIGH_SPEED_TRAIN -> "🚄"
    Product.REGIONAL_TRAIN -> "🚆"
    Product.SUBURBAN_TRAIN -> "🚈"
    Product.SUBWAY -> "🚇"
    Product.TRAM -> "🚊"
    Product.BUS -> "🚌"
    Product.FERRY -> "⛴️"
    Product.CABLECAR -> "🚡"
    Product.ON_DEMAND -> "🚖"
    null -> ""
    Product.FOOTWAY -> "🚶"
    Product.TRANSFER -> "🔁"
    Product.SECURE_CONNECTION -> "🔐"
    Product.DO_NOT_CHANGE -> "✋🏼"
    Product.UNKNOWN -> "❓"
}
