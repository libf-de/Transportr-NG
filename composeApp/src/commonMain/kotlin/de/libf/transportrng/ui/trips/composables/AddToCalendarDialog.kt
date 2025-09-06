package de.libf.transportrng.ui.trips.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.utils.getName
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_show_on_external_map
import transportr_ng.composeapp.generated.resources.action_trip_calendar
import transportr_ng.composeapp.generated.resources.add
import transportr_ng.composeapp.generated.resources.cancel
import transportr_ng.composeapp.generated.resources.connections_by_stop
import transportr_ng.composeapp.generated.resources.continue_journey_later
import transportr_ng.composeapp.generated.resources.find_departures
import transportr_ng.composeapp.generated.resources.ic_action_calendar
import transportr_ng.composeapp.generated.resources.ic_action_departures
import transportr_ng.composeapp.generated.resources.ic_action_external_map
import transportr_ng.composeapp.generated.resources.ic_location
import transportr_ng.composeapp.generated.resources.ic_menu_directions
import transportr_ng.composeapp.generated.resources.ic_stop

@Composable
fun AddToCalendarDialog(
    tripState: MutableState<Trip?>,
    onConfirm: (Trip) -> Unit
) {
    if(tripState.value != null) {
        AlertDialog(
            onDismissRequest = { tripState.value = null },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_action_calendar),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(Res.string.action_trip_calendar) + "?",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(tripState.value!!)
                        tripState.value = null
                    }
                ) {
                    Text(stringResource(Res.string.add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tripState.value = null
                    }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}