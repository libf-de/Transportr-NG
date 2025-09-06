package de.libf.transportrng.data.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.friday_long
import transportr_ng.composeapp.generated.resources.friday_short
import transportr_ng.composeapp.generated.resources.monday_long
import transportr_ng.composeapp.generated.resources.monday_short
import transportr_ng.composeapp.generated.resources.saturday_long
import transportr_ng.composeapp.generated.resources.saturday_short
import transportr_ng.composeapp.generated.resources.sunday_long
import transportr_ng.composeapp.generated.resources.sunday_short
import transportr_ng.composeapp.generated.resources.thursday_long
import transportr_ng.composeapp.generated.resources.thursday_short
import transportr_ng.composeapp.generated.resources.today
import transportr_ng.composeapp.generated.resources.tomorrow
import transportr_ng.composeapp.generated.resources.tuesday_long
import transportr_ng.composeapp.generated.resources.tuesday_short
import transportr_ng.composeapp.generated.resources.wednesday_long
import transportr_ng.composeapp.generated.resources.wednesday_short
import transportr_ng.composeapp.generated.resources.yesterday
import kotlin.math.abs

fun millisToMinutes(millis: Long): Long {
    val seconds = millis / 1000
    return seconds / 60 + when {
        seconds % 60 >= 30 -> 1
        seconds % 60 <= -30 -> -1
        else -> 0
    }
}

fun Long?.formatDuration(): String? {
    if(this == null) return null
    // get duration in minutes
    val durationMinutes = millisToMinutes(this)
    val m = durationMinutes % 60
    val h = durationMinutes / 60
    return "$h:${m.toString().padStart(2, '0')}"
}

suspend fun LocalDate.getDateLabel(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dowNames = getDayOfWeekNamesLong()
    if(this == today) return getString(Res.string.today)
    else if(this == today.minus(1, DateTimeUnit.DAY)) return getString(Res.string.yesterday)
    else if(this == today.plus(1, DateTimeUnit.DAY)) return getString(Res.string.tomorrow)
    else return this.format(LocalDate.Format {
        dayOfWeek(dowNames)
        chars(", ")
        dayOfMonth()
        chars(".")
        monthNumber()
        chars(".")
        yearTwoDigits(2000)
    })
}

private suspend fun getDayOfWeekNamesLong(): DayOfWeekNames {
    return DayOfWeekNames(
        monday = getString(Res.string.monday_long),
        tuesday = getString(Res.string.tuesday_long),
        wednesday = getString(Res.string.wednesday_long),
        thursday = getString(Res.string.thursday_long),
        friday = getString(Res.string.friday_long),
        saturday = getString(Res.string.saturday_long),
        sunday = getString(Res.string.sunday_long)
    )
}

fun Instant.toUTC(): Instant = this.toLocalDateTime(TimeZone.currentSystemDefault()).toInstant(TimeZone.UTC)

@Composable
fun getDayOfWeekNamesShortComposable(): DayOfWeekNames {
    return DayOfWeekNames(
        monday = stringResource(Res.string.monday_short),
        tuesday = stringResource(Res.string.tuesday_short),
        wednesday = stringResource(Res.string.wednesday_short),
        thursday = stringResource(Res.string.thursday_short),
        friday = stringResource(Res.string.friday_short),
        saturday = stringResource(Res.string.saturday_short),
        sunday = stringResource(Res.string.sunday_short)
    )
}

private suspend fun getDayOfWeekNamesShort(): DayOfWeekNames {
    return DayOfWeekNames(
        monday = getString(Res.string.monday_short),
        tuesday = getString(Res.string.tuesday_short),
        wednesday = getString(Res.string.wednesday_short),
        thursday = getString(Res.string.thursday_short),
        friday = getString(Res.string.friday_short),
        saturday = getString(Res.string.saturday_short),
        sunday = getString(Res.string.sunday_short)
    )
}

//@Deprecated("Use extension function Long?.formatDuration() instead")
//fun formatDuration(duration: Long?): String? {
//    if(duration == null) return null
//    // get duration in minutes
//    val durationMinutes = millisToMinutes(duration)
//    val m = durationMinutes % 60
//    val h = durationMinutes / 60
//    return "$h:${m.toString().padStart(2, '0')}"
//}

fun Long.formatTime(): String {
    return Instant.fromEpochMilliseconds(this).format(
        DateTimeComponents.Format {
            hour()
            chars(":")
            minute()
        }
    )
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