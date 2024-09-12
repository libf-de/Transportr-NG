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

package de.grobox.transportr.data.gps

import de.libf.ptek.dto.Location
import de.libf.ptek.dto.Point
import de.grobox.transportr.locations.WrapLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume

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
            val client = OkHttpClient()
            val json = Json { ignoreUnknownKeys = true }

            // https://nominatim.openstreetmap.org/reverse?lat=52.5217&lon=13.4324&format=json
            val url = StringBuilder("https://nominatim.openstreetmap.org/reverse?")
            url.append("lat=").append(lat).append("&")
            url.append("lon=").append(lon).append("&")
            url.append("format=json")

            val request = Request.Builder()
                .header("User-Agent", "Transportr (https://transportr.app)")
                .url(url.toString())
                .build()

            val rsp = client.executeAsync(request)

            if(rsp.isFailure) return Result.failure(rsp.exceptionOrNull() ?: Exception("Failed to query OSM for location"))

            rsp.onSuccess { response ->
                if (!response.isSuccessful) return Result.failure(Exception("Failed to query OSM for location"))

                response.body?.let { body ->
                    val osmData = json.decodeFromString<OsmResponse>(body.string())

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
                } ?: return Result.failure(Exception("Empty response body from OSM"))
            }
            return Result.failure(Exception("Failed to query OSM for location"))
        } catch(e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun OkHttpClient.executeAsync(request: Request): Result<Response> {
        return suspendCancellableCoroutine { continuation ->
            val call = newCall(request)
            call.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(Result.success(response))
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.failure(e))
                }
            })

            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }

}