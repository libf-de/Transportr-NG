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
package de.grobox.transportr.data.locations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.libf.ptek.dto.Location
import de.libf.ptek.NetworkId
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    //	@Query("SELECT * FROM locations WHERE networkId = :networkId")
    //	LiveData<List<GenericLocation>> getAllLocations(NetworkId networkId);
    @Query("SELECT * FROM locations WHERE networkId = :networkId")
    fun getAllLocationsAsFlow(networkId: NetworkId?): Flow<List<GenericLocation>>

    // FavoriteLocation
    //	@Query("SELECT * FROM locations WHERE networkId = :networkId")
    //	LiveData<List<FavoriteLocation>> getFavoriteLocations(NetworkId networkId);
    @Query("SELECT * FROM locations WHERE networkId = :networkId")
    fun getFavoriteLocationsAsFlow(networkId: NetworkId?): Flow<List<FavoriteLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavoriteLocation(location: FavoriteLocation): Long

    @Query("SELECT * FROM locations WHERE uid = :uid")
    suspend fun getFavoriteLocation(uid: Long): FavoriteLocation?

    @Query("SELECT * FROM locations WHERE networkId = :networkId AND type = :type AND id IS :id AND lat = :lat AND lon = :lon AND place IS :place AND name IS :name")
    fun getFavoriteLocation(
        networkId: NetworkId?,
        type: Location.Type?,
        id: String?,
        lat: Int,
        lon: Int,
        place: String?,
        name: String?
    ): FavoriteLocation?

    // HomeLocation
    //	@Query("SELECT * FROM home_locations WHERE networkId = :networkId")
    //	LiveData<HomeLocation> getHomeLocation(NetworkId networkId);
    @Query("SELECT * FROM home_locations WHERE networkId = :networkId")
    fun getHomeLocationAsFlow(networkId: NetworkId?): Flow<HomeLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHomeLocation(location: HomeLocation): Long

    /* This is just for tests to ensure, there's only ever one home location per network */
    @Query("SELECT COUNT(uid) FROM home_locations WHERE networkId = :networkId")
    fun countHomes(networkId: NetworkId?): Int

    // WorLocation
    @Query("SELECT * FROM work_locations WHERE networkId = :networkId")
    fun getWorLocationAsFlow(networkId: NetworkId?): Flow<WorLocation>

    //	@Query("SELECT * FROM work_locations WHERE networkId = :networkId")
    //	LiveData<WorLocation> getWorLocation(NetworkId networkId);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWorLocation(location: WorLocation): Long

    /* This is just for tests to ensure, there's only ever one home location per network */
    @Query("SELECT COUNT(uid) FROM work_locations WHERE networkId = :networkId")
    fun countWorks(networkId: NetworkId?): Int
}
