package de.libf.transportrng.data.utils

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs

fun millisToMinutes(millis: Long): Long {
    val seconds = millis / 1000
    return seconds / 60 + when {
        seconds % 60 >= 30 -> 1
        seconds % 60 <= -30 -> -1
        else -> 0
    }
}

fun formatDuration(duration: Long?): String? {
    if(duration == null) return null
    // get duration in minutes
    val durationMinutes = millisToMinutes(duration)
    val m = durationMinutes % 60
    val h = durationMinutes / 60
    return "$h:${m.toString().padStart(2, '0')}"
}

fun formatDelay(delay: Long): Delay {
    val delayMinutes = millisToMinutes(delay)
    return Delay(
        delay = "${if (delayMinutes >= 0) '+' else ""}$delayMinutes",
        color = if (delayMinutes > 0) Color.Red else Color.Green
    )
}

data class Delay(
    val delay: String,
    val color: Color
)

//fun formatRelativeTime(context: Context, date: Date, max: Int = 99): RelativeTime {
//    val difference = getDifferenceInMinutes(date) ?: 0L
//    return RelativeTime(
//        relativeTime = when {
//            difference !in -max..max -> ""
//            difference == 0L -> context.getString(R.string.now_small)
//            difference > 0 -> context.getString(R.string.in_x_minutes, difference)
//            else -> context.getString(R.string.x_minutes_ago, difference * -1)
//        },
//        visibility = if (difference in -max..max) View.VISIBLE else View.GONE
//    )
//}

data class RelativeTime(
    val relativeTime: String,
    val visibility: Int
)

fun Instant.isNow(): Boolean {
    return abs(Clock.System.now().minus(this).inWholeMinutes) <= 1
}

fun Instant.isToday(): Boolean {
    return abs(Clock.System.now().minus(this).inWholeDays) == 0L
}