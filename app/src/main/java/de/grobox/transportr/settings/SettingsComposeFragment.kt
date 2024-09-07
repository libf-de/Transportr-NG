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

package de.grobox.transportr.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import de.grobox.transportr.composables.BaseComposableCompat
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.ui.settings.SettingsScreen
import org.koin.android.ext.android.inject

class SettingsComposeFragment : Fragment() {

    val networkManager: TransportNetworkManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BaseComposableCompat {
                    SettingsScreen(
                        onSelectNetworkClicked = {
                            val intent = Intent(requireContext(), PickTransportNetworkActivity::class.java)
                            val x : Float = view?.x ?: view?.findFocus()?.x ?: 0f
                            val y : Float = view?.y ?: view?.findFocus()?.y ?: 0f
                            val options = ActivityOptionsCompat.makeScaleUpAnimation(requireView(), x.toInt(), y.toInt(), 0, 0)
                            ActivityCompat.startActivity(requireActivity(), intent, options.toBundle())
                        }
                    )
                }
            }
        }
    }
}