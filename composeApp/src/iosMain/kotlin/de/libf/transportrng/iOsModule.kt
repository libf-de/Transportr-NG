package de.libf.transportrng

import androidx.room.Room
import androidx.room.RoomDatabase
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import de.libf.transportrng.data.Db
import de.libf.transportrng.data.PlatformTool
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.gps.OsmGeocoder
import de.libf.transportrng.data.gps.ReverseGeocoderV2
import de.libf.transportrng.data.gps.iOsGpsRepository
import de.libf.transportrng.data.iOsPlatformTool
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

actual val PlatformModule = module {
    single<PlatformTool> {
        iOsPlatformTool()
    }

    single<ObservableSettings> {
        NSUserDefaultsSettings(
            NSUserDefaults.standardUserDefaults()
        )
    }

    single<GpsRepository> {
        iOsGpsRepository()
    }

    single { ReverseGeocoderV2(
        geocoders = listOf(
            get<OsmGeocoder>()
        ))
    }

    single<RoomDatabase.Builder<Db>> {
        val dbFilePath = documentDirectory() + "/transportr.db"
        Room.databaseBuilder<Db>(
            name = dbFilePath,
        )
    }

    factory {
        HttpClient(Darwin)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}