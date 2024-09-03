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

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.databinding.FragmentTripDetailBinding
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.BOTTOM
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.EXPANDED
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.MIDDLE
import de.grobox.transportr.trips.detail.TripUtils.getStandardFare
import de.grobox.transportr.trips.detail.TripUtils.hasFare
import de.grobox.transportr.trips.detail.TripUtils.intoCalendar
import de.grobox.transportr.trips.detail.TripUtils.share
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatRelativeTime
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.FullScreenUtil
import de.schildbach.pte.dto.Trip
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class TripDetailFragment : TransportrFragment(), Toolbar.OnMenuItemClickListener {

    companion object {
        val TAG = TripDetailFragment::class.java.simpleName
    }

    private val viewModel: TripDetailViewModel by activityViewModel()

    private var _binding: FragmentTripDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var fromTimeRel: TextView
    private lateinit var fromTime: TextView
    private lateinit var from: TextView
    private lateinit var toTime: TextView
    private lateinit var to: TextView
    private lateinit var duration: TextView
    private lateinit var durationTop: TextView
    private lateinit var price: TextView
    private lateinit var priceTop: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var topBar: View
    private lateinit var bottomBar: View
    private lateinit var list: RecyclerView
    private lateinit var closeButton: ImageView

    private val timeUpdater: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000 * 30) {
        override fun onTick(millisUntilFinished: Long) {
            viewModel.getTrip().value?.let {
                formatRelativeTime(fromTimeRel.context, it.firstDepartureTime).let {
                    fromTimeRel.apply {
                        text = it.relativeTime
                        visibility = it.visibility
                    }
                }
            }
        }

        override fun onFinish() {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTripDetailBinding.inflate(inflater, container, false)

        fromTimeRel = binding.fromTimeRel
        fromTime = binding.fromTime
        from = binding.from
        toTime = binding.toTime
        to = binding.to
        duration = binding.duration
        durationTop = binding.durationTop
        price = binding.price
        priceTop = binding.priceTop
        toolbar = binding.toolbar
        topBar = binding.topBar
        bottomBar = binding.bottomBar
        list = binding.list
        closeButton = binding.closeButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(toolbar)
        setHasOptionsMenu(true)

        toolbar.setNavigationOnClickListener { _ -> onToolbarClose() }
        toolbar.setOnMenuItemClickListener(this)
        list.layoutManager = LinearLayoutManager(context)
        bottomBar.setOnClickListener { _ -> onBottomBarClick() }


        viewModel.getTrip().observe(viewLifecycleOwner, Observer<Trip> { this.onTripChanged(it) })
        viewModel.sheetState.observe(viewLifecycleOwner, Observer<SheetState> { this.onSheetStateChanged(it) })
    }

    override fun onStart() {
        super.onStart()
        timeUpdater.start()
    }

    override fun onStop() {
        super.onStop()
        timeUpdater.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val toolbarMenu = toolbar.menu
        inflater.inflate(R.menu.trip_details, toolbarMenu)
        viewModel.tripReloadError.observe(this, Observer<String> { this.onTripReloadError(it) })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reload -> {
                item.setActionView(R.layout.actionbar_progress_actionview)
                viewModel.reloadTrip()
                return true
            }
            R.id.action_share -> {
                share(context, viewModel.getTrip().value)
                return true
            }
            R.id.action_calendar -> {
                intoCalendar(context, viewModel.getTrip().value)
                return true
            }
            else -> return false
        }
    }

    private fun onTripChanged(trip: Trip?) {
        if (trip == null) return

        val reloadMenuItem = toolbar.menu.findItem(R.id.action_reload)
        if (reloadMenuItem != null) reloadMenuItem.actionView = null

        val network = viewModel.transportNetwork.value
        val showLineName = network != null && network.hasGoodLineNames()
        val adapter = LegAdapter(trip.legs, viewModel, showLineName)
        list.adapter = adapter

        fromTime.text = formatTime(context, trip.firstDepartureTime)
        formatRelativeTime(fromTimeRel.context, trip.firstDepartureTime).let {
            fromTimeRel.apply {
                text = it.relativeTime
                visibility = it.visibility
            }
        }
        from.text = trip.from.uniqueShortName()
        toTime.text = formatTime(context, trip.lastArrivalTime)
        to.text = trip.to.uniqueShortName()
        duration.text = formatDuration(trip.duration)
        durationTop.text = getString(R.string.total_time, formatDuration(trip.duration))
        price.visibility = if (trip.hasFare()) VISIBLE else GONE
        price.text = trip.getStandardFare()
        priceTop.visibility = if (trip.hasFare()) VISIBLE else GONE
        priceTop.text = trip.getStandardFare()
    }

    private fun onToolbarClose() {
        viewModel.sheetState.value = BOTTOM
    }

    private fun onBottomBarClick() {
        viewModel.sheetState.value = MIDDLE
    }

    private fun onSheetStateChanged(sheetState: SheetState?) {
        FullScreenUtil.applyImageViewTopInset(closeButton)
        when (sheetState) {
            null -> return
            BOTTOM -> {
                closeButton.visibility = GONE
                topBar.visibility = GONE
                bottomBar.visibility = VISIBLE
            }
            MIDDLE -> {

                closeButton.visibility = GONE
                topBar.visibility = VISIBLE
                bottomBar.visibility = GONE
            }
            EXPANDED -> {
                closeButton.visibility = VISIBLE
                topBar.visibility = VISIBLE
                bottomBar.visibility = GONE
            }
        }
    }

    private fun onTripReloadError(error: String?) {
        toolbar.menu.findItem(R.id.action_reload).actionView = null
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

}
