package de.libf.transportrng.data

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME
import android.provider.CalendarContract.EXTRA_EVENT_END_TIME
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.StringRes
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.utils.toShareString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.error_no_calendar
import transportr_ng.composeapp.generated.resources.error_no_map
import transportr_ng.composeapp.generated.resources.show_location_in
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class AndroidPlatformTool(
    private val context: Context
) : PlatformTool {


    override fun showLocationOnMap(loc: WrapLocation) {
        CoroutineScope(Dispatchers.IO).launch {
            val uri1 = "geo:0,0?q=${loc.latLng.latitude},${loc.latLng.longitude}"
            val uri2 = try {
                "(" + URLEncoder.encode(loc._getName(), "utf-8") + ")"
            } catch (e: UnsupportedEncodingException) {
                "(" + loc._getName() + ")"
            }
            val geo = Uri.parse(uri1 + uri2)

            // show station on external map
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                data = geo
            }

            val intentChooser = Intent.createChooser(intent, getString(Res.string.show_location_in))
            intentChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            // exclude Transportr from list on Android >= 7
//                intentChooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ComponentName(context, MapActivity::class.java)))
            try {
                Log.d(context.javaClass.simpleName, "Starting geo intent: $geo")
                context.startActivity(intentChooser)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, getString(Res.string.error_no_map), Toast.LENGTH_LONG).show()
            }
        }


    }

    override fun shareText(text: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override suspend fun addToCalendar(trip: Trip): String? {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = "vnd.android.cursor.item/event"
            flags = FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_EVENT_BEGIN_TIME, trip.firstDepartureTime)
            putExtra(EXTRA_EVENT_END_TIME, trip.lastArrivalTime)
            putExtra(CalendarContract.Events.TITLE, trip.from.name + " → " + trip.to.name)
            putExtra(CalendarContract.Events.DESCRIPTION, trip.toShareString())
            if (trip.from.place != null) putExtra(CalendarContract.Events.EVENT_LOCATION, trip.from.place)
        }
        try {
            context.startActivity(intent)
            return null
        } catch (e: ActivityNotFoundException) {
            return getString(Res.string.error_no_calendar)
        }
    }
}