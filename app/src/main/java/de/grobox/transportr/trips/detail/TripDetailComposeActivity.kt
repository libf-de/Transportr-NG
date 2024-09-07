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
package de.grobox.transportr.ui.trips.detail

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.grobox.transportr.R
import de.grobox.transportr.composables.BaseComposableCompat
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.ThreeStateBottomSheetBehavior
import de.grobox.transportr.ui.trips.TripDetailViewModel
import de.grobox.transportr.utils.FullScreenUtil.Companion.applyTopInset
import de.grobox.transportr.utils.FullScreenUtil.Companion.drawBehindStatusbar
import de.schildbach.pte.dto.Trip
import org.koin.androidx.viewmodel.ext.android.viewModel
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class TripDetailComposeActivity : ComponentActivity() {
    private val viewModel: TripDetailViewModel by viewModel()

    private lateinit var bottomSheetBehavior: ThreeStateBottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BaseComposableCompat {
//                TripDetailComposable(
//                    viewModel = viewModel,
//                    setBarColor = { statusBar, navBar ->
//                        window.statusBarColor = statusBar.toArgb()
//                        window.navigationBarColor = navBar.toArgb()
//                    },
//                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
//                )
            }
        }

        val trip = intent.getSerializableExtra(TRIP) as Trip?
        val from = intent.getSerializableExtra(FROM) as WrapLocation?
        val via = intent.getSerializableExtra(VIA) as WrapLocation?
        val to = intent.getSerializableExtra(TO) as WrapLocation?
        //viewModel.setTrip(trip!!)
        viewModel.from = from
        viewModel.via = via
        viewModel.to = to

        if (viewModel.showWhenLocked()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }

        }

        //viewModel.sheetState.observe(this) { sheetState: TripDetailViewModel.SheetState? -> this.onSheetStateChanged(sheetState) }

//        if (savedInstanceState == null) {
//            viewModel.sheetState.setValue(TripDetailViewModel.SheetState.MIDDLE)
//            supportFragmentManager.beginTransaction()
//                .add(R.id.topContainer, TripMapFragment(), TripMapFragment.TAG)
//                .add(R.id.bottomContainer, TripDetailFragment(), TAG)
//                .commit()
//
//            showOnboarding()
//        }
//
//        (findViewById<View>(R.id.backView) as ImageButton).setOnClickListener { view: View? -> onBackPressedDispatcher.onBackPressed() }
    }

//    private fun showOnboarding() {
//        if (settingsManager.showTripDetailFragmentOnboarding()) {
//            OnboardingBuilder(this)
//                .setTarget(R.id.bottomContainer)
//                .setPrimaryText(R.string.onboarding_location_title)
//                .setSecondaryText(R.string.onboarding_location_message)
//                .setPromptStateChangeListener { _: MaterialTapTargetPrompt?, state: Int ->
//                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
//                        settingsManager.tripDetailOnboardingShown()
//                        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
//                    }
//                }
//                .show()
//        }
//    }

    private fun onSheetStateChanged(sheetState: TripDetailViewModel.SheetState?) {
        if (sheetState == null) return
        when (sheetState) {
            TripDetailViewModel.SheetState.BOTTOM -> {
                bottomSheetBehavior!!.setBottom()
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                drawBehindStatusbar(this)
            }

            TripDetailViewModel.SheetState.MIDDLE -> {
                bottomSheetBehavior.isHideable = true // ensures it can be swiped down
                bottomSheetBehavior!!.setMiddle()
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                drawBehindStatusbar(this)
                applyTopInset(findViewById(R.id.appBarLayout))
            }

            TripDetailViewModel.SheetState.EXPANDED -> drawBehindStatusbar(this)
        }
    }

    companion object {
        const val TRIP: String = "de.schildbach.pte.dto.Trip"
        const val FROM: String = "de.schildbach.pte.dto.Trip.from"
        const val VIA: String = "de.schildbach.pte.dto.Trip.via"
        const val TO: String = "de.schildbach.pte.dto.Trip.to"
    }
}
