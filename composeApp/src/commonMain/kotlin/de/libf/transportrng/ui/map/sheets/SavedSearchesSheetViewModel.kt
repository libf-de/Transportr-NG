package de.libf.transportrng.ui.map.sheets

import androidx.lifecycle.viewModelScope
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.networks.TransportNetworkManager
import de.libf.transportrng.data.favorites.FavoriteTripItem
import de.libf.transportrng.data.gps.GpsRepository
import de.libf.transportrng.data.locations.LocationRepository
import de.libf.transportrng.data.searches.SearchesRepository
import de.libf.transportrngocations.CombinedSuggestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SavedSearchesSheetViewModel internal constructor(
    transportNetworkManager: TransportNetworkManager,
    locationRepository: LocationRepository,
    searchesRepository: SearchesRepository,
) : SavedSearchesViewModel(transportNetworkManager, locationRepository, searchesRepository) {
    private val _uiState: MutableStateFlow<SavedSearchesSheetState> = MutableStateFlow(SavedSearchesSheetState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        combine(favoriteTrips, specialLocations) { favorites, specials ->
            SavedSearchesSheetState.Success(
                favorites = favorites,
                specials = specials
            )
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}


sealed class SavedSearchesSheetState {
    data object Loading : SavedSearchesSheetState()
    data class Success(
        val specials: List<FavoriteTripItem>,
        val favorites: List<FavoriteTripItem>
    ) : SavedSearchesSheetState()
}