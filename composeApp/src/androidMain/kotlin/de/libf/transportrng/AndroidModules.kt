package de.libf.transportrng

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import de.libf.transportrng.data.Db
import de.libf.transportrng.data.gps.AndroidGeocoder
import de.libf.transportrng.data.gps.AndroidGpsRepository
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.gps.OsmGeocoder
import de.libf.transportrng.data.gps.ReverseGeocoderV2
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


actual val PlatformModule = module {
    single<ObservableSettings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences(
                "settings",
                Context.MODE_PRIVATE
            )
        )
    }

    single<GpsRepository> {
        AndroidGpsRepository(
            context = androidContext()
        )
    }

    single { AndroidGeocoder(context = androidContext()) }

    single { ReverseGeocoderV2(
        geocoders = listOf(
            get<AndroidGeocoder>(),
            get<OsmGeocoder>()
        ))
    }

    single<RoomDatabase.Builder<Db>> {
        val dbFile = androidContext().getDatabasePath(Db.DATABASE_NAME)
        Room.databaseBuilder<Db>(
            context = androidContext(),
            name = dbFile.absolutePath
        )
    }

    factory {
        HttpClient(CIO)
    }
}