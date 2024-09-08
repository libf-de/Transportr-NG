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
import org.maplibre.android.geometry.LatLng

object LatLngSerializer : KSerializer<LatLng> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(LatLng::class.java.name) {
        element<Double>("latitude")
        element<Double>("longitude")
        element<Double>("altitude")
    }

    override fun deserialize(decoder: Decoder): LatLng {
        return decoder.decodeStructure(descriptor) {
            LatLng(
                decodeDoubleElement(descriptor, 0),
                decodeDoubleElement(descriptor, 1),
                decodeDoubleElement(descriptor, 2)
            )
        }
    }

    override fun serialize(encoder: Encoder, value: LatLng) {
        encoder.encodeStructure(descriptor) {
            encodeDoubleElement(descriptor, 0, value.latitude)
            encodeDoubleElement(descriptor, 1, value.longitude)
            encodeDoubleElement(descriptor, 2, value.altitude)
        }
    }
}