package de.libf.transportrng

import androidx.room.Room
import androidx.room.RoomDatabase
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import de.libf.transportrng.data.Db
import de.libf.transportrng.data.DesktopPlatformTool
import de.libf.transportrng.data.PlatformTool
import de.libf.transportrng.data.gps.DesktopGpsRepository
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.gps.OsmGeocoder
import de.libf.transportrng.data.gps.ReverseGeocoderV2
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import org.koin.dsl.module
import java.io.File
import java.util.prefs.Preferences

actual val PlatformModule = module {
    single<PlatformTool> {
        DesktopPlatformTool()
    }

    single<ObservableSettings> {
        PreferencesSettings(
            Preferences.userNodeForPackage(DesktopPlatformTool::class.java)
        )
    }

    single<GpsRepository> {
        DesktopGpsRepository()
    }

    single { ReverseGeocoderV2(
        geocoders = listOf(
            get<OsmGeocoder>()
        ))
    }

    single<RoomDatabase.Builder<Db>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
        Room.databaseBuilder<Db>(
            name = dbFile.absolutePath,
        )
    }

    factory {
        HttpClient(Java)
    }
}