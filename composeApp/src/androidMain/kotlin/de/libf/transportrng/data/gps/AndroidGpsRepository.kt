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

package de.libf.transportrng.data.gps

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import de.libf.ptek.dto.Point
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class AndroidGpsRepository(
    private val context: Context,
    private val provider: String = LocationManager.FUSED_PROVIDER,
    private val minDeltaMeters: Float = 50f
) : GpsRepository {

    var lastLocation: Point? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    var listenerRegistered = false
    override var isEnabled = true
        private set

    override fun getGpsStateFlow(): Flow<GpsState> = callbackFlow {
        locationManager = getSystemService(context, LocationManager::class.java) ?: run {
            trySend(GpsState.Disabled)
            return@callbackFlow
        }

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                lastLocation = Point.fromDouble(location.latitude, location.longitude).also {
                    if(isEnabled)
                        trySend(
                            GpsState.Enabled(it, location.accuracy < 100)
                        )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {
                if(provider == this@AndroidGpsRepository.provider && isEnabled) {
                    lastLocation?.let {
                        trySend(GpsState.Enabled(it, true))
                    } ?: trySend(GpsState.EnabledSearching)
                } else {
                    trySend(GpsState.Disabled)
                }
            }
            override fun onProviderDisabled(provider: String) {
                if(provider == this@AndroidGpsRepository.provider) {
                    trySend(GpsState.Disabled)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if(locationManager?.getLocationEnabled() == true && isEnabled)
                if(lastLocation == null)
                    trySend(GpsState.EnabledSearching)
                else
                    trySend(GpsState.Enabled(lastLocation!!, true))
            else
                trySend(GpsState.Disabled)

            startLocationUpdates()
        } else {
            // Handle permission not granted
            trySend(GpsState.Denied)
            close()
        }

        awaitClose {
            locationListener?.let {
                locationManager?.removeUpdates(it)
            }

            locationListener = null
        }
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        locationListener?.let { listener ->
            locationManager?.removeUpdates(listener)
            listenerRegistered = false
        }
    }

    private fun startLocationUpdates() {
        if (isEnabled && !listenerRegistered && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationListener?.let { locationListener ->
                locationManager?.also {
                    listenerRegistered = true
                }?.requestLocationUpdates(
                    provider,
                    10000, // 10 seconds
                    minDeltaMeters,
                    locationListener
                )

            }
        }
    }

    private fun LocationManager.getLocationEnabled(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            this.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            val mode = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }
}