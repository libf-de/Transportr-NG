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

import androidx.room.Room
import de.grobox.transportr.data.Db
import de.grobox.transportr.data.locations.LocationDao
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesDao
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.locations.CombinedSuggestionRepository
import de.grobox.transportr.locations.SuggestLocationsRepository
import de.grobox.transportr.map.MapViewModel
import de.grobox.transportr.map.PositionController
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.settings.SettingsViewModel
import de.grobox.transportr.trips.detail.TripDetailViewModel
import de.grobox.transportr.trips.search.DirectionsViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val TransportrModule = module {
    single {
        SettingsManager(context = androidContext())
    }
    single {
        TransportNetworkManager(settingsManager = get())
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

    factory {
        PositionController(context = androidContext())
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
            positionController = get(),
            combinedSuggestionRepository = get()
        )
    }

    viewModel {
        TripDetailViewModel(
            application = androidApplication() as TransportrApplication,
            transportNetworkManager = get(),
            positionController = get(),
            settingsManager = get()
        )
    }

    viewModel {
        SettingsViewModel(
            application = androidApplication() as TransportrApplication,
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