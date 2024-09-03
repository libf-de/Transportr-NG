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
package de.grobox.transportr

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.maplibre.android.MapLibre

open class TransportrApplication : Application() {
    //lateinit var component: AppComponent
    //    private set

    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)

        super.onCreate()

        startKoin {
            // declare used Android context
            androidContext(this@TransportrApplication)

            // declare modules
            modules(
                TransportrModule,
                DatabaseModule,
                ViewModelModule
            )
        }

        MapLibre.getInstance(applicationContext)
    }
}
