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

import de.schildbach.pte.dto.Trip
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
object TripSerializer : KSerializer<Trip> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor(Trip::class.java.name, SerialKind.CONTEXTUAL) {
        element<String>("id")
        element("from", LocationSerializer.descriptor)
        element("to", LocationSerializer.descriptor)
        element("legs", LegListSerializer.descriptor)
        element("fares", FareListSerializer.descriptor)
        element<IntArray>("capacity")
        element<Int>("numChanges")
    }

    override fun serialize(encoder: Encoder, value: Trip) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id)
            encodeSerializableElement(descriptor, 1, LocationSerializer, value.from)
            encodeSerializableElement(descriptor, 2, LocationSerializer, value.to)
            encodeSerializableElement(descriptor, 3, LegListSerializer, value.legs)
            encodeNullableSerializableElement(descriptor, 4, FareListSerializer, value.fares)
            encodeSerializableElement(descriptor, 5, ListSerializer(Int.serializer()), value.capacity.toList())
            encodeIntElement(descriptor, 6, value.numChanges)
        }
    }

    override fun deserialize(decoder: Decoder): Trip {
        return decoder.decodeStructure(descriptor) {
            Trip(
                decodeStringElement(descriptor, 0),
                decodeSerializableElement(descriptor, 1, LocationSerializer),
                decodeSerializableElement(descriptor, 2, LocationSerializer),
                decodeSerializableElement(descriptor, 3, LegListSerializer),
                decodeNullableSerializableElement(descriptor, 4, FareListSerializer),
                decodeSerializableElement(descriptor, 5, ListSerializer(Int.serializer())).toIntArray(),
                decodeIntElement(descriptor, 6)
            )
        }
    }
}