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

import de.schildbach.pte.dto.Fare
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

@OptIn(ExperimentalSerializationApi::class)
object FareSerializer : KSerializer<Fare> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(Fare::class.java.name) {
        element<String>("name")
        element("type", FareTypeSerializer.descriptor)
        element("currency", CurrencySerializer.descriptor)
        element<Float>("fare")
        element<String?>("unitName")
        element<String?>("units")
    }

    override fun deserialize(decoder: Decoder): Fare {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Fare) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeSerializableElement(descriptor, 1, FareTypeSerializer, value.type)
            encodeSerializableElement(descriptor, 2, CurrencySerializer, value.currency)
            encodeFloatElement(descriptor, 3, value.fare)
            encodeNullableSerializableElement(descriptor, 4, String.serializer(), value.unitName)
            encodeNullableSerializableElement(descriptor, 5, String.serializer(), value.units)
        }
    }
}

object FareTypeSerializer : KSerializer<Fare.Type> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(Fare.Type::class.java.name, PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Fare.Type {
        return Fare.Type.entries[decoder.decodeInt()]
    }

    override fun serialize(encoder: Encoder, value: Fare.Type) {
        encoder.encodeInt(value.ordinal)
    }
}

val FareListSerializer = ListSerializer(FareSerializer)