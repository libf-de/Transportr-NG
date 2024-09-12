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

package de.grobox.transportr.settings

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.grobox.transportr.R
import de.grobox.transportr.map.MapActivity
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.settings.SettingsManager.Companion.LANGUAGE
import de.grobox.transportr.settings.SettingsManager.Companion.THEME
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    internal val manager: TransportNetworkManager by inject()
    private lateinit var networkPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, s: String?) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)

        // Fill in current transport network if available
        networkPref = findPreference("pref_key_network")!!
//        manager.transportNetwork.observe(this) {
//            onTransportNetworkChanged(it)
//        }

        networkPref.setOnPreferenceClickListener {
            if (activity == null || view == null) return@setOnPreferenceClickListener false

            val intent = Intent(activity, PickTransportNetworkActivity::class.java)
            val x : Float = view?.x ?: view?.findFocus()?.x ?: 0f
            val y : Float = view?.y ?: view?.findFocus()?.y ?: 0f
            val options = ActivityOptionsCompat.makeScaleUpAnimation(requireView(), x.toInt(), y.toInt(), 0, 0)
            ActivityCompat.startActivity(requireActivity(), intent, options.toBundle())
            true
        }

        (findPreference(THEME) as Preference?)?.let {
            it.setOnPreferenceChangeListener { _, newValue ->
                when(newValue) {
                    "light" -> setDefaultNightMode(MODE_NIGHT_NO)
                    "dark" -> setDefaultNightMode(MODE_NIGHT_YES)
                    else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }
        }
        (findPreference(LANGUAGE) as Preference?)?.let {
            it.setOnPreferenceChangeListener { _, _ ->
                reload()
                true
            }
        }
    }

    private fun onTransportNetworkChanged(network: TransportNetwork?) {
        context?.let { networkPref.summary = network?.getName(it) ?: "(unknown)" }
    }

    private fun reload() {
        // getActivity().recreate() does only recreate SettingActivity

        activity?.let {
            val intent = Intent(context, MapActivity::class.java)
            intent.flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
            it.startActivity(intent)
            it.finish()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            // show a material design 3 dialog instead of the default md2 preference dialog
            is ListPreference -> {
                val currentValueIndex = preference.entryValues.indexOf(preference.value)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(preference.title)
                    .setSingleChoiceItems(preference.entries, currentValueIndex) { dialog, index ->
                        val newValue = preference.entryValues[index]
                        if (preference.callChangeListener(newValue)) {
                            preference.value = newValue as String
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}
