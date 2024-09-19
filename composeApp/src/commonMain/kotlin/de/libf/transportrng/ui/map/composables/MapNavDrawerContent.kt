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

package de.libf.transportrng.ui.map.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Dvr
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Dvr
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.libf.transportrng.data.networks.TransportNetwork
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.drawer_about
import transportr_ng.composeapp.generated.resources.drawer_changelog
import transportr_ng.composeapp.generated.resources.drawer_contributors
import transportr_ng.composeapp.generated.resources.drawer_report_issue
import transportr_ng.composeapp.generated.resources.drawer_settings
import transportr_ng.composeapp.generated.resources.ic_action_about
import transportr_ng.composeapp.generated.resources.ic_action_changelog
import transportr_ng.composeapp.generated.resources.ic_action_settings
import transportr_ng.composeapp.generated.resources.ic_bug_report
import transportr_ng.composeapp.generated.resources.ic_people

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
                label = { Text(text = stringResource(Res.string.drawer_settings)) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onSettingsClick() }
            )

            HorizontalDivider()

            NavigationDrawerItem(
                label = { Text(text = stringResource(Res.string.drawer_changelog)) },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Dvr,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onChangelogClick() }
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(Res.string.drawer_contributors)) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null
                        )
                },
                selected = false,
                onClick = { onContributorsClick() }
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(Res.string.drawer_report_issue)) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onReportAProblemClick() }
            )

            NavigationDrawerItem(
                label = { Text(text = stringResource(Res.string.drawer_about)) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = { onAboutClick() }
            )
        }
    }


}