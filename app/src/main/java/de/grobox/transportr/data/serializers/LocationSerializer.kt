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

import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
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
object LocationSerializer : KSerializer<Location> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(Location::class.java.name) {
        element("type", LocationTypeSerializer.descriptor)
        element<String?>("id")
        element("coord", PointSerializer.descriptor)
        element<String?>("place")
        element<String?>("name")
        element("products", ProductSetSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, LocationTypeSerializer, value.type)
            encodeNullableSerializableElement(descriptor, 1, String.serializer(), value.id)
            encodeNullableSerializableElement(descriptor, 2, PointSerializer, value.coord)
            encodeNullableSerializableElement(descriptor, 3, String.serializer(), value.place)
            encodeNullableSerializableElement(descriptor, 4, String.serializer(), value.name)
            encodeNullableSerializableElement(descriptor, 5, ProductSetSerializer, value.products)
        }
    }

    override fun deserialize(decoder: Decoder): Location {
        TODO("Not yet implemented")
    }
}

object LocationTypeSerializer : KSerializer<LocationType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(LocationType::class.java.name, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: LocationType) {
        encoder.encodeInt(value.ordinal)
    }

    override fun deserialize(decoder: Decoder): LocationType {
        return LocationType.entries[decoder.decodeInt()]
    }
}