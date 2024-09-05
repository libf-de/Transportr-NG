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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.data.locations.FavoriteLocation.Companion.FromComparator
import de.grobox.transportr.data.locations.FavoriteLocation.Companion.ToComparator
import de.grobox.transportr.data.locations.FavoriteLocation.Companion.ViaComparator
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.LocationRepository
import java.util.Locale

class CombinedSuggestionRepository(
    private val suggestLocationsRepository: SuggestLocationsRepository,
    private val locationRepository: LocationRepository
) {

    private val _combinedSuggestions = MediatorLiveData<Set<WrapLocation>>()
    private val _sorting = MutableLiveData(FavLocationType.FROM)
    private val _favoriteLocations = MediatorLiveData<Set<WrapLocation>>()
    private val _searchQuery = MutableLiveData("")

    val suggestions: LiveData<Set<WrapLocation>> = _combinedSuggestions
    val isLoading: LiveData<Boolean> = suggestLocationsRepository.isLoading

    init {
        _favoriteLocations.addSource(locationRepository.homeLocation, ::updateFavorites)
        _favoriteLocations.addSource(locationRepository.workLocation, ::updateFavorites)
        _favoriteLocations.addSource(locationRepository.favoriteLocations, ::updateFavorites)
        _favoriteLocations.addSource(_searchQuery, ::updateFavorites)

        _combinedSuggestions.addSource(_favoriteLocations, ::updateCombinedSuggestions)
        _combinedSuggestions.addSource(suggestLocationsRepository.suggestedLocations, ::updateCombinedSuggestions)
    }

    private fun updateFavorites(ignored: Any?) {
        _favoriteLocations.postValue(
            setOfNotNull(
                setOfNotNull(locationRepository.homeLocation.value,
                    locationRepository.workLocation.value),

                locationRepository.favoriteLocations.value
                    /*?.filter {
                    it != locationRepository.homeLocation.value &&
                            it != locationRepository.workLocation.value
                }*/?.sortedWith(
                    when (_sorting.value) {
                        FavLocationType.TO -> ToComparator
                        FavLocationType.VIA -> ViaComparator
                        else -> FromComparator
                    }
                )
            ).flatten().filter {
                _searchQuery.value?.let { sq ->
                    it.fullName.lowercase(Locale.getDefault()).contains(sq)
                } ?: true
            }.toSet()
        )
    }

    private fun updateCombinedSuggestions(ignored: Any?) {
        _combinedSuggestions.postValue(
            setOfNotNull(
                _favoriteLocations.value,
                suggestLocationsRepository.suggestedLocations.value
            ).flatten().toSet()
        )
    }

    fun setSorting(sorting: FavLocationType) = _sorting.postValue(sorting)
    fun updateSuggestions(query: String) {
        _searchQuery.postValue(query.lowercase(Locale.getDefault()))
        suggestLocationsRepository.suggestLocations(query)
    }
    fun cancelSuggestions() = suggestLocationsRepository.cancelSuggestLocations()
    fun reset() = suggestLocationsRepository.reset()

}