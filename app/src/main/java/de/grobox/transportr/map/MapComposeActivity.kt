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
package de.grobox.transportr.map

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import de.grobox.transportr.composables.BaseComposableCompat
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.map.MapViewModel
import de.grobox.transportr.ui.trips.search.DirectionsActivity
import de.grobox.transportr.utils.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class MapComposeActivity : ComponentActivity() /*, LocationViewListener, NavigationView.OnNavigationItemSelectedListener*/ {
    private val viewModel: MapViewModel by viewModel()

    public override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        //if (BuildConfig.DEBUG) enableStrictMode()

        setContent {
            BaseComposableCompat {
                //MapScreen(viewModel)
            }
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == null) return

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            viewModel.setGeoUri(intent.data!!)
        } else if (intent.action == DirectionsActivity.ACTION_SEARCH) {
            val location = intent.getSerializableExtra(Constants.WRAP_LOCATION) as WrapLocation?
            viewModel.selectLocation(location)
            viewModel.findNearbyStations(location!!)
        }
    }

//    private fun checkAndShowChangelog() {
//        val cl = TransportrChangeLog(this, settingsManager)
//        if (cl.isFirstRun && !cl.isFirstRunEver) {
//            cl.getMaterialDialog(cl.isFirstRunEver).show()
//        }
//    }
//
//    private fun enableStrictMode() {
//        val threadPolicy = ThreadPolicy.Builder()
//        threadPolicy.detectAll()
//        threadPolicy.penaltyLog()
//        StrictMode.setThreadPolicy(threadPolicy.build())
//
//        val vmPolicy = VmPolicy.Builder()
//        vmPolicy.detectAll()
//        vmPolicy.penaltyLog()
//        StrictMode.setVmPolicy(vmPolicy.build())
//    }
//
//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.settings -> {
//                startActivity(Intent(this, SettingsActivity::class.java))
//            }
//
//            R.id.changelog -> {
//                TransportrChangeLog(this, settingsManager).getMaterialDialog(true).show()
//            }
//
//            R.id.about -> {
//                startActivity(Intent(this, AboutActivity::class.java))
//            }
//
//            R.id.contributors -> {
//                startActivity(Intent(this, ContributorsActivity::class.java))
//            }
//
//            R.id.bug_report -> {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bug_tracker))))
//            }
//        }
//        closeDrawer()
//        return true
//    }
}
