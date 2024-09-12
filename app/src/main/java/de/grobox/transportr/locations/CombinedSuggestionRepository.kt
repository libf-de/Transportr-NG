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

package de.grobox.transportr.locations

import de.grobox.transportr.data.locations.FavoriteLocation.Companion.FromComparator
import de.grobox.transportr.data.locations.FavoriteLocation.Companion.ToComparator
import de.grobox.transportr.data.locations.FavoriteLocation.Companion.ViaComparator
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.util.Locale

class CombinedSuggestionRepository(
    private val suggestLocationsRepository: SuggestLocationsRepository,
    private val locationRepository: LocationRepository
) {
    //    private val _combinedSuggestions = MediatorLiveData<Set<WrapLocation>>()
//    private val _sorting = MutableLiveData(FavLocationType.FROM)
    private val _sorting = MutableStateFlow(FavLocationType.FROM)


    private val _searchQuery = MutableStateFlow("")

    private val _favoriteLocations: Flow<Set<WrapLocation>> = combine(
        locationRepository.homeLocation,
        locationRepository.worLocation,
        locationRepository.favoriteLocations,
        _searchQuery
    ) { home, work, favorites, query ->
        setOfNotNull(
            setOfNotNull(home, work),

            favorites
                /*?.filter {
                it != locationRepository.homeLocation.value &&
                        it != locationRepository.worLocation.value
            }*/?.sortedWith(
                    when (_sorting.value) {
                        FavLocationType.TO -> ToComparator
                        FavLocationType.VIA -> ViaComparator
                        else -> FromComparator
                    }
                )
        ).flatten().filter {
            it.fullName.lowercase(Locale.getDefault()).contains(query)
        }.toSet()
    }

    val suggestions = combine(
        _favoriteLocations,
        suggestLocationsRepository.suggestedLocations
    ) { favorites, suggestions ->
        setOfNotNull(favorites, suggestions).flatten().toSet()
    }

    val isLoading: Flow<Boolean> = suggestLocationsRepository.isLoading

    fun setSorting(sorting: FavLocationType) {
        _sorting.value = sorting
    }

    fun updateSuggestions(query: String) {
        _searchQuery.value = query.lowercase(Locale.getDefault())
        suggestLocationsRepository.suggestLocations(query)
    }

    fun cancelSuggestions() = suggestLocationsRepository.cancelSuggestLocations()
    fun reset() = suggestLocationsRepository.reset()

}