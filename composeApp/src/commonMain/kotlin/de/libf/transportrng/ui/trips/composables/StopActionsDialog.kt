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
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.Stop
import de.libf.transportrng.data.utils.getName
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_show_on_external_map
import transportr_ng.composeapp.generated.resources.cancel
import transportr_ng.composeapp.generated.resources.connections_by_stop
import transportr_ng.composeapp.generated.resources.continue_journey_later
import transportr_ng.composeapp.generated.resources.find_departures
import transportr_ng.composeapp.generated.resources.ic_action_departures
import transportr_ng.composeapp.generated.resources.ic_action_external_map
import transportr_ng.composeapp.generated.resources.ic_location
import transportr_ng.composeapp.generated.resources.ic_menu_directions
import transportr_ng.composeapp.generated.resources.ic_stop

data class StopActions(
    val showStationOnExternalMap: (Stop) -> Unit,
    val findDepartures: (Stop) -> Unit,
    val findConnections: (Stop) -> Unit,
    val continueJourneyLater: (Stop) -> Unit
)

@Composable
fun StopActionsDialog(
    selectedStop: MutableState<Stop?>,
    actions: StopActions
) {
    val isStop = selectedStop.value?.plannedArrivalTime != null
    if(selectedStop.value != null) {
        AlertDialog(
            onDismissRequest = {
                selectedStop.value = null
            },
            icon = {
                Icon(
                    painter = painterResource(
                        if(isStop)
                            Res.drawable.ic_stop
                        else
                            Res.drawable.ic_location
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = selectedStop.value?.location.getName() ?: "Haltestelle",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            actions.showStationOnExternalMap(selectedStop.value!!)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_action_external_map),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(Res.string.action_show_on_external_map),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }

                    if(isStop) {
                        FilledTonalButton(
                            onClick = {
                                actions.findDepartures(selectedStop.value!!)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_action_departures),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(Res.string.find_departures),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            actions.findConnections(selectedStop.value!!)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_menu_directions),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(Res.string.connections_by_stop),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            actions.continueJourneyLater(selectedStop.value!!)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(Res.string.continue_journey_later),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }

                    TextButton(
                        onClick = { selectedStop.value = null },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        )
    }
}