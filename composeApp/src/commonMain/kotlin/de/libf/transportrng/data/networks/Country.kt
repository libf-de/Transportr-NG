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

package de.grobox.transportr.networks

import de.libf.transportrng.data.networks.TransportNetwork
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class Country constructor(
    val name: StringResource,
    val flag: String,
    val sticky: Boolean = false,
    val networks: List<TransportNetwork>
) : Region {

    override suspend fun getName(): String {
        return getString(name)
    }

    companion object {
        suspend fun List<Country>.sorted(): List<Country> {
            return this.map {
                it to it.getName()
            }.sortedWith(compareBy(
                { it.first.sticky },
                { it.second }
            )).map {
                it.first
            }
        }
    }

//    class Comparator constructor(private val context: Context) : java.util.Comparator<Country> {
//        override fun compare(c1: Country, c2: Country): Int {
//            if (c1.sticky && !c2.sticky) return -1
//            if (!c1.sticky && c2.sticky) return 1
//            return c1.getName(context).compareTo(c2.getName(context))
//        }
//    }

}