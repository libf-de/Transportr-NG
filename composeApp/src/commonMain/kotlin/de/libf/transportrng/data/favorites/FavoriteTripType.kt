package de.libf.transportrng.data.favorites

import kotlinx.serialization.Serializable

@Serializable
enum class FavoriteTripType {
    HOME,
    WORK,
    TRIP
}