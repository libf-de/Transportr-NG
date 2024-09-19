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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.intl.Locale
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import de.libf.ptek.NetworkId
import de.libf.ptek.NetworkProvider.Optimize
import de.libf.ptek.NetworkProvider.WalkSpeed
import de.libf.ptek.dto.Product
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res


class SettingsManager constructor(private val settings: Settings) {

    object Values {
        const val LOCALE_DEFAULT = "default"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_AUTO = "auto"
        const val WALKSPEED_DEFAULT = "NORMAL"
        const val OPTIMIZE_DEFAULT = "LEAST_DURATION"
    }

    val locale: Locale
        get() { //pref_language_value_default
            val default = Values.LOCALE_DEFAULT
            val str = settings.getString(LANGUAGE, default) ?: default
            return when {
                str == default -> Locale.current
                else -> Locale(str)
            }
        }

    val theme: Boolean?
        get() {
            val dark = Values.THEME_DARK
            val light = Values.THEME_LIGHT
            val auto = Values.THEME_AUTO
            return when (settings.getString(THEME, auto)) {
                dark -> true
                light -> false
                else -> null
            }
        }

    val walkSpeed: WalkSpeed
        get() {
            return try {
                val default = Values.WALKSPEED_DEFAULT
                WalkSpeed.valueOf(settings.getString(WALK_SPEED, default) ?: default)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                WalkSpeed.NORMAL
            }
        }

    val optimize: Optimize
        get() {
            return try {
                val default = Values.OPTIMIZE_DEFAULT
                Optimize.valueOf(settings.getString(OPTIMIZE, default) ?: default)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Optimize.LEAST_DURATION
            }
        }

    fun showLocationFragmentOnboarding(): Boolean = settings.getBoolean(LOCATION_ONBOARDING, true)
    fun locationFragmentOnboardingShown() {
        settings.putBoolean(LOCATION_ONBOARDING, false)
    }

    fun showTripDetailFragmentOnboarding(): Boolean = settings.getBoolean(TRIP_DETAIL_ONBOARDING, true)
    fun tripDetailOnboardingShown() {
        settings.putBoolean(TRIP_DETAIL_ONBOARDING, false)
    }

    fun getNetworkId(i: Int): NetworkId? {
        var networkSettingsStr = NETWORK_ID_1
        if (i == 2) networkSettingsStr = NETWORK_ID_2
        else if (i == 3) networkSettingsStr = NETWORK_ID_3

        val networkStr = settings.getString(networkSettingsStr, "").takeIf { it.isNotEmpty() } ?: return null

        return try {
            NetworkId.valueOf(networkStr)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun setNetworkId(newNetworkId: NetworkId) {
        val networkId1 = settings.getString(NETWORK_ID_1, "")
        if (networkId1 == newNetworkId.name) {
            return  // same network selected
        }
        val networkId2 = settings.getString(NETWORK_ID_2, "")
        if (networkId2 != newNetworkId.name) {
            settings.putString(NETWORK_ID_3, networkId2)
        }
        settings.putString(NETWORK_ID_2, networkId1)
        settings.putString(NETWORK_ID_1, newNetworkId.name)
    }

    fun showWhenLocked(): Boolean {
        return settings.getBoolean(SHOW_WHEN_LOCKED, true)
    }

    fun setPreferredProducts(selected: Set<Product>) {
        Product.ALL.toSet().forEach { product ->
            settings.putBoolean(LAST_PRODUCT_PREFIX + product.name, product in selected)
        }
    }

    fun getPreferredProducts(): Set<Product> {
        val firstTime = Product.ALL.none {
            val cand: Boolean? = settings[LAST_PRODUCT_PREFIX + it.name]
            cand != null
        }
        if (firstTime) {
            setPreferredProducts(Product.ALL)
            return Product.ALL
        }

        val products = mutableSetOf<Product>()
        Product.ALL.toSet().forEach { product ->
            if (settings.getBoolean(LAST_PRODUCT_PREFIX + product.name, false)) {
                products.add(product)
            }
        }
        return products
    }

    companion object {
        private const val NETWORK_ID_1 = "NetworkId"
        private const val NETWORK_ID_2 = "NetworkId2"
        private const val NETWORK_ID_3 = "NetworkId3"

        internal const val LANGUAGE = "pref_key_language"
        internal const val THEME = "pref_key_theme"
        internal const val SHOW_WHEN_LOCKED = "pref_key_show_when_locked"
        internal const val WALK_SPEED = "pref_key_walk_speed"
        internal const val OPTIMIZE = "pref_key_optimize"
        private const val LOCATION_ONBOARDING = "locationOnboarding"
        private const val TRIP_DETAIL_ONBOARDING = "tripDetailOnboarding"
        private const val LAST_PRODUCT_PREFIX = "pref_key_last_product_prefix_"
    }

}
