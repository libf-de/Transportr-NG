package de.libf.transportrng.ui.datetimepicker

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.until

@Composable
private fun getWeekday(firstDayWeekday: Int, selectedDayOfMonth: Int): String {
    val daysMed = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    return daysMed[firstDayWeekday.plus(selectedDayOfMonth).minus(1).mod(7)]
}

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    preselected: Triple<Int, Int, Int>,
    onDaySelected: (Triple<Int, Int, Int>, Boolean) -> Unit
) {
    var selected by remember { mutableStateOf(preselected) }
    var state: CalendarState by remember { mutableStateOf(CalendarState.SelectingDate) }

    val days = listOf("M", "D", "M", "D", "F", "S", "S")
    val daysMed = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    val months = listOf("", "Januar", "Feburar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")

    var firstDayWeekday by remember { mutableStateOf(0) }
    var daysInMonth by remember { mutableStateOf(30) }

    LaunchedEffect(selected) {
        LocalDate(
            year = selected.third,
            monthNumber = selected.second,
            dayOfMonth = 1
        ).let {
            firstDayWeekday = it.dayOfWeek.ordinal
            daysInMonth = it.until(it.plus(1, DateTimeUnit.MONTH), DateTimeUnit.DAY)
        }

        println("Selected: $selected")
        println("First day weekday: $firstDayWeekday")
        println("Days in month: $daysInMonth")

        onDaySelected(selected, false)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(state) {
        state.let {
            if(it is CalendarState.SelectingYear) {
                listState.animateScrollToItem(selected.third - 1970)
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(12.dp)
        ) {
            Text(
                text = selected.third.toString(),
                modifier = Modifier.fillMaxWidth().clickable {
                    state = CalendarState.SelectingYear
                },
                style = MaterialTheme.typography.titleLarge,
                color = if(state == CalendarState.SelectingYear) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${getWeekday(firstDayWeekday, selected.first)}., ${selected.first}. ${months[selected.second].take(3)}",
                modifier = Modifier.fillMaxWidth().clickable { 
                    state = CalendarState.SelectingDate
                },
                style = MaterialTheme.typography.displaySmall,
                color = if(state == CalendarState.SelectingDate) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface
            )
        }

        Crossfade(
            targetState = state,
            modifier = Modifier.fillMaxWidth()
        ) { tgt ->
            when(tgt) {
                is CalendarState.SelectingDate -> {
                    Column(
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if(selected.second == 1) {
                                        selected = selected.copy(second = 12, third = selected.third - 1)
                                    } else {
                                        selected = selected.copy(second = selected.second - 1)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }

                            Text(
                                text = months[selected.second],
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if(selected.second == 12) {
                                        selected = selected.copy(second = 1, third = selected.third + 1)
                                    } else {
                                        selected = selected.copy(second = selected.second + 1)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        }
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                            Row(modifier = Modifier.weight(1f)) {
                                days.forEach {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            (0 until daysInMonth).prependNulls(firstDayWeekday).chunked(7).forEach { daysOfWeek ->
                                Row(modifier = Modifier.weight(1f)) {
                                    daysOfWeek.forEach {
                                        val highlightModifier = if(it?.plus(1) == selected.first)
                                            Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                                        else Modifier

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clickable {
                                                    if(it != null) {
                                                        selected = selected.copy(first = it.plus(1))
                                                        onDaySelected(selected, true)
                                                    }
                                                }
                                                .then(highlightModifier)
                                        ) {


                                            if(it != null) {
                                                Text(
                                                    text = it.plus(1).toString(),
                                                    modifier = Modifier.align(Alignment.Center),
                                                    color = if(it.plus(1) == selected.first)
                                                                MaterialTheme.colorScheme.onPrimary
                                                            else Color.Unspecified
                                                )
                                            }
                                        }
                                    }
                                    repeat(7 - daysOfWeek.size) {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is CalendarState.SelectingYear -> {
                    LazyColumn {
                        items((1970..2100).toList()) {
                            val style = if(it == selected.third)
                                MaterialTheme.typography.titleLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            else MaterialTheme.typography.bodyLarge

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable {
                                        selected = selected.copy(third = it + 1)
                                        state = CalendarState.SelectingDate
                                    }
                            ) {
                                Text(
                                    text = it.toString(),
                                    style = style,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

sealed class CalendarState {
    data object SelectingDate : CalendarState()
    data object SelectingYear : CalendarState()
}

fun <T> Iterable<T>.prependNulls(n: Int): List<T?> = List(n) { null } + this