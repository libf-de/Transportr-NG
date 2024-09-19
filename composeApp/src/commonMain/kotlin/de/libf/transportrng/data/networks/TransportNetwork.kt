/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.libf.transportrng.data.networks

import de.grobox.transportr.networks.Region
import de.libf.ptek.NetworkId
import de.libf.ptek.NetworkProvider
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.network_placeholder

data class TransportNetwork internal constructor(
    val id: NetworkId,
    private val name: StringResource? = null,
    private val description: StringResource? = null,
    private val agencies: StringResource? = null,
    val status: Status = Status.STABLE,
    val logo: DrawableResource = Res.drawable.network_placeholder,
    private val goodLineNames: Boolean = false,
    private val itemIdExtra: Int = 0,
    private val factory: () -> NetworkProvider
) : Region {

    enum class Status {
        ALPHA, BETA, STABLE
    }

    val networkProvider: NetworkProvider by lazy { factory.invoke() }

//    val networkProvider: NetworkProvider by lazy { networkProviderRef.get() ?: getNetworkProviderReference().get()!! }
//    private val networkProviderRef by lazy { getNetworkProviderReference() }
//    private fun getNetworkProviderReference() = WeakReference(factory.invoke())

    init {
        require(description != null || agencies != null)
    }

    override suspend fun getName(): String {
        return if (name == null) {
            id.name
        } else {
            getString(name)
        }
    }

    fun getNameRes(): Pair<StringResource?, String> {
        return Pair(name, id.name)
    }

    fun getDescriptionRes(): List<StringResource> {
        return if (description != null && agencies != null) {
            listOf(description, agencies)
        } else if (description != null) {
            listOf(description)
        } else if (agencies != null) {
            listOf(agencies)
        } else {
            throw IllegalArgumentException()
        }
    }

    suspend fun getDescription(): String {
        return if (description != null && agencies != null) {
            getString(description) + " (" + getString(agencies) + ")"
        } else if (description != null) {
            getString(description)
        } else if (agencies != null) {
            getString(agencies)
        } else {
            throw IllegalArgumentException()
        }
    }

    fun hasGoodLineNames(): Boolean {
        return goodLineNames
    }

}
