package de.libf.transportrng

import android.os.Build
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.libf.transportrng.data.settings.SettingsManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val settingsMgr by inject<SettingsManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if(settingsMgr.isFirstRun) {
            settingsMgr.isFirstRun = false
            askForLocationPermissions()
        }


        setContent {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            LaunchedEffect(currentBackStackEntry) {
                showWhenLocked(settingsMgr.showWhenLocked() &&
                        currentBackStackEntry?.destination?.route.isRouteToShowWhenLocked())
            }

            App(navController)
        }
    }

    @Suppress("DEPRECATION")
    private fun showWhenLocked(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(enabled)
        } else {
            if(enabled)
                window.addFlags(FLAG_SHOW_WHEN_LOCKED)
            else
                window.clearFlags(FLAG_SHOW_WHEN_LOCKED)
        }
    }


    fun askForLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {}

        locationPermissionRequest.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}