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

package de.libf.transportrng.data.gps

import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
import de.libf.transportrng.data.locations.WrapLocation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.headersOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OsmGeocoder : GeocodeProvider {

    @Serializable
    data class OsmAddress(
        @SerialName("house_number") val houseNumber: String,
        val road: String?,
        val suburb: String?,
        val city: String?,
        val postcode: String?,
        val country: String?,
        val state: String?
    )

    @Serializable
    data class OsmResponse(
        @SerialName("display_name") val displayName: String,
        val address: OsmAddress
    )


    override suspend fun findLocation(lat: Double, lon: Double): Result<WrapLocation> {
        try {
            val client = HttpClient(CIO) {
                headersOf("User-Agent", "Transportr (https://transportr.app)")
            }
            val json = Json { ignoreUnknownKeys = true }

            // https://nominatim.openstreetmap.org/reverse?lat=52.5217&lon=13.4324&format=json
            val url = StringBuilder("https://nominatim.openstreetmap.org/reverse?")
            url.append("lat=").append(lat).append("&")
            url.append("lon=").append(lon).append("&")
            url.append("format=json")

            val rsp = client.get(url.toString())

            if(rsp.status.value != 200) return Result.failure(Exception("Failed to query OSM for location: status ${rsp.status}"))

            rsp.body<OsmResponse>().let { osmData ->
                val name = osmData.address.road
                    .takeIf { !it.isNullOrEmpty() }
                    ?.let {
                        if(osmData.address.houseNumber.isNotEmpty())
                            "$it ${osmData.address.houseNumber}"
                        else
                            it
                    }
                    ?: osmData.displayName.split(",")[0]

                val place = osmData.address.city.takeIf { !it.isNullOrEmpty() } ?: (osmData.address.state ?: "")

                return Result.success(
                    WrapLocation(
                        Location(
                            null,
                            Location.Type.ADDRESS,
                            Point.fromDouble(lat, lon),
                            place,
                            name
                        )
                    )
                )
            }
//            return Result.failure(Exception("Failed to query OSM for location"))
        } catch(e: Exception) {
            return Result.failure(e)
        }
    }
}