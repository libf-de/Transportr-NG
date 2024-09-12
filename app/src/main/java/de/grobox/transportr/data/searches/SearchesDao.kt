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
package de.grobox.transportr.data.searches

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.schildbach.pte.NetworkId
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SearchesDao {
    //	@Query("SELECT * FROM searches WHERE networkId = :networkId")
    //	LiveData<List<StoredSearch>> getStoredSearches(NetworkId networkId);
    @Query("SELECT * FROM searches WHERE networkId = :networkId")
    fun getStoredSearchesAsFlow(networkId: NetworkId?): Flow<List<StoredSearch>>

    @Query("SELECT * FROM searches WHERE networkId = :networkId AND from_id = :fromId AND via_id IS :viaId AND to_id = :toId")
    fun getStoredSearch(networkId: NetworkId, fromId: Long, viaId: Long?, toId: Long): StoredSearch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeSearch(storedSearch: StoredSearch): Long

    @Query("UPDATE searches SET count = count + 1, lastUsed = :lastUsed WHERE uid = :uid")
    fun updateStoredSearch(uid: Long, lastUsed: Date?)

    @Query("SELECT favorite FROM searches WHERE uid = :uid")
    fun isFavorite(uid: Long): Boolean

    @Query("UPDATE searches SET favorite = :favorite WHERE uid = :uid")
    fun setFavorite(uid: Long, favorite: Boolean)

    @Delete
    fun delete(storedSearch: StoredSearch)
}
