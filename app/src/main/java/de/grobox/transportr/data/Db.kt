/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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
package de.grobox.transportr.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.GenericLocation
import de.grobox.transportr.data.locations.HomeLocation
import de.grobox.transportr.data.locations.LocationDao
import de.grobox.transportr.data.locations.WorLocation
import de.grobox.transportr.data.searches.SearchesDao
import de.grobox.transportr.data.searches.StoredSearch
import de.grobox.transportr.data.trips.LineEntity
import de.grobox.transportr.data.trips.StopEntity
import de.grobox.transportr.data.trips.TripEntity
import de.grobox.transportr.data.trips.TripLegEntity
import de.grobox.transportr.data.trips.TripLegToStopsCrossRef
import de.grobox.transportr.data.trips.TripsDao

@Database(
    version = 2,
    entities = [
        FavoriteLocation::class,
        HomeLocation::class,
        WorLocation::class,
        StoredSearch::class,

        GenericLocation::class,
        TripEntity::class,
        LineEntity::class,
        StopEntity::class,
        TripLegToStopsCrossRef::class,
        TripLegEntity::class
    ]
)
@TypeConverters(Converters::class)
abstract class Db : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun searchesDao(): SearchesDao

    abstract fun tripsDao(): TripsDao

    companion object {
        const val DATABASE_NAME: String = "transportr.db"
    }
}
