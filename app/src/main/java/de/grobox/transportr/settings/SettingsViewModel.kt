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

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.preference.PreferenceManager
import de.grobox.transportr.R
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.NetworkProvider.WalkSpeed

class SettingsViewModel(
    application: TransportrApplication,
    netManager: TransportNetworkManager
) : TransportNetworkViewModel(application, netManager) {

    val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    val defaultOptimize = application.getString(R.string.pref_optimize_value_default)
    val optimize: LiveData<NetworkProvider.Optimize> = SharedPreferenceStringLiveData(
        sharedPrefs = sharedPrefs,
        key = SettingsManager.OPTIMIZE,
        defValue = defaultOptimize
    ).map(NetworkProvider.Optimize::valueOf)
    val optimizeNames = mapOf(
        NetworkProvider.Optimize.LEAST_DURATION to R.string.pref_optimize_least_changes,
        NetworkProvider.Optimize.LEAST_CHANGES to R.string.pref_optimize_least_duration,
        NetworkProvider.Optimize.LEAST_WALKING to R.string.pref_optimize_least_walking
    )

    val defaultWalkSpeed = application.getString(R.string.pref_walk_speed_value_default)
    val walkSpeed: LiveData<WalkSpeed> = SharedPreferenceStringLiveData(
        sharedPrefs = sharedPrefs,
        key = SettingsManager.WALK_SPEED,
        defValue = defaultWalkSpeed
    ).map(WalkSpeed::valueOf)
    val walkSpeedNames = mapOf(
        WalkSpeed.SLOW to R.string.pref_walk_speed_slow,
        WalkSpeed.NORMAL to R.string.pref_walk_speed_normal,
        WalkSpeed.FAST to R.string.pref_walk_speed_fast
    )

    private val themeDark = application.getString(R.string.pref_theme_value_dark)
    private val themeLight = application.getString(R.string.pref_theme_value_light)
    private val themeAuto = application.getString(R.string.pref_theme_value_auto)
    val defaultTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    val theme: LiveData<Int> = SharedPreferenceStringLiveData(
        sharedPrefs = sharedPrefs,
        key = SettingsManager.THEME,
        defValue = themeAuto
    ).map {
        when (it) {
            themeDark -> AppCompatDelegate.MODE_NIGHT_YES
            themeLight -> AppCompatDelegate.MODE_NIGHT_NO
            else -> defaultTheme
        }
    }
    val themeNames = mapOf(
        AppCompatDelegate.MODE_NIGHT_YES to R.string.pref_theme_dark,
        AppCompatDelegate.MODE_NIGHT_NO to R.string.pref_theme_light,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM to R.string.pref_theme_value_auto,
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY to R.string.pref_theme_value_auto
    )


    val defaultLocale = application.getString(R.string.pref_language_value_default)
    val locale: LiveData<String> = SharedPreferenceStringLiveData(
        sharedPrefs = sharedPrefs,
        key = SettingsManager.LANGUAGE,
        defValue = defaultLocale
    )

    val localeNames = mapOf(
        defaultLocale to R.string.system_default,
        "en" to R.string.english,
        "de" to R.string.german,
        "es" to R.string.spanish,
        "eu" to R.string.basque,
        "fr" to R.string.french,
        "it" to R.string.italian,
        "nb" to R.string.norwegian_bokmal,
        "nl" to R.string.dutch,
        "pt_BR" to R.string.portuguese_br,
        "tr" to R.string.turkish,
        "ca" to R.string.catalan,
        "pl" to R.string.polish,
        "ta" to R.string.tamil,
        "eo" to R.string.esperanto,
        "cs" to R.string.czech,
        "hu" to R.string.hungarian,
        "ja" to R.string.japanese,
        "ru" to R.string.russian,
        "el" to R.string.greek,
        "fa" to R.string.farsi,
        "zh_TW" to R.string.taiwanese,
        "sv" to R.string.swedish,
        "uk" to R.string.ukrainian,
        "da" to R.string.danish
    )

    val defaultShowWhenLocked = true
    val showWhenLocked: LiveData<Boolean> = SharedPreferenceBooleanLiveData(
        sharedPrefs = sharedPrefs,
        key = SettingsManager.SHOW_WHEN_LOCKED,
        defValue = defaultShowWhenLocked
    )

    fun setOptimize(optimize: NetworkProvider.Optimize) {
        sharedPrefs.edit().putString(SettingsManager.OPTIMIZE, optimize.name).apply()
    }

    fun setWalkSpeed(walkSpeed: WalkSpeed) {
        sharedPrefs.edit().putString(SettingsManager.WALK_SPEED, walkSpeed.name).apply()
    }

    fun setTheme(theme: Int) {
        when (theme) {
            AppCompatDelegate.MODE_NIGHT_YES -> sharedPrefs.edit().putString(SettingsManager.THEME, themeDark).apply()
            AppCompatDelegate.MODE_NIGHT_NO -> sharedPrefs.edit().putString(SettingsManager.THEME, themeLight).apply()
            else -> sharedPrefs.edit().putString(SettingsManager.THEME, themeAuto).apply()
        }
    }

    fun setLocale(locale: String) {
        sharedPrefs.edit().putString(SettingsManager.LANGUAGE, locale).apply()
    }

    fun setShowWhenLocked(showWhenLocked: Boolean) {
        sharedPrefs.edit().putBoolean(SettingsManager.SHOW_WHEN_LOCKED, showWhenLocked).apply()
    }



    private abstract class SharedPreferenceLiveData<T>(val sharedPrefs: SharedPreferences,
                                               val key: String,
                                               val defValue: T) : LiveData<T>() {

        private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == this.key) {
                value = getValueFromPreferences(key, defValue)
            }
        }

        abstract fun getValueFromPreferences(key: String, defValue: T): T

        override fun onActive() {
            super.onActive()
            value = getValueFromPreferences(key, defValue)
            sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        }

        override fun onInactive() {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
            super.onInactive()
        }
    }

    private class SharedPreferenceStringLiveData(sharedPrefs: SharedPreferences, key: String, defValue: String) :
        SharedPreferenceLiveData<String>(sharedPrefs, key, defValue) {
        override fun getValueFromPreferences(key: String, defValue: String): String = sharedPrefs.getString(key, defValue) ?: defValue
    }

    private class SharedPreferenceBooleanLiveData(sharedPrefs: SharedPreferences, key: String, defValue: Boolean) :
        SharedPreferenceLiveData<Boolean>(sharedPrefs, key, defValue) {
        override fun getValueFromPreferences(key: String, defValue: Boolean): Boolean = sharedPrefs.getBoolean(key, defValue)
    }

    init {

    }
}