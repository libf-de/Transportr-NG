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

package de.libf.transportrng

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.russhwolf.settings.ObservableSettings
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworks
import de.libf.transportrng.data.settings.SettingsManager
import de.libf.transportrng.data.Db
import de.libf.transportrng.data.gps.OsmGeocoder
import de.libf.transportrng.data.locations.LocationDao
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.searches.SearchesDao
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrng.data.trips.TripsDao
import de.libf.transportrng.data.trips.TripsRepository
import de.libf.transportrng.ui.departures.DeparturesViewModel
import de.libf.transportrng.ui.directions.DirectionsViewModel
import de.libf.transportrng.ui.map.MapViewModel
import de.libf.transportrng.ui.map.sheets.LocationDetailSheetViewModel
import de.libf.transportrng.ui.map.sheets.SavedSearchesSheetViewModel
import de.libf.transportrng.ui.settings.SettingsViewModel
import de.libf.transportrng.ui.trips.TripDetailViewModel
import de.libf.transportrngocations.CombinedSuggestionRepository
import de.libf.transportrngocations.SuggestLocationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect val PlatformModule: Module

val TransportrModule = module {
    single {
        TransportNetworks(
            httpClient = get()
        )
    }

    single {
        SettingsManager(
            settings = get<ObservableSettings>()
        )
    }

    single {
        TransportNetworkManager(
            settingsManager = get(),
            transportNetworks = get()
        )
    }

    single {
        TripsRepository(
            networkManager = get(),
            settingsManager = get(),
            locationRepository = get(),
            searchesRepository = get(),
            tripsDao = get()
        )
    }

    single {
        LocationRepository(
            locationDao = get(),
            transportNetworkManager = get()
        )
    }
    single {
        SearchesRepository(
            searchesDao = get(),
            locationDao = get(),
            transportNetworkManager = get()
        )
    }

    single {
        SuggestLocationsRepository(
            manager = get(),
            dispatcher = Dispatchers.IO
        )
    }

    single {
        CombinedSuggestionRepository(
            suggestLocationsRepository = get(),
            locationRepository = get()
        )
    }

    single { OsmGeocoder() }

//    single { ReverseGeocoderV2(
//        geocoders = listOf(
//            get<AndroidGeocoder>(),
//            get<OsmGeocoder>()
//        ))
//    }

//    factory {
//        PositionController(
//            context = androidContext(),
//            geoCoder = get()
//        )
//    }
}

val DatabaseModule = module {
    single<LocationDao> { get<Db>().locationDao() }

    single<SearchesDao> { get<Db>().searchesDao() }

    single<TripsDao> { get<Db>().tripsDao()}


    single<Db> {
        get<RoomDatabase.Builder<Db>>()
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

val ViewModelModule = module {
    viewModel {
        MapViewModel(
            transportNetworkManager = get(),
            locationRepository = get(),
            searchesRepository = get(),
            gpsRepository = get(),
            combinedSuggestionRepository = get()
        )
    }

    viewModel {
        DirectionsViewModel(
            transportNetworkManager = get(),
            settingsManager = get(),
            locationRepository = get(),
            searchesRepository = get(),
//            positionController = get(),
            combinedSuggestionRepository = get(),
            tripsRepository = get(),
            geocoder = get(),
            gpsRepository = get()
        )
    }

    viewModel {
        TripDetailViewModel(
            transportNetworkManager = get(),
            gpsRepository = get(),
            settingsManager = get(),
            tripsRepository = get(),
            platformTool = get()
        )
    }

    viewModel {
        SettingsViewModel(
            settings = get(),
            netManager = get(),
            networks = get()
        )
    }

    viewModel {
        DeparturesViewModel(
            transportManager = get()
        )
    }

    viewModel {
        LocationDetailSheetViewModel(
            transportNetworkManager = get(),
            locationRepository = get(),
            searchesRepository = get()
        )
    }

    viewModel {
        SavedSearchesSheetViewModel(
            transportNetworkManager = get(),
            locationRepository = get(),
            searchesRepository = get()
        )
    }
}

//fun inject(fragment: MapFragment?)
//fun inject(fragment: LocationFragment?)
//fun inject(fragment: SavedSearchesFragment?)
//fun inject(fragment: de.libf.transportrng.trips.search.SavedSearchesFragment?)
//fun inject(fragment: DirectionsFragment?)
//fun inject(fragment: TripsFragment?)
//fun inject(fragment: TripMapFragment?)
//fun inject(fragment: TripDetailFragment?)
//fun inject(fragment: SettingsFragment?)
//fun inject(fragment: HomePickerDialogFragment?)
//fun inject(fragment: WorkPickerDialogFragment?)
//fun inject(fragment: ProductDialogFragment?)