package de.libf.transportrng.ui.departures.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Departure
import de.libf.transportrng.data.utils.getName
import de.libf.transportrng.ui.transport.composables.ProductComposable
import de.libf.transportrng.ui.transport.composables.getDrawableRes
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.in_x_minutes
import transportr_ng.composeapp.generated.resources.now
import transportr_ng.composeapp.generated.resources.product_bus
import transportr_ng.composeapp.generated.resources.x_minutes_ago
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Composable
fun RelativeTimeDisplay(
    instant: Instant?,
    text: @Composable (String) -> Unit
) {
    if(instant != null) {
        var currentInstant by remember { mutableStateOf(Clock.System.now().toUTC()) }

        val duration = instant - currentInstant
        val minutes = duration.inWholeMinutes.toInt()

        val displayText = when {
            minutes > 0 -> stringResource(Res.string.in_x_minutes, minutes)
            minutes < 0 -> stringResource(Res.string.x_minutes_ago, -minutes)
            else -> stringResource(Res.string.now)
        }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1.minutes)
                currentInstant = Clock.System.now().toUTC()
            }
        }

        text(displayText)
    } else {
        text("")
    }
}

private fun Instant.toUTC(): Instant {
    return this.toLocalDateTime(TimeZone.currentSystemDefault()).toInstant(TimeZone.UTC)
}

@Composable
fun DepartureComposable(
    departure: Departure,
    modifier: Modifier = Modifier
) {
    println(departure.time)

    val departureInstant = departure.time?.let(Instant::fromEpochMilliseconds)

    val departureTime = departureInstant?.format(DateTimeComponents.Format {
        hour()
        chars(":")
        minute()
    })

    val delay = departure.plannedTime?.let { plan ->
        departure.predictedTime?.let { pred ->
            val absolute = (pred - plan).milliseconds.inWholeMinutes
            val sign = if(pred >= plan) 1 else -1
            absolute * sign
        }
    } ?: 0

    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row {
                Text(text = departureTime ?: "")
                Text(text = delay.toString())

                Spacer(Modifier.weight(1f))

                RelativeTimeDisplay(departureInstant) {
                    Text(text = it)
                }
            }

            Row {
                ProductComposable(
                    line = departure.line
                )
                Text(text = departure.destination.getName() ?: "")
            }
        }
    }
}