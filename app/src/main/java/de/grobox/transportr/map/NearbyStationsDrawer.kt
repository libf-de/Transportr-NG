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
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.data.dto.KProduct
import de.grobox.transportr.locations.WrapLocation
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap


class NearbyStationsDrawer(context: Context) : MapDrawer(context) {

    private val nearbyLocations = HashMap<Marker, KLocation>()

    fun draw(map: MapLibreMap, nearbyStations: List<KLocation>) {
        val builder = LatLngBounds.Builder()
        for (location in nearbyStations) {
            val icon = getIconForProduct(location.products)
            if (!location.hasLocation) continue
            if (nearbyLocations.containsValue(location)) continue
            if (icon == null) continue
            val marker = markLocation(map, location, icon, location.uniqueShortName ?: "???")
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

    private fun getIconForProduct(p: Set<KProduct>?): Icon? {
        val firstProduct = if (p.isNullOrEmpty()) null else p.iterator().next()
        val res = when (firstProduct) {
            KProduct.HIGH_SPEED_TRAIN -> R.drawable.product_high_speed_train_marker
            KProduct.REGIONAL_TRAIN -> R.drawable.product_regional_train_marker
            KProduct.SUBURBAN_TRAIN -> R.drawable.product_suburban_train_marker
            KProduct.SUBWAY -> R.drawable.product_subway_marker
            KProduct.TRAM -> R.drawable.product_tram_marker
            KProduct.BUS -> R.drawable.product_bus_marker
            KProduct.FERRY -> R.drawable.product_ferry_marker
            KProduct.CABLECAR -> R.drawable.product_cablecar_marker
            KProduct.ON_DEMAND -> R.drawable.product_on_demand_marker
            else -> R.drawable.product_bus_marker
        }
        val drawable = ContextCompat.getDrawable(context, res) ?: throw RuntimeException()
        return drawable.toIcon()
    }

}
