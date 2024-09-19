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
package de.libf.transportrng.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import de.libf.transportrng.data.locations.FavoriteLocation
import de.libf.transportrng.data.locations.GenericLocation
import de.libf.transportrng.data.locations.HomeLocation
import de.libf.transportrng.data.locations.LocationDao
import de.libf.transportrng.data.locations.WorkLocation
import de.libf.transportrng.data.searches.SearchesDao
import de.libf.transportrng.data.searches.StoredSearch
import de.libf.transportrng.data.trips.LineEntity
import de.libf.transportrng.data.trips.StopEntity
import de.libf.transportrng.data.trips.TripEntity
import de.libf.transportrng.data.trips.TripLegEntity
import de.libf.transportrng.data.trips.TripLegToStopsCrossRef
import de.libf.transportrng.data.trips.TripsDao

@Database(
    version = 2,
    entities = [
        FavoriteLocation::class,
        HomeLocation::class,
        WorkLocation::class,
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
@ConstructedBy(DatabaseConstructor::class)
abstract class Db : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    abstract fun searchesDao(): SearchesDao

    abstract fun tripsDao(): TripsDao

    companion object {
        const val DATABASE_NAME: String = "transportr.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object DatabaseConstructor : RoomDatabaseConstructor<Db> {
    override fun initialize(): Db
}
