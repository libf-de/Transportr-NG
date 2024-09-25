package de.libf.transportrng.data.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import androidx.core.content.ContextCompat
import de.libf.ptek.dto.Point
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose


class AndroidGpsRepository(private val context: Context) : GpsRepository {

    @Volatile
    private var isEnabledInternal = true // Enabled by default

    override val isEnabled: Boolean
        get() = isEnabledInternal

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @OptIn(FlowPreview::class)
    @SuppressLint("MissingPermission")
    override fun getGpsStateFlow(): Flow<GpsState> = callbackFlow<GpsState> {
        if (!hasLocationPermission()) {
            trySend(GpsState.Denied)
            close() // Terminate the flow since we cannot proceed without permissions
            return@callbackFlow
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trySend(GpsState.Disabled)
            close() // Terminate the flow as GPS provider is disabled
            return@callbackFlow
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val point = Point.fromDouble(location.latitude, location.longitude)
                val gpsState = GpsState.Enabled(
                    location = point,
                    isAccurate = isLocationAccurate(location)
                )
                trySend(gpsState)
            }

            override fun onProviderDisabled(provider: String) {
                trySend(GpsState.Disabled)
            }

            override fun onProviderEnabled(provider: String) {
                // When the provider is enabled, we start searching for location
                trySend(GpsState.EnabledSearching)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Deprecated, but can be used if needed for older Android versions
            }
        }

        try {
            // Start requesting location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )

            // Emit the last known location if available
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                val point = Point.fromDouble(
                    lastKnownLocation.latitude,
                    lastKnownLocation.longitude
                )
                val gpsState = GpsState.Enabled(
                    location = point,
                    isAccurate = isLocationAccurate(lastKnownLocation)
                )
                trySend(gpsState)
            } else {
                // If no last known location, indicate that we're searching
                trySend(GpsState.EnabledSearching)
            }
        } catch (e: SecurityException) {
            trySend(GpsState.Denied)
            close() // Terminate the flow since we cannot proceed without permissions
            return@callbackFlow
        } catch (e: Exception) {
            trySend(GpsState.Error(e.message ?: "Error obtaining location"))
        }

        // Await until the flow is closed (i.e., when the consumer cancels the flow)
        awaitClose {
            // Stop location updates when flow collection ends
            locationManager.removeUpdates(locationListener)
        }
    }
        .filterByDistance(50f)
        .debounce(5000)
        .distinctUntilChanged() // To avoid emitting duplicate GPS states

    override fun setEnabled(enabled: Boolean) {
        // Since the repository is enabled by default and starts/stops based on flow collection,
        // this function can control an internal flag if needed.
        // We'll update the internal state but actual start/stop is managed by flow collection.
        isEnabledInternal = enabled
    }

    private fun hasLocationPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationAccurate(location: Location): Boolean {
        return location.hasAccuracy() && location.accuracy <= ACCURACY_THRESHOLD_METERS
    }

    companion object {
        private const val MIN_TIME_BW_UPDATES = 2000L // 2 seconds
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 0f // 0 meters
        private const val ACCURACY_THRESHOLD_METERS = 20 // Accuracy within 20 meters
    }
}