package de.libf.transportrng.data.maplibrecompat

import androidx.annotation.FloatRange
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class LatLngBounds(
    val latitudeNorth: Double,
    val latitudeSouth: Double,
    val longitudeEast: Double,
    val longitudeWest: Double
) {
    val center: LatLng
        get() {
            val latCenter = (latitudeNorth + latitudeSouth) / 2.0
            val longCenter = (longitudeEast + longitudeWest) / 2.0
            return LatLng(latCenter, longCenter)
        }

    class InvalidLatLngBoundsException(latLngsListSize: Int) :
        RuntimeException("Cannot create a LatLngBounds from $latLngsListSize items")

    class Builder {
        private val latLngList: MutableList<LatLng> = ArrayList()

        /**
         * Builds a new LatLngBounds.
         *
         *
         * Throws an [InvalidLatLngBoundsException] when no LatLngBounds can be created.
         *
         *
         * @return the build LatLngBounds
         */
        fun build(): LatLngBounds {
            if (latLngList.size < 2) {
                throw InvalidLatLngBoundsException(latLngList.size)
            }
            return fromLatLngs(latLngList)
        }

        /**
         * Adds a LatLng object to the LatLngBounds.Builder.
         *
         * @param latLngs the List of LatLng objects to be added
         * @return this
         */
        fun includes(latLngs: List<LatLng>): Builder {
            latLngList.addAll(latLngs)
            return this
        }

        /**
         * Adds a LatLng object to the LatLngBounds.Builder.
         *
         * @param latLng the LatLng to be added
         * @return this
         */
        fun include(latLng: LatLng): Builder {
            latLngList.add(latLng)
            return this
        }
    }

    companion object {

        /**
         * Constructs a LatLngBounds that contains all of a list of LatLng
         * objects. Empty lists will yield invalid LatLngBounds.
         *
         * @param latLngs List of LatLng objects
         * @return LatLngBounds
         */
        fun fromLatLngs(latLngs: List<LatLng>): LatLngBounds {
            var minLat = 90.0
            var minLon = Double.MAX_VALUE
            var maxLat = -90.0
            var maxLon = -Double.MAX_VALUE
            for (gp in latLngs) {
                val latitude = gp.latitude
                val longitude = gp.longitude
                minLat = min(minLat, latitude)
                minLon = min(minLon, longitude)
                maxLat = max(maxLat, latitude)
                maxLon = max(maxLon, longitude)
            }
            return LatLngBounds(maxLat, maxLon, minLat, minLon)
        }

        /**
         * Constructs a LatLngBounds from doubles representing a LatLng pair.
         *
         *
         * This values of latNorth and latSouth should be in the range of [-90, 90],
         * see [GeometryConstants.MIN_LATITUDE] and [GeometryConstants.MAX_LATITUDE],
         * otherwise IllegalArgumentException will be thrown.
         * latNorth should be greater or equal latSouth, otherwise  IllegalArgumentException will be thrown.
         *
         *
         * This method doesn't recalculate most east or most west boundaries.
         * Note @since 7.0.0  lonEast and lonWest will NOT be wrapped to be in the range of [-180, 180],
         * see [GeometryConstants.MIN_LONGITUDE] and [GeometryConstants.MAX_LONGITUDE]
         * lonEast should be greater or equal lonWest, otherwise  IllegalArgumentException will be thrown.
         *
         */
        fun from(
            @FloatRange(from = -90.0, to = 90.0) latNorth: Double,
            lonEast: Double,
            @FloatRange(from = -90.0, to = 90.0) latSouth: Double,
            lonWest: Double
        ): LatLngBounds {
            checkParams(latNorth, lonEast, latSouth, lonWest)
            return LatLngBounds(latNorth, lonEast, latSouth, lonWest)
        }

        private fun checkParams(
            @FloatRange(from = -90.0, to = 90.0) latNorth: Double,
            lonEast: Double,
            @FloatRange(from = -90.0, to = 90.0) latSouth: Double,
            lonWest: Double
        ) {
            require(!(latNorth.isNaN() || latSouth.isNaN())) { "latitude must not be NaN" }
            require(!(lonEast.isNaN() || lonWest.isNaN())) { "longitude must not be NaN" }
            require(!(lonEast.isInfinite() || lonWest.isInfinite())) { "longitude must not be infinite" }
            require(
                !(latNorth > 90.0 || latNorth < -90.0 || latSouth > 90.0 || latSouth < -90.0)
            ) { "latitude must be between -90 and 90" }
            require(latNorth >= latSouth) { "latNorth cannot be less than latSouth" }
            require(lonEast >= lonWest) { "lonEast cannot be less than lonWest" }
        }

        private fun lat_(z: Int, y: Int): Double {
            val n = PI - 2.0 * PI * y /2.0.pow(z.toDouble())
            return atan(0.5 * (exp(n) - exp(-n))).toDegrees()
        }

        private fun Double.toDegrees(): Double {
            return this * (180.0 / PI)
        }


        private fun lon_(z: Int, x: Int): Double {
            return x / 2.0.pow(z.toDouble()) * 360.0 - 180.0
        }

        /**
         * Constructs a LatLngBounds from a Tile identifier.
         *
         *
         * Returned bounds will have latitude in the range of Mercator projection.
         *
         * @param z Tile zoom level.
         * @param x Tile X coordinate.
         * @param y Tile Y coordinate.
         * @see GeometryConstants.MIN_MERCATOR_LATITUDE
         *
         * @see GeometryConstants.MAX_MERCATOR_LATITUDE
         */
        fun from(z: Int, x: Int, y: Int): LatLngBounds {
            return LatLngBounds(lat_(z, y), lon_(z, x + 1), lat_(z, y + 1), lon_(z, x))
        }
    }
}