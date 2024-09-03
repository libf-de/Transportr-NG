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

package de.grobox.transportr.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsRadioButton
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.SettingsSwitch
import de.grobox.transportr.R
import de.schildbach.pte.NetworkProvider
import org.koin.androidx.compose.koinViewModel

enum class Dialogs {
    ROUTING_OPTIMIZE,
    ROUTING_SPEED,
    THEME,
    LOCALE
}

@Composable
fun <T> RadioSetting(
    setting: MutableState<T>,
    value: T,
    valueNameMap: Map<T, Int>,
    confirm: () -> Unit = {}
) {
    Row(
        modifier = Modifier.clickable { setting.value = value; confirm() }
    ) {
        RadioButton(
            selected = setting.value == value,
            onClick = { setting.value = value; confirm() }
        )
        Text(text = valueNameMap[value]?.let { stringResource(id = it) } ?: value.toString())
    }
}

@Composable
fun <T> RadioSettingDialog(
    value: T,
    valueNameMap: Map<T, Int>,
    onConfirmation: (newValue: T) -> Unit,
    onDismissRequest: () -> Unit,
    icon: @Composable () -> Unit = { },
) {
    var optimizeSetting = remember { mutableStateOf(value) }

    AlertDialog(
        icon = { icon() },
        title = {
            Text(text = stringResource(id = R.string.pref_optimize))
        },
        text = {
            Column {
                valueNameMap.keys.forEach {
                    RadioSetting(
                        setting = optimizeSetting,
                        value = it,
                        valueNameMap = valueNameMap,
                        confirm = { onConfirmation(optimizeSetting.value) }
                    )
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = { },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun <T> RadioSettingGroup(
    value: T,
    valueNameMap: Map<T, Int>,
    onSelect: (newValue: T) -> Unit,
) {
    Column {
        valueNameMap.keys.forEach { v ->
            SettingsRadioButton(
                state = value == v,
                title = { Text(text = valueNameMap[v]?.let { stringResource(id = it) } ?: v.toString()) },
                modifier = Modifier,
                enabled = true,
                onClick = {
                    onSelect(v)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSelectNetworkClicked: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val viewModel: SettingsViewModel = koinViewModel()

    val networkName by viewModel.transportNetwork.observeAsState()
    val optimize by viewModel.optimize.observeAsState(NetworkProvider.Optimize.valueOf(viewModel.defaultOptimize))
    val walkSpeed by viewModel.walkSpeed.observeAsState(NetworkProvider.WalkSpeed.valueOf(viewModel.defaultWalkSpeed))
    val theme by viewModel.theme.observeAsState(viewModel.defaultTheme)
    val locale by viewModel.locale.observeAsState(viewModel.defaultLocale)
    val showWhenLocked by viewModel.showWhenLocked.observeAsState(viewModel.defaultShowWhenLocked)

    var openDialog: Dialogs? by remember { mutableStateOf(null) }

    when (openDialog) {
//        Dialogs.ROUTING_OPTIMIZE -> {
//            RadioSettingDialog(
//                value = optimize,
//                valueNameMap = viewModel.optimizeNames,
//                onConfirmation = {
//                    viewModel.setOptimize(it)
//                    openDialog = null
//                },
//                onDismissRequest = {
//                    openDialog = null
//                }
//            )
//        }

        Dialogs.ROUTING_SPEED -> {
            RadioSettingDialog(
                value = walkSpeed,
                valueNameMap = viewModel.walkSpeedNames,
                onConfirmation = {
                    viewModel.setWalkSpeed(it)
                    openDialog = null
                },
                onDismissRequest = {
                    openDialog = null
                }
            )
        }

        Dialogs.THEME -> {
            RadioSettingDialog(
                value = theme,
                valueNameMap = viewModel.themeNames,
                onConfirmation = {
                    viewModel.setTheme(it)
                    openDialog = null
                },
                onDismissRequest = {
                    openDialog = null
                }
            )
        }

        Dialogs.LOCALE -> {
            RadioSettingDialog(
                value = locale,
                valueNameMap = viewModel.localeNames,
                onConfirmation = {
                    viewModel.setLocale(it)
                    openDialog = null
                },
                onDismissRequest = {
                    openDialog = null
                }
            )
        }

        else -> { /* no dialog */ }
    }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        stringResource(id = R.string.drawer_settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { pv ->
        LazyColumn(
            modifier = Modifier.padding(pv)
        ) {
            item {
                SettingsGroup(
                    modifier = Modifier,
                    enabled = true,
                    title = { Text(text = stringResource(id = R.string.pref_transport_network)) },
                    contentPadding = PaddingValues(16.dp),
                ) {
                    SettingsMenuLink(
                        title = { Text(text = stringResource(id = R.string.network)) },
                        subtitle = { Text(text = networkName?.getName(LocalContext.current) ?: "unknown network" ) },
                        modifier = Modifier,
                        enabled = true,
                        icon = {
                            //TODO: proper icon
                            Icon(
                                painter = painterResource(id = R.drawable.ic_globe),
                                contentDescription = null
                            )},
                        onClick = { onSelectNetworkClicked() },
                    )
                }
            }

            item {
                SettingsGroup(
                    modifier = Modifier,
                    enabled = true,
                    title = { Text(text = stringResource(id = R.string.pref_directions_routing)) },
                    contentPadding = PaddingValues(16.dp),
                ) {
                    SettingsMenuLink(
                        title = { Text(text = stringResource(id = R.string.pref_optimize)) },
                        subtitle = { Text(text = viewModel.optimizeNames[optimize]?.let { stringResource(id = it) } ?: "unknown" ) },
                        modifier = Modifier,
                        enabled = true,
                        icon = {
                            //TODO: proper icon
                            Icon(
                                painter = painterResource(id = R.drawable.ic_globe),
                                contentDescription = null
                            )},
                        onClick = { openDialog = Dialogs.ROUTING_OPTIMIZE },
                    )

                    ExpandableSetting(expanded = openDialog == Dialogs.ROUTING_OPTIMIZE) {
                        RadioSettingGroup(
                            value = optimize,
                            valueNameMap = viewModel.optimizeNames
                        ) {
                            viewModel.setOptimize(it)
                            openDialog = null
                        }
                    }


                    SettingsSlider(
                        value = walkSpeed.ordinal.toFloat(),
                        valueRange = 0f..(NetworkProvider.WalkSpeed.entries.size - 1).toFloat(),
                        steps = 1,
                        title = { Text(text = stringResource(id = R.string.pref_walk_speed)) },
                        subtitle = { Text(text = viewModel.walkSpeedNames[walkSpeed]?.let { stringResource(id = it) } ?: "unknown" ) },
                        modifier = Modifier,
                        enabled = true,
                        icon = {
                               //TODO
                        },
                        onValueChange = {
                            viewModel.setWalkSpeed(NetworkProvider.WalkSpeed.entries[it.toInt()])
                        },
                    )
                }
            }

            item {
                SettingsGroup(
                    modifier = Modifier,
                    enabled = true,
                    title = { Text(text = stringResource(id = R.string.pref_display)) },
                    contentPadding = PaddingValues(16.dp),
                ) {
                    SettingsMenuLink(
                        title = { Text(text = stringResource(id = R.string.pref_theme_title)) },
                        subtitle = { Text(text = viewModel.themeNames[theme]?.let { stringResource(id = it) } ?: "unknown" ) },
                        modifier = Modifier,
                        enabled = true,
                        icon = {
                            //TODO: proper icon
                            Icon(
                                painter = painterResource(id = R.drawable.ic_globe),
                                contentDescription = null
                            )},
                        onClick = { openDialog = Dialogs.THEME },
                    )

                    SettingsMenuLink(
                        title = { Text(text = stringResource(id = R.string.pref_language_title)) },
                        subtitle = { Text(text = viewModel.localeNames[locale]?.let { stringResource(id = it) } ?: "unknown" ) },
                        modifier = Modifier,
                        enabled = true,
                        icon = {
                            //TODO: proper icon
                            Icon(
                                painter = painterResource(id = R.drawable.ic_globe),
                                contentDescription = null
                            )},
                        onClick = { openDialog = Dialogs.LOCALE },
                    )

                    SettingsSwitch(
                        state = showWhenLocked,
                        title = { Text(text = stringResource(id = R.string.pref_show_when_locked_title)) },
                        subtitle = { Text(text = stringResource(id = R.string.pref_show_when_locked_summary)) },
                        modifier = Modifier,
                        enabled = true,
                        icon = {
                            /* TODO */
                        },
                        onCheckedChange = viewModel::setShowWhenLocked,
                    )
                }
            }
        }


    }
}

@Composable
fun ExpandableSetting(
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
    ) {
        if(expanded) content()

    }
}