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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.navigation.NavigationView
import de.grobox.transportr.BuildConfig
import de.grobox.transportr.R
import de.grobox.transportr.TransportrActivity
import de.grobox.transportr.about.AboutActivity
import de.grobox.transportr.about.ContributorsActivity
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.databinding.ActivityMapBinding
import de.grobox.transportr.locations.LocationFragment
import de.grobox.transportr.locations.LocationView.LocationViewListener
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.locations.WrapLocation.WrapType
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.settings.SettingsActivity
import de.grobox.transportr.ui.TransportrChangeLog
import de.grobox.transportr.ui.map.MapViewModel
import de.grobox.transportr.ui.trips.search.DirectionsActivity
import de.grobox.transportr.utils.Constants
import de.grobox.transportr.utils.FullScreenUtil.Companion.applyTopInset
import de.grobox.transportr.utils.FullScreenUtil.Companion.drawBehindStatusbar
import de.grobox.transportr.utils.IntentUtils.findDirections
import de.grobox.transportr.utils.OnboardingBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.maplibre.android.geometry.LatLng
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class MapActivity : TransportrActivity(), LocationViewListener, NavigationView.OnNavigationItemSelectedListener {
    private val viewModel: MapViewModel by viewModel()

    private lateinit var binding: ActivityMapBinding

    //private var search: LocationView? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private var locationFragment: LocationFragment? = null
    private var transportNetworkInitialized = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) enableStrictMode()

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        drawBehindStatusbar(this)
        applyTopInset( binding.searchCardView, 16)

        binding.menu.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            currentFocus?.let {
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }
            binding.drawerLayout.open()
        }

        binding.search.setLocationViewListener(this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.search.clearLocation()
                    binding.search.reset()
                    viewModel.setPeekHeight(0)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // unused
            }
        })

        // get view model and observe data
        viewModel.transportNetwork.observe(this, this::onTransportNetworkChanged)
        viewModel.home.observe(this, binding.search::setHomeLocation)
        viewModel.work.observe(this, binding.search::setWorkLocation)
        viewModel.locations.observe(this, binding.search::setFavoriteLocations)
        viewModel.mapClicked.observe(this) {
            onMapClicked()
        }
        viewModel.markerClicked.observe(this) {
            onMarkerClicked()
        }
        viewModel.getSelectedLocation().observe(this, this::onLocationSelected)
        viewModel.getSelectedLocationClicked().observe(this, this::onSelectedLocationClicked)
        viewModel.getPeekHeight().observe(this, bottomSheetBehavior::setPeekHeight)

        binding.directionsFab.setOnClickListener {
            val from = WrapLocation(WrapType.GPS)
            val to: WrapLocation? = locationFragment?.location.takeIf { locationFragmentVisible() }

            findDirections(this@MapActivity, from, null, to)
        }

        onNewIntent(intent)

        if (savedInstanceState == null) {
            showSavedSearches()
            checkAndShowChangelog()
        } else {
            locationFragment = supportFragmentManager.findFragmentByTag(LocationFragment.TAG) as LocationFragment?
        }
    }

    private fun showSavedSearches() {
        val f = SavedSearchesFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomSheet, f, SavedSearchesFragment::class.java.simpleName)
            .commitNow() // otherwise takes some time and empty bottomSheet will not be shown
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        viewModel.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO)
    }

    private fun onTransportNetworkChanged(network: TransportNetwork?) {
        if (transportNetworkInitialized) {
            viewModel.selectLocation(null)
            binding.search.setLocation(null)
            closeDrawer()
            showSavedSearches()
            recreate()
        } else {
            // it didn't really change, this is just the first notification from LiveData Observer
            binding.search.setTransportNetwork(network)
            transportNetworkInitialized = true
        }
    }

    private fun setupDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView) { v: View, insets: WindowInsetsCompat ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left,
                sb.top,
                sb.right,
                sb.bottom
            )
            insets
        }

        val header = binding.navigationView.getHeaderView(0)
        header.setOnClickListener {
            openPickNetworkProviderActivity()
        }

        // Network
        manager.transportNetwork.value?.let {
            val title = header.findViewById<TextView>(R.id.network_name)
            val description = header.findViewById<TextView>(R.id.network_description)
            val image = header.findViewById<ImageView>(R.id.network_image)
            title.text = it.getName(this)
            description.text = it.getDescription(this)
            image.setImageResource(it.logo)
        }

        // Network 2
        manager.getTransportNetwork(2)?.let { network2: TransportNetwork ->
            val image = header.findViewById<ImageView>(R.id.network_image_two)
            image.setImageResource(network2.logo)
            image.setOnClickListener {
                manager.setTransportNetwork(network2)
                closeDrawer()
            }
        }

        // Network 3
        manager.getTransportNetwork(3)?.let { network3: TransportNetwork ->
            val image = header.findViewById<ImageView>(R.id.network_image_three)
            image.setImageResource(network3.logo)
            image.setOnClickListener {
                manager.setTransportNetwork(network3)
                closeDrawer()
            }
        }
    }

    private fun closeDrawer() {
        binding.drawerLayout.close()
    }

    private fun openPickNetworkProviderActivity() {
        val intent = Intent(this, PickTransportNetworkActivity::class.java)
        ActivityCompat.startActivity(this, intent, null)
    }

    override fun onLocationItemClick(loc: WrapLocation, type: FavLocationType) {
        viewModel.selectLocation(loc)
    }

    override fun onLocationCleared(type: FavLocationType) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        viewModel.selectLocation(null)
        binding.search.postDelayed({ // show dropdown again after it got hidden by hiding the bottom sheet
            binding.search.onClick()
        }, 500)
    }

    private fun onLocationSelected(loc: WrapLocation?) {
        if (loc == null) return

        locationFragment = LocationFragment.newInstance(loc)
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomSheet, locationFragment!!, LocationFragment.TAG)
            .commit() // takes some time and empty bottomSheet will not be shown
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // show on-boarding dialog
        if (settingsManager.showLocationFragmentOnboarding()) {
            OnboardingBuilder(this)
                .setTarget(R.id.bottomSheet)
                .setPrimaryText(R.string.onboarding_location_title)
                .setSecondaryText(R.string.onboarding_location_message)
                .setPromptStateChangeListener { _: MaterialTapTargetPrompt?, state: Int ->
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                        settingsManager.locationFragmentOnboardingShown()
                        viewModel.selectedLocationClicked(loc.latLng)
                    }
                }
                .show()
        }
    }

    private fun onSelectedLocationClicked(latLng: LatLng?) {
        if (latLng == null) return
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun onMapClicked() {
        binding.search.clearFocus() // also hides soft keyboard
    }

    private fun onMarkerClicked() {
        locationFragment?.let {
            binding.search.setLocation(it.location)
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun locationFragmentVisible(): Boolean {
        return locationFragment != null &&
                locationFragment!!.isVisible &&
                bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
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

    private fun checkAndShowChangelog() {
        val cl = TransportrChangeLog(this, settingsManager)
        if (cl.isFirstRun && !cl.isFirstRunEver) {
            cl.getMaterialDialog(cl.isFirstRunEver).show()
        }
    }

    private fun enableStrictMode() {
        val threadPolicy = ThreadPolicy.Builder()
        threadPolicy.detectAll()
        threadPolicy.penaltyLog()
        StrictMode.setThreadPolicy(threadPolicy.build())

        val vmPolicy = VmPolicy.Builder()
        vmPolicy.detectAll()
        vmPolicy.penaltyLog()
        StrictMode.setVmPolicy(vmPolicy.build())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.changelog -> {
                TransportrChangeLog(this, settingsManager).getMaterialDialog(true).show()
            }

            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }

            R.id.contributors -> {
                startActivity(Intent(this, ContributorsActivity::class.java))
            }

            R.id.bug_report -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bug_tracker))))
            }
        }
        closeDrawer()
        return true
    }
}
