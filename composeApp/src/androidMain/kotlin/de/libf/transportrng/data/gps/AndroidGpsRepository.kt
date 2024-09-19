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
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import de.libf.ptek.dto.Point
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class AndroidGpsRepository(private val context: Context) : GpsRepository {
    var locationManager: LocationManager? = null

    override fun getGpsState(): Flow<GpsState> = flow {
        if(ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            emit(GpsState.DENIED)
        else {
            if(locationManager == null) emit(GpsState.DISABLED)
            else emit(GpsState.ENABLED)
        }
    }

    override fun getLocationFlow(): Flow<Result<Point>> = callbackFlow {
        val locationManager = getSystemService(context, LocationManager::class.java)

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: android.location.Location) {
                val Point = Point.fromDouble(location.latitude, location.longitude)
                trySend(Result.success(Point))
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // 5 seconds
                10f,  // 10 meters
                locationListener
            ) ?: trySend(Result.failure(Exception("Could not get location manager")))
        } else {
            // Handle permission not granted
            trySend(Result.failure(Exception("Permission not granted")))
            close()
        }

        awaitClose {
            locationManager?.removeUpdates(locationListener)
        }
    }

}