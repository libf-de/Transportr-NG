package de.libf.transportrng

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.maplibre.android.MapLibre

class TransportrApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        startKoin {
            // declare used Android context
            androidContext(this@TransportrApplication)

            // declare modules
            modules(
                TransportrModule,
                DatabaseModule,
                ViewModelModule,
                PlatformModule
            )
        }

        MapLibre.getInstance(applicationContext)
    }
}