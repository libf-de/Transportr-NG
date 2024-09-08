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

package de.grobox.transportr.data.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.maplibre.android.geometry.LatLngBounds

object LatLngBoundsSerializer : KSerializer<LatLngBounds> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(LatLngBounds::class.java.name) {
        element<Double>("latitudeNorth")
        element<Double>("longitudeEast")
        element<Double>("latitudeSouth")
        element<Double>("longitudeWest")
    }

    override fun deserialize(decoder: Decoder): LatLngBounds {
        return decoder.decodeStructure(descriptor) {
            LatLngBounds.from(
                latNorth = decodeDoubleElement(descriptor, 0),
                lonEast = decodeDoubleElement(descriptor, 1),
                latSouth = decodeDoubleElement(descriptor, 2),
                lonWest = decodeDoubleElement(descriptor, 3)
            )
        }
    }

    override fun serialize(encoder: Encoder, value: LatLngBounds) {
        encoder.encodeStructure(descriptor) {
            encodeDoubleElement(descriptor, 0, value.latitudeNorth)
            encodeDoubleElement(descriptor, 1, value.longitudeEast)
            encodeDoubleElement(descriptor, 2, value.latitudeSouth)
            encodeDoubleElement(descriptor, 3, value.longitudeWest)
        }
    }
}