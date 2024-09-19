/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.map

import android.content.Context
import androidx.core.content.ContextCompat
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Product
import de.libf.transportrng.R
import de.libf.transportrng.data.locations.WrapLocation
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.product_bus_marker
import transportr_ng.composeapp.generated.resources.product_cablecar_marker
import transportr_ng.composeapp.generated.resources.product_ferry_marker
import transportr_ng.composeapp.generated.resources.product_high_speed_train_marker
import transportr_ng.composeapp.generated.resources.product_on_demand_marker
import transportr_ng.composeapp.generated.resources.product_regional_train_marker
import transportr_ng.composeapp.generated.resources.product_suburban_train_marker
import transportr_ng.composeapp.generated.resources.product_subway_marker
import transportr_ng.composeapp.generated.resources.product_tram_marker


class NearbyStationsDrawer(context: Context) : MapDrawer(context) {

    private val nearbyLocations = HashMap<Marker, Location>()

    fun draw(map: MapLibreMap, nearbyStations: List<Location>) {
        val builder = LatLngBounds.Builder()
        for (location in nearbyStations) {
            val icon = getIconForProduct(location.products)
            if (!location.hasLocation) continue
            if (nearbyLocations.containsValue(location)) continue
            if (icon == null) continue
            val marker = marLocation(map, location, icon, location.uniqueShortName ?: "???")
            marker?.let {
                nearbyLocations.put(marker, location)
                builder.include(marker.position)
            }
        }
        zoomToBounds(map, builder, true)
    }

    fun getClickedNearbyStation(marker: Marker): WrapLocation? {
        if (nearbyLocations.containsKey(marker)) {
            return nearbyLocations[marker]?.let { WrapLocation(it) }
        }
        return null
    }

    fun reset() {
        nearbyLocations.clear()
    }

    private fun getIconForProduct(p: Set<Product>?): Icon? {
        val firstProduct = if (p.isNullOrEmpty()) null else p.iterator().next()
//        val res = when (firstProduct) {
//            Product.HIGH_SPEED_TRAIN -> Res.drawable.product_high_speed_train_marker
//            Product.REGIONAL_TRAIN -> Res.drawable.product_regional_train_marker
//            Product.SUBURBAN_TRAIN -> Res.drawable.product_suburban_train_marker
//            Product.SUBWAY -> Res.drawable.product_subway_marker
//            Product.TRAM -> Res.drawable.product_tram_marker
//            Product.BUS -> Res.drawable.product_bus_marker
//            Product.FERRY -> Res.drawable.product_ferry_marker
//            Product.CABLECAR -> Res.drawable.product_cablecar_marker
//            Product.ON_DEMAND -> Res.drawable.product_on_demand_marker
//            else -> Res.drawable.product_bus_marker
//        }
//        val drawable = ContextCompat.getDrawable(context, res) ?: throw RuntimeException()
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_trip_walk) ?: throw RuntimeException()
        return drawable.toIcon()
    }

}
