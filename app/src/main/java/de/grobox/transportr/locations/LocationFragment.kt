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
package de.grobox.transportr.locations

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.data.dto.KLocation
import de.grobox.transportr.databinding.FragmentLocationBinding
import de.grobox.transportr.departures.DeparturesActivity
import de.grobox.transportr.departures.DeparturesLoader
import de.grobox.transportr.locations.ReverseGeocoder.ReverseGeocoderCallback
import de.grobox.transportr.ui.map.MapViewModel
import de.grobox.transportr.utils.Constants
import de.grobox.transportr.utils.IntentUtils.findNearbyStations
import de.grobox.transportr.utils.IntentUtils.startGeoIntent
import de.grobox.transportr.utils.TransportrUtils.getCoordName
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.QueryDeparturesResult
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Date
import java.util.SortedSet
import java.util.TreeSet
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class LocationFragment : TransportrFragment(), LoaderManager.LoaderCallbacks<QueryDeparturesResult?>, ReverseGeocoderCallback,
    OnGlobalLayoutListener {

    private val viewModel: MapViewModel by activityViewModel<MapViewModel>()
    lateinit var location: WrapLocation
    private val adapter = LineAdapter()

    private lateinit var binding: FragmentLocationBinding
    private lateinit var locationIcon: ImageView
    private lateinit var locationName: TextView
    private lateinit var locationInfo: TextView
    private lateinit var linesLayout: RecyclerView
    private lateinit var nearbyStationsButton: Button
    private lateinit var nearbyStationsProgress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        val args = arguments ?: throw IllegalStateException()
        location = (args.getSerializable(Constants.WRAP_LOCATION) as WrapLocation?)!!

        binding = FragmentLocationBinding.inflate(inflater, container, false)

        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.material_personalized_color_surface_container_low
            )
        )

        viewModel.nearbyStationsFound().observe(viewLifecycleOwner) { onNearbyStationsLoaded() }

        // Location
        locationIcon = binding.locationIcon
        locationName = binding.locationName
        locationIcon.setOnClickListener { onLocationClicked() }
        locationName.setOnClickListener { onLocationClicked() }

        // Lines
        linesLayout = binding.linesLayout
        linesLayout.visibility = View.GONE
        linesLayout.adapter = adapter
        linesLayout.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        linesLayout.setOnClickListener { onLocationClicked() }

        // Location Info
        locationInfo = binding.locationInfo
        showLocation()

        if (location.location.type == KLocation.Type.COORD) {
            val geocoder = ReverseGeocoder(requireActivity(), this)
            geocoder.findLocation(location.location)
        }

        // Departures
        val departuresButton: Button = binding.departuresButton
        if (location.hasId()) {
            departuresButton.setOnClickListener {
                val intent = Intent(context, DeparturesActivity::class.java)
                //intent.putExtra(Constants.WRAP_LOCATION, location)
                startActivity(intent)
            }
        } else {
            departuresButton.visibility = View.GONE
        }

        // Nearby Stations
        nearbyStationsButton = binding.nearbyStationsButton
        nearbyStationsProgress = binding.nearbyStationsProgress
        nearbyStationsButton.setOnClickListener {
            it.visibility = View.INVISIBLE
            nearbyStationsProgress.visibility = View.VISIBLE
            findNearbyStations(context, location)
        }

        // Share Location
        val shareButton: Button = binding.shareButton
        shareButton.setOnClickListener { startGeoIntent(requireActivity(), location) }

        // Overflow Button
        val overflowButton = binding.overflowButton
        overflowButton.setOnClickListener { view: View? -> LocationPopupMenu(context, view, location).show() }

        val v: View = binding.root
        v.viewTreeObserver.addOnGlobalLayoutListener(this)

        return v
    }

    override fun onGlobalLayout() {
        // set peek distance to show view header
        if (activity == null) return
        if (linesLayout.bottom > 0) {
            viewModel.setPeekHeight(linesLayout.bottom + resources.getDimensionPixelSize(R.dimen.locationPeekPadding))
        } else if (locationInfo.bottom > 0) {
            viewModel.setPeekHeight(locationInfo.bottom + resources.getDimensionPixelSize(R.dimen.locationPeekPadding))
        }
    }

    private fun showLocation() {
        locationName.text = location.getName()
        locationIcon.setImageDrawable(ContextCompat.getDrawable(context, location.drawableInt))
        val locationInfoStr = StringBuilder()
        if (!Strings.isNullOrEmpty(location.location.place)) {
            locationInfoStr.append(location.location.place)
        }
        if (location.location.hasCoords) {
            if (locationInfoStr.isNotEmpty()) locationInfoStr.append(", ")
            locationInfoStr.append(getCoordName(location.location))
        }
        locationInfo.text = locationInfoStr
    }

    private fun onLocationClicked() {
        viewModel.selectedLocationClicked(location.latLng)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (location.hasId()) {
            val args = DeparturesLoader.getBundle(location.id, Date(), DeparturesActivity.MAX_DEPARTURES)
            loaderManager.initLoader(Constants.LOADER_DEPARTURES, args, this).forceLoad()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): DeparturesLoader {
        return DeparturesLoader(context, viewModel.transportNetwork.value, args)
    }

    override fun onLoadFinished(loader: Loader<QueryDeparturesResult?>, data: QueryDeparturesResult?) {
        if (data != null && data.status == QueryDeparturesResult.Status.OK) {
            val lines: SortedSet<Line> = TreeSet()
            for (s in data.stationDepartures) {
                if (s.lines != null) {
                    for (d in s.lines!!) lines.add(d.line)
                }
                for (d in s.departures) lines.add(d.line)
            }
            adapter.swapLines(ArrayList(lines))

            linesLayout.alpha = 0f
            linesLayout.visibility = View.VISIBLE
            linesLayout.animate().setDuration(750).alpha(1f).start()
        }
    }

    override fun onLoaderReset(loader: Loader<QueryDeparturesResult?>) { /* do nothing */ }

    @WorkerThread
    override fun onLocationRetrieved(location: WrapLocation) {
        if (activity == null) return
        runOnUiThread {
            this@LocationFragment.location = location
            showLocation()
        }
    }

    private fun onNearbyStationsLoaded() {
        nearbyStationsButton.visibility = View.VISIBLE
        nearbyStationsButton.isEnabled = false
        nearbyStationsProgress.visibility = View.INVISIBLE
    }

    companion object {
        val TAG: String = LocationFragment::class.java.name

        fun newInstance(location: WrapLocation?): LocationFragment {
            val f = LocationFragment()

            val args = Bundle()
            //args.putSerializable(Constants.WRAP_LOCATION, location)
            f.arguments = args

            return f
        }
    }
}
