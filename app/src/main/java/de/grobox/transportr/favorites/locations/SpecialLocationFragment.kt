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

package de.grobox.transportr.favorites.locations

import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.databinding.FragmentSpecialLocationBinding
import de.grobox.transportr.favorites.trips.FavoriteTripListener
import de.grobox.transportr.locations.LocationView
import de.grobox.transportr.locations.LocationsViewModel
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.settings.SettingsManager
import org.koin.android.ext.android.inject
import javax.annotation.ParametersAreNonnullByDefault


@ParametersAreNonnullByDefault
abstract class SpecialLocationFragment : DialogFragment(), LocationView.LocationViewListener {

    val settingsManager: SettingsManager by inject()
    abstract val viewModel: LocationsViewModel

    var listener: FavoriteTripListener? = null

    private var _binding: FragmentSpecialLocationBinding? = null;
    private val binding get() = _binding!!

    private lateinit var loc: LocationView

    @get:StringRes
    protected abstract val hint: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSpecialLocationBinding.inflate(inflater, container, false)
        val v = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize LocationView
        loc = binding.locationInput
        loc.setHint(hint)
        loc.setLocationViewListener(this)

        // Get view model and observe data
//        viewModel.transportNetwork.observe(viewLifecycleOwner, Observer {
//                transportNetwork -> transportNetwork?.let { loc.setTransportNetwork(it) }
//        })
//
//        viewModel.locations.observe(viewLifecycleOwner, Observer { favoriteLocations ->
//            favoriteLocations?.let {
//                loc.setFavoriteLocations(it)
//                loc.post { loc.onClick() }  // don't know why this only works when posted
//            }
//        })

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.setCanceledOnTouchOutside(true)

        // set width to match parent and show keyboard
        val window = dialog?.window
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.TOP)
            window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    override fun onLocationItemClick(loc: WrapLocation, type: FavLocationType) {
        onSpecialLocationSet(loc)
        dialog?.cancel()
    }

    protected abstract fun onSpecialLocationSet(location: WrapLocation)

    override fun onLocationCleared(type: FavLocationType) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
