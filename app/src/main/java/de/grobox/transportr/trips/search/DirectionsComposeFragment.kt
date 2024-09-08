/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2024 Torsten Grote
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

package de.grobox.transportr.ui.trips.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import de.grobox.transportr.composables.BaseComposableCompat
import de.grobox.transportr.ui.directions.DirectionsViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DirectionsComposeFragment() : Fragment() {
    val viewModel: DirectionsViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BaseComposableCompat {
//                    DirectionsComposable(
//                        viewModel = viewModel,
//                        onSelectDepartureClicked = {
//                            if (viewModel.lastQueryCalendar.value == null) throw IllegalStateException()
//                            val fragment = TimeDateFragment.newInstance(viewModel.lastQueryCalendar.value!!, viewModel.isDeparture.value!!)
//                            fragment.setTimeDateListener(viewModel)
//                            fragment.show(requireActivity().supportFragmentManager, TimeDateFragment.TAG)
//                        },
//                        onSelectDepartureLongClicked = {
//                            viewModel.resetCalender()
//                        },
//                        tripClicked = {
//                            startActivity(
//                                Intent(context, TripDetailComposeActivity::class.java).apply {
//                                    putExtra(TripDetailActivity.TRIP, it)
//                                    // unfortunately, PTE does not save these locations reliably in the Trip object
//                                    putExtra(TripDetailActivity.FROM, viewModel.fromLocation.value)
//                                    putExtra(TripDetailActivity.VIA, viewModel.viaLocation.value)
//                                    putExtra(TripDetailActivity.TO, viewModel.toLocation.value)
//                                }
//                            )
//                        }
//                    )
                }
            }
        }
    }
}