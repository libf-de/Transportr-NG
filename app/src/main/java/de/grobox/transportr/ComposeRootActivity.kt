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

package de.grobox.transportr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.navigation.NavigationView
import org.maplibre.android.geometry.LatLng
import de.grobox.transportr.BuildConfig
import de.grobox.transportr.R
import de.grobox.transportr.TransportrActivity
import de.grobox.transportr.about.AboutActivity
import de.grobox.transportr.about.ContributorsActivity
import de.grobox.transportr.composables.BaseComposableCompat
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.databinding.ActivityMapBinding
import de.grobox.transportr.locations.LocationFragment
import de.grobox.transportr.locations.LocationView.LocationViewListener
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.locations.WrapLocation.WrapType
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.settings.SettingsActivity
import de.grobox.transportr.trips.search.DirectionsActivity
import de.grobox.transportr.ui.TransportrChangeLog
import de.grobox.transportr.utils.Constants
import de.grobox.transportr.utils.FullScreenUtil.Companion.applyTopInset
import de.grobox.transportr.utils.FullScreenUtil.Companion.drawBehindStatusbar
import de.grobox.transportr.utils.IntentUtils.findDirections
import de.grobox.transportr.utils.OnboardingBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class ComposeRootActivity : ComponentActivity() /*, LocationViewListener, NavigationView.OnNavigationItemSelectedListener*/ {

    public override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        //if (BuildConfig.DEBUG) enableStrictMode()

        setContent {
            BaseComposableCompat {
                TransportrNavigationController()
            }
        }
    }
}
