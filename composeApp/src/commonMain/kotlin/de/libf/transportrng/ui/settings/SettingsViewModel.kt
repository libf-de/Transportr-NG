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

package de.libf.transportrng.ui.settings

import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanStateFlow
import com.russhwolf.settings.coroutines.getStringStateFlow
import de.libf.transportrng.data.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.grobox.transportr.networks.TransportNetworks
import de.grobox.transportr.settings.SettingsManager
import de.libf.ptek.NetworkProvider
import de.libf.ptek.NetworkProvider.WalkSpeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.basque
import transportr_ng.composeapp.generated.resources.catalan
import transportr_ng.composeapp.generated.resources.czech
import transportr_ng.composeapp.generated.resources.danish
import transportr_ng.composeapp.generated.resources.dutch
import transportr_ng.composeapp.generated.resources.english
import transportr_ng.composeapp.generated.resources.esperanto
import transportr_ng.composeapp.generated.resources.farsi
import transportr_ng.composeapp.generated.resources.french
import transportr_ng.composeapp.generated.resources.german
import transportr_ng.composeapp.generated.resources.greek
import transportr_ng.composeapp.generated.resources.hungarian
import transportr_ng.composeapp.generated.resources.italian
import transportr_ng.composeapp.generated.resources.japanese
import transportr_ng.composeapp.generated.resources.norwegian_bokmal
import transportr_ng.composeapp.generated.resources.polish
import transportr_ng.composeapp.generated.resources.portuguese_br
import transportr_ng.composeapp.generated.resources.pref_optimize_least_changes
import transportr_ng.composeapp.generated.resources.pref_optimize_least_duration
import transportr_ng.composeapp.generated.resources.pref_optimize_least_walking
import transportr_ng.composeapp.generated.resources.pref_theme_dark
import transportr_ng.composeapp.generated.resources.pref_theme_light
import transportr_ng.composeapp.generated.resources.pref_theme_value_auto
import transportr_ng.composeapp.generated.resources.pref_walk_speed_fast
import transportr_ng.composeapp.generated.resources.pref_walk_speed_normal
import transportr_ng.composeapp.generated.resources.pref_walk_speed_slow
import transportr_ng.composeapp.generated.resources.russian
import transportr_ng.composeapp.generated.resources.spanish
import transportr_ng.composeapp.generated.resources.swedish
import transportr_ng.composeapp.generated.resources.system_default
import transportr_ng.composeapp.generated.resources.taiwanese
import transportr_ng.composeapp.generated.resources.tamil
import transportr_ng.composeapp.generated.resources.turkish
import transportr_ng.composeapp.generated.resources.ukrainian

@OptIn(ExperimentalSettingsApi::class)
class SettingsViewModel(
    private val netManager: TransportNetworkManager,
    private val settings: ObservableSettings,
    private val networks: TransportNetworks,
) : TransportNetworkViewModel(netManager) {
//    val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    val allNetworks = flow {
        emit(networks.networks)
    }

    val defaultOptimize = SettingsManager.Values.OPTIMIZE_DEFAULT
    val optimize: Flow<NetworkProvider.Optimize> = settings
        .getStringStateFlow(viewModelScope, SettingsManager.OPTIMIZE, defaultOptimize)
        .map(NetworkProvider.Optimize::valueOf)
    val optimizeNames = mapOf(
        NetworkProvider.Optimize.LEAST_DURATION to Res.string.pref_optimize_least_changes,
        NetworkProvider.Optimize.LEAST_CHANGES to Res.string.pref_optimize_least_duration,
        NetworkProvider.Optimize.LEAST_WALKING to Res.string.pref_optimize_least_walking
    )

//    val defaultWalkSpeed = getString(Res.string.pref_walk_speed_value_default)
    val walkSpeed: Flow<WalkSpeed> = settings
        .getStringStateFlow(viewModelScope,SettingsManager.WALK_SPEED, SettingsManager.WALK_SPEED)
        .map(WalkSpeed::valueOf)
    val walkSpeedNames = mapOf(
        WalkSpeed.SLOW to Res.string.pref_walk_speed_slow,
        WalkSpeed.NORMAL to Res.string.pref_walk_speed_normal,
        WalkSpeed.FAST to Res.string.pref_walk_speed_fast
    )

    private val themeDark = SettingsManager.Values.THEME_DARK
    private val themeLight = SettingsManager.Values.THEME_LIGHT
    private val themeAuto = SettingsManager.Values.THEME_AUTO
    val defaultTheme = null
    val theme: Flow<Boolean?> = settings
        .getStringStateFlow(viewModelScope,SettingsManager.THEME, themeAuto)
        .map {
            when (it) {
                themeDark -> true
                themeLight -> false
                else -> defaultTheme
            }
        }
    val themeNames = mapOf(
        true to Res.string.pref_theme_dark,
        false to Res.string.pref_theme_light,
        null to Res.string.pref_theme_value_auto
    )


    val defaultLocale = SettingsManager.Values.LOCALE_DEFAULT
    val locale: Flow<String> = settings
        .getStringStateFlow(viewModelScope,SettingsManager.LANGUAGE, defaultLocale)
    val localeNames = mapOf(
        defaultLocale to Res.string.system_default,
        "en" to Res.string.english,
        "de" to Res.string.german,
        "es" to Res.string.spanish,
        "eu" to Res.string.basque,
        "fr" to Res.string.french,
        "it" to Res.string.italian,
        "nb" to Res.string.norwegian_bokmal,
        "nl" to Res.string.dutch,
        "pt_BR" to Res.string.portuguese_br,
        "tr" to Res.string.turkish,
        "ca" to Res.string.catalan,
        "pl" to Res.string.polish,
        "ta" to Res.string.tamil,
        "eo" to Res.string.esperanto,
        "cs" to Res.string.czech,
        "hu" to Res.string.hungarian,
        "ja" to Res.string.japanese,
        "ru" to Res.string.russian,
        "el" to Res.string.greek,
        "fa" to Res.string.farsi,
        "zh_TW" to Res.string.taiwanese,
        "sv" to Res.string.swedish,
        "uk" to Res.string.ukrainian,
        "da" to Res.string.danish
    )

    val defaultShowWhenLocked = true
    val showWhenLocked: Flow<Boolean> = settings.getBooleanStateFlow(viewModelScope,
        SettingsManager.SHOW_WHEN_LOCKED,
        defaultShowWhenLocked
    )

    fun setTransportNetwork(transportNetwork: TransportNetwork) {
        netManager.setTransportNetwork(transportNetwork)
    }

    fun setOptimize(optimize: NetworkProvider.Optimize) {
        settings.putString(SettingsManager.OPTIMIZE, optimize.name)
    }

    fun setWalkSpeed(walkSpeed: WalkSpeed) {
        settings.putString(SettingsManager.WALK_SPEED, walkSpeed.name)
    }

    fun setTheme(theme: Boolean?) {
        when (theme) {
            true -> settings.putString(SettingsManager.THEME, themeDark)
            false -> settings.putString(SettingsManager.THEME, themeLight)
            else -> settings.putString(SettingsManager.THEME, themeAuto)
        }
    }

    fun setLocale(locale: String) {
        settings.putString(SettingsManager.LANGUAGE, locale)
    }

    fun setShowWhenLocked(showWhenLocked: Boolean) {
        settings.putBoolean(SettingsManager.SHOW_WHEN_LOCKED, showWhenLocked)
    }
}