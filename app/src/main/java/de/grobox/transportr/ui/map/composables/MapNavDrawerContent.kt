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

package de.grobox.transportr.ui.map.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.networks.TransportNetwork

@Composable
fun MapNavDrawerContent(
    transportNetworks: List<TransportNetwork>,
    onNetworkClick: (TransportNetwork) -> Unit,
    onNetworkCardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChangelogClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onReportAProblemClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier
            .width(360.dp)
            .systemBarsPadding()
            .padding(horizontal = 28.dp, vertical = 8.dp)
        ) {
            TransportNetworkComposable(
                networks = transportNetworks,
                onNetworkClick = onNetworkClick,
                onClick = onNetworkCardClick
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.drawer_settings)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_action_settings),
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onSettingsClick() }
            )

            HorizontalDivider()

            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.drawer_changelog)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_action_changelog),
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onChangelogClick() }
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.drawer_contributors)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_people),
                        contentDescription = null
                        )
                },
                selected = false,
                onClick = { onContributorsClick() }
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.drawer_report_issue)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_bug_report),
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onReportAProblemClick() }
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(R.string.drawer_about)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_action_about),
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onAboutClick() }
            )
        }
    }


}