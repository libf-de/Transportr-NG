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

package de.grobox.transportr.trips.detail

import androidx.annotation.LayoutRes
import de.grobox.transportr.R
import de.grobox.transportr.map.GpsMapFragment
import de.schildbach.pte.dto.Trip
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

class TripMapFragment : GpsMapFragment<TripDetailViewModel>() {

    companion object {
        @JvmField
        val TAG: String = TripMapFragment::class.java.simpleName
    }

    override val layout: Int
        @LayoutRes
        get() = R.layout.fragment_trip_map

    override val viewModel: TripDetailViewModel by activityViewModel()

    override fun onMapReady(mapboxMap: MapLibreMap) {
        super.onMapReady(mapboxMap)

        // set bottom padding, so everything gets centered in top half of screen
        setPadding(bottom = mapView.height / 2)

        viewModel.getTrip().observe(this) { onTripChanged(it) }
        viewModel.getZoomLocation().observe(this) { this.animateTo(it) }
        viewModel.getZoomLeg().observe(this) { this.animateToBounds(it) }
    }

    private fun onTripChanged(trip: Trip?) {
        if (trip == null) return

        val tripDrawer = TripDrawer(context)
        val zoom = viewModel.isFreshStart.value ?: throw IllegalStateException()
        tripDrawer.draw(map!!, trip, zoom)
        if (zoom) {
            // do not zoom again when returning to this Fragment
            viewModel.isFreshStart.value = false
        }
    }

    private fun animateTo(latLng: LatLng?) {
        animateTo(latLng, 16)
    }

}
