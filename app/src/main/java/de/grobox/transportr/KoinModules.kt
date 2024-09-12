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

package de.grobox.transportr

import android.content.Context
import androidx.room.Room
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import de.grobox.transportr.data.Db
import de.grobox.transportr.data.gps.AndroidGeocoder
import de.grobox.transportr.data.gps.AndroidGpsRepository
import de.grobox.transportr.data.gps.GpsRepository
import de.grobox.transportr.data.gps.OsmGeocoder
import de.grobox.transportr.data.gps.ReverseGeocoderV2
import de.grobox.transportr.data.locations.LocationDao
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesDao
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.data.trips.TripsDao
import de.grobox.transportr.data.trips.TripsRepository
import de.grobox.transportr.locations.CombinedSuggestionRepository
import de.grobox.transportr.locations.SuggestLocationsRepository
import de.grobox.transportr.map.PositionController
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.ui.directions.DirectionsViewModel
import de.grobox.transportr.ui.map.MapViewModel
import de.grobox.transportr.ui.settings.SettingsViewModel
import de.grobox.transportr.ui.trips.TripDetailViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val TransportrModule = module {
    single<ObservableSettings> {
        SharedPreferencesSettings(androidContext().getSharedPreferences("settings", Context.MODE_PRIVATE))
    }

    single {
        SettingsManager(
            context = androidContext(),
            settings = get<ObservableSettings>()
        )
    }

    single {
        TransportNetworkManager(settingsManager = get())
    }

    single {
        TripsRepository(
            ctx = androidContext(),
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

    single<GpsRepository> {
        AndroidGpsRepository(
            context = androidContext()
        )
    }

    single { AndroidGeocoder(context = androidContext()) }
    single { OsmGeocoder() }
    single { ReverseGeocoderV2(
        geocoders = listOf(
            get<AndroidGeocoder>(),
            get<OsmGeocoder>()
        ))
    }

    factory {
        PositionController(
            context = androidContext(),
            geoCoder = get()
        )
    }
}

val DatabaseModule = module {
    single<Db> {
        Room.databaseBuilder<Db>(
            androidContext(),
            Db::class.java, Db.DATABASE_NAME
        ).build();
    }

    single<LocationDao> { get<Db>().locationDao() }

    single<SearchesDao> { get<Db>().searchesDao() }

    single<TripsDao> { get<Db>().tripsDao()}
}

val ViewModelModule = module {
    viewModel {
        MapViewModel(
            application = androidApplication() as TransportrApplication,
            transportNetworkManager = get(),
            locationRepository = get(),
            searchesRepository = get(),
            positionController = get(),
            combinedSuggestionRepository = get()
        )
    }

    viewModel {
        DirectionsViewModel(
            application = androidApplication() as TransportrApplication,
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
            application = androidApplication() as TransportrApplication,
            transportNetworkManager = get(),
            positionController = get(),
            settingsManager = get(),
            tripsRepository = get()
        )
    }

    viewModel {
        SettingsViewModel(
            application = androidApplication() as TransportrApplication,
            settings = get(),
            netManager = get()
        )
    }
}

//fun inject(fragment: MapFragment?)
//fun inject(fragment: LocationFragment?)
//fun inject(fragment: SavedSearchesFragment?)
//fun inject(fragment: de.grobox.transportr.trips.search.SavedSearchesFragment?)
//fun inject(fragment: DirectionsFragment?)
//fun inject(fragment: TripsFragment?)
//fun inject(fragment: TripMapFragment?)
//fun inject(fragment: TripDetailFragment?)
//fun inject(fragment: SettingsFragment?)
//fun inject(fragment: HomePickerDialogFragment?)
//fun inject(fragment: WorkPickerDialogFragment?)
//fun inject(fragment: ProductDialogFragment?)