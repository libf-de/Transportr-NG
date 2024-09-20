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

package de.libf.transportrng.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.color.MaterialColors
import com.google.gson.JsonParser
import de.grobox.transportr.map.NearbyStationsDrawer
import de.libf.transportrng.R
import de.libf.transportrng.ui.map.drawable.createSpeechBubbleDrawable
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.Location
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Stop
import de.libf.ptek.dto.Trip
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Line
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import kotlin.math.abs


// Returns the Jawg url depending on the style given (jawg-streets by default)
// taken from https://www.jawg.io/docs/integration/maplibre-gl-android/simple-map/
private fun makeStyleUrl(style: String = "jawg-streets", context: Context) =
    "${context.getString(R.string.jawg_styles_url) + style}.json?access-token=${context.getString(R.string.jawg_access_token)}"

enum class MarkerType {
    BEGIN, CHANGE, STOP, END, WALK
}

actual fun provideMapState(): MapViewStateInterface = MapViewState()

@Composable
actual fun <T : MapViewStateInterface> MapViewComposable(
    mapViewState: T,
    compassMargins: CompassMargins,
    isHalfHeight: Boolean,
    mapPadding: MapPadding,
    rotateGestures: Boolean,
    showLogo: Boolean,
    showAttribution: Boolean,
    mapStyle: String
) {
    AndroidMapViewComposable(mapViewState as MapViewState, compassMargins, isHalfHeight, mapPadding, rotateGestures, showLogo, showAttribution, mapStyle)
}

@Composable
fun AndroidMapViewComposable(
    mapViewState: MapViewState,
    compassMargins: CompassMargins,
    isHalfHeight: Boolean,
    mapPadding: MapPadding,
    rotateGestures: Boolean,
    showLogo: Boolean,
    showAttribution: Boolean,
    mapStyle: String
) {
    val compassMarginsInt = compassMargins.toIntArray()

    var viewOnDestroy: () -> Unit = {}

    DisposableEffect(Unit) {
        onDispose {
            mapViewState.symbolManager?.onDestroy()
            viewOnDestroy()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(
                ContextThemeWrapper(context, R.style.MapStyle),
                MapLibreMapOptions()
                    .compassMargins(compassMarginsInt)
                    .rotateGesturesEnabled(rotateGestures)
                    .attributionEnabled(showAttribution)
                    .logoEnabled(showLogo)
            ).apply {
                viewOnDestroy = this::onDestroy

                val mvs = mapViewState.registerMapView(this, context)
                getMapAsync { map ->
                    val styleUrl = makeStyleUrl(mapStyle, context)
                    if (map.style?.uri != styleUrl) map.setStyle(styleUrl) { style ->
                        mvs.lineManager = LineManager(this, map, style)
                        mvs.symbolManager = SymbolManager(this, map, style)

                        mvs.symbolManager?.let {
                            it.iconAllowOverlap = true
                            it.textAllowOverlap = true
                            it.iconIgnorePlacement = true
                        }

                        mvs.onMapStyleLoaded(style)
                    }

                    map.addOnMapClickListener(mvs::maybeClearPopup)

                    if (!isHalfHeight) {
                        mvs.mapInset = mapPadding

                        map.moveCamera(
                            CameraUpdateFactory.paddingTo(
                                mapPadding.left.toDouble(),
                                mapPadding.top.toDouble(),
                                mapPadding.right.toDouble(),
                                mapPadding.bottom.toDouble()
                            )
                        )
                    } else {
                        mvs.mapInset = MapPadding(0, 0, 0, this.height / 2)
                        map.moveCamera(
                            CameraUpdateFactory.paddingTo(
                                0.0,
                                0.0,
                                0.0,
                                this.height / 2.0
                            )
                        )

                    }

                }
            }
        }
    )
}

private fun Location.toLatLng(): LatLng {
    return LatLng(this.latAsDouble, this.lonAsDouble)
}

private fun Stop.getText(context: Context): String {
    var text = ""
    this.getArrivalTime(false)?.let {
        text += "${context.getString(R.string.trip_arr)}: ${
            Instant.fromEpochMilliseconds(it).format(
                DateTimeComponents.Format {
                    hour()
                    chars(":")
                    minute()
                }
            )
        }"
    }
    this.getDepartureTime(false)?.let {
        if (text.isNotEmpty()) text += "\n"
        text += "${context.getString(R.string.trip_dep)}: ${
            Instant.fromEpochMilliseconds(it).format(
                DateTimeComponents.Format {
                    hour()
                    chars(":")
                    minute()
                }
            )
        }"
    }
    return text
}

private fun Symbol.near(other: LatLng): Boolean {
    val dLat = abs(this.latLng.latitude - other.latitude)
    val dLon = abs(this.latLng.longitude - other.longitude)

    val latOk = dLat < 0.001
    val lonOk = dLon < 0.001

    return latOk && lonOk
}

class MapViewState : MapViewStateInterface {
    protected var context: Context? = null
    protected var mapView: MapView? = null
    var mapPadding: Int = 0
    internal var mapInset: MapPadding = MapPadding()
    internal var onMapStyleLoaded: (style: Style) -> Unit = {}

    internal var symbolManager: SymbolManager? = null
    internal var lineManager: LineManager? = null

    private val lines: MutableList<Line> = mutableListOf()
    private val markers: MutableList<Symbol> = mutableListOf()

    private var onSymbolClickListener: ((Symbol) -> Boolean)? = null
    private var speechBubbleSymbol: Symbol? = null
    private var speechBubbleId: String? = null

    internal var nearbyStationsDrawer: NearbyStationsDrawer? = null
        private set

    internal fun maybeClearPopup(ignored: LatLng): Boolean {
        if(markers.any { it.near(ignored) }) return false

        speechBubbleSymbol?.let {
            symbolManager?.delete(it)
            speechBubbleSymbol = null

            mapView?.getMapAsync { map ->
                map.style?.removeImage(speechBubbleId ?: "")
                speechBubbleId = null
            }

            return true
        }

        return false
    }

    internal fun styleLoadedTrig(style: Style) {
        this.onMapStyleLoaded(style)

    }

    internal fun registerMapView(mapView: MapView, context: Context): MapViewState {
        this.mapView = mapView
        this.context = context
        return this
    }

    fun setOnMapStyleLoaded(onMapStyleLoaded: (style: Style) -> Unit) {
        this.onMapStyleLoaded = onMapStyleLoaded
    }

    override suspend fun animateTo(latLng: de.libf.transportrng.data.maplibrecompat.LatLng?, zoom: Int) {
        return animateTo(latLng?.toLatLng(), zoom)
    }

    fun animateTo(latLng: LatLng?, zoom: Int) {
        if (latLng == null) return
        mapView?.getMapAsync { map ->
            val padding = mapInset + mapPadding
            map.moveCamera(
                CameraUpdateFactory.paddingTo(
                    padding.left.toDouble(),
                    padding.top.toDouble(),
                    padding.right.toDouble(),
                    padding.bottom.toDouble()
                )
            )
            val update = if (map.cameraPosition.zoom < zoom) CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoom.toDouble()
            ) else CameraUpdateFactory.newLatLng(latLng)
            map.easeCamera(update, 1500)
        }
    }

    override suspend fun zoomToBounds(latLngBounds: de.libf.transportrng.data.maplibrecompat.LatLngBounds?, animate: Boolean) {
        return _zoomToBounds(latLngBounds?.toLatLngBounds(), animate)
    }

    private fun _zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
        if (latLngBounds == null) return
        val padding = mapInset + mapPadding
        val update = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding.left, padding.top, padding.right, padding.bottom)

        mapView?.getMapAsync { map ->
            if (animate) {
                map.easeCamera(update)
            } else {
                map.moveCamera(update)
            }
        }
    }


    override suspend fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        // store map padding to be retained even after CameraBoundsUpdates
        // and update directly for subsequent camera updates in MapDrawer
        mapInset = MapPadding(left, top, right, bottom)
        mapView?.getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.paddingTo(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble()))
        }
    }

    private fun getStationText(context: Context, leg: Leg, type: MarkerType): String {
        return when (type) {
            MarkerType.BEGIN -> leg.departureTime.let {
                "${context.getString(R.string.trip_dep)}: ${it.formatTime()}"
            }

            MarkerType.END -> leg.arrivalTime.let {
                "${context.getString(R.string.trip_arr)}: ${it.formatTime()}"
            }

            else -> ""
        }
    }

    private fun getStationText(context: Context, leg1: Leg, leg2: Leg): String {
        var text = ""
        leg1.arrivalTime.let {
            text += "${context.getString(R.string.trip_arr)}: ${it.formatTime()}"
        }
        leg2.departureTime.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${it.formatTime()}"
        }
        return text
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun drawTrip(trip: Trip?, shouldZoom: Boolean): Boolean {
        if (trip == null) return false

        withTimeout(5000L) {
            while (symbolManager == null) {
                delay(500L)
            }
            true
        }

        //symbolManager?.removeClickListener(this.onSymbolClickListener)
        //symbolManager?.deleteAll()
        lineManager?.deleteAll()

        val builder = LatLngBounds.Builder()
        var pointCount = 0

        context?.let { ctx ->
            mapView?.getMapAsync { map ->
//                TripDrawer(ctx).draw(map, trip, shouldZoom)

                if (this.onSymbolClickListener == null)
                    this.onSymbolClickListener = { symbol ->
                        if (symbol.iconImage == speechBubbleId) {
                            speechBubbleSymbol?.let {
                                symbolManager?.delete(it)
                                map.style?.removeImage(speechBubbleId ?: "")

                                speechBubbleId = null
                                speechBubbleSymbol = null
                            }
                        } else {
                            speechBubbleSymbol?.let { symbolManager?.delete(it) }
                            speechBubbleId?.let { map.style?.removeImage(it) }

                            symbol.data?.let {
                                val title = it.asJsonObject["title"].asString
                                val text = it.asJsonObject["text"].asString

                                val drawable = createSpeechBubbleDrawable(
                                    context = ctx,
                                    title = title,
                                    content = text,
                                    backgroundColor = Color.White.toArgb(),
                                    outlineColor = Color.Blue.toArgb(),
                                    textColor = Color.Black.toArgb()
                                ).toBitmapDrawable().toBitmap()
                                //.toBitmap(width = 400, height = 200)

                                val code = drawable.hashCode().toString()

                                speechBubbleId = code

                                map.style?.addImage(code, drawable)

                                val symbolOptions = SymbolOptions()
                                    .withLatLng(symbol.latLng)
                                    .withIconImage(speechBubbleId ?: "")
                                    .withIconSize(1.0f)
                                    .withSymbolSortKey(1000f)

                                speechBubbleSymbol = symbolManager!!.create(symbolOptions)
                            }
                        }

                        true
                    }

                symbolManager?.addClickListener(this.onSymbolClickListener!!)


                var i = 1
                trip.legs.forEachIndexed { j, leg ->
                    val path = leg.path

                    // get colors
                    val backgroundColor = leg.getBackgroundColor(ctx).let(::Color).takeIf { leg is PublicLeg } ?: Color(0xFFFED21B)
                    //val foregroundColor = leg.getForegroundColor(ctx).let(::Color)
                    val foregroundColor = Color.White

                    lineManager?.let { lineMgr ->
                        val points = ArrayList<LatLng>(path.size)

                        val colorHex = backgroundColor.toHexString()

                        path.mapTo(points) { LatLng(it.lat, it.lon) }
                        val lineOptions = LineOptions()
                            .withLineJoin("round")
                            .withLatLngs(points)
                            .withLineColor(colorHex)
                            .withLineWidth(5.0f)

                        lines.add(
                            lineMgr.create(lineOptions)
                        )

                        builder.includes(points)
                        pointCount += points.size
                    }

                    symbolManager?.let { symMgr ->
                        if (leg is PublicLeg) {
                            leg.intermediateStops?.forEach { stop ->
                                val stopIconId = stop.hashCode().toString()
                                val stopIcon = getMarkerIcon(ctx, MarkerType.STOP, backgroundColor, foregroundColor)

                                addMarker(
                                    map = map,
                                    pos = stop.location.toLatLng(),
                                    title = stop.location.uniqueShortName ?: "",
                                    text = stop.getText(ctx),
                                    icon = stopIcon
                                )
                            }

                            // Draw first station or change station
                            val startId = leg.departure.hashCode().toString()
                            val icon: Drawable
                            var text: String = ""
                            if (i == 1 || i == 2 && trip.legs[0] !is PublicLeg) {
                                icon = getMarkerIcon(ctx, MarkerType.BEGIN, backgroundColor, foregroundColor)
                                text = getStationText(ctx, leg, MarkerType.BEGIN)
                            } else {
                                icon = getMarkerIcon(ctx, MarkerType.CHANGE, backgroundColor, foregroundColor)
                                text = getStationText(ctx, trip.legs[i - 2], leg)
                            }

                            addMarker(
                                map = map,
                                pos = leg.departure.toLatLng(),
                                icon = icon,
                                text = text,
                                title = leg.departure.uniqueShortName ?: ""
                            )

                            // Draw final station only at the end or if end is walking
                            if (i == trip.legs.size || i == trip.legs.size - 1 && trip.legs[i] !is PublicLeg) {
                                val endId = leg.arrival.hashCode().toString()
                                val endIcon = getMarkerIcon(ctx, MarkerType.END, backgroundColor, foregroundColor)

                                addMarker(
                                    map = map,
                                    pos = leg.arrival.toLatLng(),
                                    icon = endIcon,
                                    text = getStationText(ctx, leg, MarkerType.END),
                                    title = leg.arrival.uniqueShortName ?: ""
                                )
                            }


                        } else if (i > 1 && i < trip.legs.size) {
                            // only draw an icon if walk is required in the middle of a trip
                            val id = leg.departure.hashCode().toString()
                            val icon = getMarkerIcon(ctx, MarkerType.WALK, backgroundColor, foregroundColor)

                            addMarker(
                                map = map,
                                pos = leg.departure.toLatLng(),
                                icon = icon,
                                text = getStationText(ctx, leg, MarkerType.END),
                                title = leg.departure.uniqueShortName ?: ""
                            )
                        }
                    }

                    i += 1
                }

                if (shouldZoom && pointCount > 1) {
                    _zoomToBounds(builder.build(), false)
                }
            }
        }


        return true
    }

    private fun getMarkerIcon(context: Context, type: MarkerType, backgroundColor: Color, foregroundColor: Color): Drawable {
        // Get Drawable
        val drawable: Drawable
        if (type == MarkerType.STOP) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_trip_stop) ?: throw RuntimeException()
            drawable.setTint(backgroundColor.toArgb())
            //return DrawableCompat.wrap(drawable)
            //drawable.mutate().setColorFilter(backgroundColor.toArgb(), SRC_IN)
        } else {
            val res: Int = when (type) {
                MarkerType.BEGIN -> R.drawable.ic_marker_trip_begin
                MarkerType.CHANGE -> R.drawable.ic_marker_trip_change
                MarkerType.END -> R.drawable.ic_marker_trip_end
                MarkerType.WALK -> R.drawable.ic_marker_trip_walk
                else -> throw IllegalArgumentException()
            }
            drawable = ContextCompat.getDrawable(context, res) as LayerDrawable
            drawable.getDrawable(0).setTint(backgroundColor.toArgb())
            drawable.getDrawable(1).setTint(foregroundColor.toArgb())

            //drawable.getDrawable(0).mutate().setColorFilter(backgroundColor.toArgb(), MULTIPLY)
            //drawable.getDrawable(1).mutate().setColorFilter(foregroundColor.toArgb(), SRC_IN)
        }
        return drawable
    }

    //private fun marLocation(map: MapLibreMap, location: Location, icon: Icon, text: String) {
    private fun addMarker(map: MapLibreMap, pos: LatLng, title: String, text: String, icon: Drawable): Symbol? {
        symbolManager?.let {
            val id = (pos.hashCode() + text.hashCode()).toString()
            val jsonData = """
                {
                    "title": "$title",
                    "text": "$text"
                }
            """
            map.style?.addImage(id, icon)

            val symbolOptions = SymbolOptions()
                .withData(JsonParser.parseString(jsonData))
                .withLatLng(pos)
                .withIconImage(id)
                .withIconSize(1.0f)
            //.withTextOffset(arrayOf(0f, if(icon.intrinsicHeight > 100) 1.4f else 0f))  // Adjust the second value to move text down
            //.withTextAnchor("top")
            //.withTextField(title)

            return it.create(symbolOptions).also { markers.add(it) }
        }

        return null
    }
}

private fun Long.formatTime(): String {
    return Instant.fromEpochMilliseconds(this).format(
        DateTimeComponents.Format {
            hour()
            chars(":")
            minute()
        }
    )
}

private fun de.libf.transportrng.data.maplibrecompat.LatLngBounds.toLatLngBounds(): LatLngBounds {
    return LatLngBounds.Companion.from(
        latNorth = this.latitudeNorth,
        latSouth = this.latitudeSouth,
        lonEast = this.longitudeEast,
        lonWest = this.longitudeWest
    )
}

private fun de.libf.transportrng.data.maplibrecompat.LatLng.toLatLng(): LatLng {
    return if(this.altitude != null)
        LatLng(this.latitude, this.longitude, this.altitude)
    else
        LatLng(this.latitude, this.longitude)
}

@ColorInt
fun Leg.getBackgroundColor(context: Context): Int {
    if (this is PublicLeg) {
        return line.style?.backgroundColor?.takeIf { it != 0} ?: MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, android.graphics.Color.TRANSPARENT)
    }
    return MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, android.graphics.Color.TRANSPARENT)
}

@ColorInt
fun Leg.getForegroundColor(context: Context): Int {
    if (this is PublicLeg) {
        return line.style?.foregroundColor?.takeIf { it != 0 } ?: ContextCompat.getColor(context, android.R.color.white)
    }
    return ContextCompat.getColor(context, android.R.color.black)
}

fun Color.toHexString(): String {
    val red = (this.red * 255).toInt().toString(16).padStart(2, '0')
    val green = (this.green * 255).toInt().toString(16).padStart(2, '0')
    val blue = (this.blue * 255).toInt().toString(16).padStart(2, '0')
    return "#$red$green$blue"
}

fun _createSpeechBubbleDrawable(context: Context, title: String, content: String, backgroundColor: Int, textColor: Int): Drawable {
    return object : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val contentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()

        init {
            paint.color = backgroundColor
            paint.style = Paint.Style.FILL

            titlePaint.color = textColor
            titlePaint.textSize = 48f
            titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            titlePaint.textAlign = Paint.Align.CENTER

            contentPaint.color = textColor
            contentPaint.textSize = 36f
            contentPaint.textAlign = Paint.Align.CENTER
        }

        override fun draw(canvas: Canvas) {
            val bounds = bounds
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()

            // Draw the main bubble (same as before)
            path.reset()
            path.moveTo(0f, 20f)
            path.lineTo(0f, height - 40f)
            path.quadTo(0f, height - 20f, 20f, height - 20f)
            path.lineTo(width - 40f, height - 20f)
            path.quadTo(width - 20f, height - 20f, width - 20f, height - 40f)
            path.lineTo(width - 20f, 20f)
            path.quadTo(width - 20f, 0f, width - 40f, 0f)
            path.lineTo(20f, 0f)
            path.quadTo(0f, 0f, 0f, 20f)

            // Draw the tail
            path.moveTo(width - 40f, height - 20f)
            path.lineTo(width, height)
            path.lineTo(width - 60f, height - 20f)

            canvas.drawPath(path, paint)

            // Draw the title
            val xPos = width / 2
            val titleYPos = height * 0.3f - ((titlePaint.descent() + titlePaint.ascent()) / 2)
            canvas.drawText(title, xPos, titleYPos, titlePaint)

            // Draw the content
            val contentYPos = height * 0.6f - ((contentPaint.descent() + contentPaint.ascent()) / 2)
            canvas.drawText(content, xPos, contentYPos, contentPaint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
            titlePaint.alpha = alpha
            contentPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
            titlePaint.colorFilter = colorFilter
            contentPaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}

