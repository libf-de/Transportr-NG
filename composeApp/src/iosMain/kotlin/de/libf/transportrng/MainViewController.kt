package de.libf.transportrng

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import de.libf.transportrng.ui.map.CompassMargins
import de.libf.transportrng.ui.map.MapPadding
import de.libf.transportrng.ui.map.MapViewComposable
import de.libf.transportrng.ui.map.UIKitMapView
import de.libf.transportrng.ui.map.iOsMapViewState
import de.libf.transportrng.ui.map.provideMapState

fun MainViewController() = ComposeUIViewController {
    App()
 }