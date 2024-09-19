package de.libf.transportrng.data.utils

import androidx.compose.runtime.Composable
import de.libf.ptek.dto.Fare
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.locations.getCoordName
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_action_about
import transportr_ng.composeapp.generated.resources.product_bus
import transportr_ng.composeapp.generated.resources.product_cablecar
import transportr_ng.composeapp.generated.resources.product_ferry
import transportr_ng.composeapp.generated.resources.product_high_speed_train
import transportr_ng.composeapp.generated.resources.product_on_demand
import transportr_ng.composeapp.generated.resources.product_regional_train
import transportr_ng.composeapp.generated.resources.product_suburban_train
import transportr_ng.composeapp.generated.resources.product_subway
import transportr_ng.composeapp.generated.resources.product_tram

fun Trip.hasFare(): Boolean {
    return fares?.isNotEmpty() ?: false
}

fun Trip.getStandardFare(): String? {
    fares?.find { fare -> fare.type == Fare.Type.ADULT }?.let {
        //val format = NumberFormat.getCurrencyInstance()
        //format.currency = Currency.getInstance(it.currency)
        //return format.format(it.fare)
        return "${it.fare} ${it.currency}"
    }
    return null
}

fun Location?.getName(): String? {
    return when {
        this == null -> ""
        this.type == Location.Type.COORD -> this.getCoordName()
        this.uniqueShortName != null -> this.uniqueShortName
        else -> ""
    }
}


private fun Int?.largerThan(i: Int): Boolean {
    return this?.let { it > i } ?: false
}

fun Trip.hasProblem(): Boolean {
    if (!isTravelable) return true
    for (leg in legs) {
        if (leg !is PublicLeg) continue
        if (!leg.message.isNullOrEmpty()) return true
        if (!leg.line.message.isNullOrEmpty()) return true
    }
    return false
}

fun Int.largerThan(i: Int): Boolean {
    return this > i
}

@Composable
fun Pair<StringResource?, String>.name(): String {
    return this.first?.let { stringResource(it) } ?: this.second
}

fun Product?.getDrawable(): DrawableResource = when (this) {
    Product.HIGH_SPEED_TRAIN -> Res.drawable.product_high_speed_train
    Product.REGIONAL_TRAIN -> Res.drawable.product_regional_train
    Product.SUBURBAN_TRAIN -> Res.drawable.product_suburban_train
    Product.SUBWAY -> Res.drawable.product_subway
    Product.TRAM -> Res.drawable.product_tram
    Product.BUS -> Res.drawable.product_bus
    Product.FERRY -> Res.drawable.product_ferry
    Product.CABLECAR -> Res.drawable.product_cablecar
    Product.ON_DEMAND -> Res.drawable.product_on_demand
    null -> Res.drawable.product_bus
    else -> Res.drawable.ic_action_about
}