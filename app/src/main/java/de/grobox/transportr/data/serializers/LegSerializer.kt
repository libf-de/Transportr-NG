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

import de.schildbach.pte.dto.Trip.Leg
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

object LegSerializer : KSerializer<Leg> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(Leg::class.java.name) {
        element("departure", LocationSerializer.descriptor)
        element("arrival", LocationSerializer.descriptor)
        element("path", PointListSerializer.descriptor)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Leg) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, LocationSerializer, value.departure)
            encodeSerializableElement(descriptor, 1, LocationSerializer, value.arrival)
            encodeNullableSerializableElement(descriptor, 2, PointListSerializer, value.path)
        }
    }

    override fun deserialize(decoder: Decoder): Leg {
        TODO("Not yet implemented")
    }
}

val LegListSerializer = ListSerializer(LegSerializer)