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

import de.schildbach.pte.dto.Product
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ProductSerializer : KSerializer<Product> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(Product::class.java.name, PrimitiveKind.CHAR)

    override fun serialize(encoder: Encoder, value: Product) {
        encoder.encodeChar(value.code)
    }

    override fun deserialize(decoder: Decoder): Product {
        return Product.fromCode(decoder.decodeChar())
    }
}

val ProductSetSerializer = SetSerializer(ProductSerializer)