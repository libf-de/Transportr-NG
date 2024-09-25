package de.libf.transportrng.data.gps

import de.libf.ptek.dto.Point
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLAuthorizationStatusVar
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

class iOsGpsRepository(private val minDeltaMeters: Float = 50f) : GpsRepository {

    override var isEnabled = true
        private set

    private var locationManager: CLLocationManager? = null

    @OptIn(ExperimentalForeignApi::class, FlowPreview::class)
    override fun getGpsStateFlow(): Flow<GpsState> = callbackFlow {
        locationManager = CLLocationManager().also { locationManager ->
            locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
            locationManager.requestWhenInUseAuthorization()
            locationManager.delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    (didUpdateLocations.lastOrNull() as? CLLocation)?.let { location ->
                        location.coordinate
                            .useContents { Point.fromDouble(this.latitude, this.longitude) }
                            .let {
                                trySend(GpsState.Enabled(
                                    it,
                                    location.horizontalAccuracy < 100
                                ))
                            }
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    println("Location manager failed with error: ${didFailWithError.localizedDescription}")
                    trySend(GpsState.Error(didFailWithError.localizedDescription))
                }

                override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
                    when (didChangeAuthorizationStatus) {
                        platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse,
                        platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways -> {
                            println("Location access granted")

                            if(isEnabled) {
                                trySend(GpsState.EnabledSearching)
                                locationManager.startUpdatingLocation()
                            } else {
                                trySend(GpsState.Disabled)
                            }
                        }
                        platform.CoreLocation.kCLAuthorizationStatusDenied-> {
                            println("Location access denied/disabled")

                            trySend(GpsState.Disabled)
                        }
                        platform.CoreLocation.kCLAuthorizationStatusRestricted -> {
                            println("Location access denied")

                            trySend(GpsState.Denied)
                        }

                        platform.CoreLocation.kCLAuthorizationStatusNotDetermined -> {
                            println("Location access not determined")

                            trySend(GpsState.Denied)
                        }
                        else -> {
                            println("Unknown authorization status")

                            trySend(GpsState.Error("unknown authorization status"))
                        }
                    }
                }
            }
        }

        awaitClose {
            locationManager?.stopUpdatingLocation()
            locationManager = null
        }
    }
        .filterByDistance(minDeltaMeters)
        .debounce(5000)
        .distinctUntilChanged()


    override fun setEnabled(enabled: Boolean) {
        this.isEnabled = enabled
        if(!enabled) {
            locationManager?.stopUpdatingLocation()
        } else {
            locationManager?.startUpdatingLocation()
        }
    }
}