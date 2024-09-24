package de.libf.transportrng.ui.map.composables

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import de.libf.transportrng.data.gps.GpsState
import de.libf.transportrng.data.gps.enabled
import de.libf.transportrng.data.maplibrecompat.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_gps

private val GpsState.containerColor: Color
    @Composable get() = if(this.enabled) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

private val GpsState.contentColor: Color
    @Composable get() = if(this.enabled) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

@Composable
fun GpsFabComposable(
    gpsState: GpsState,
    modifier: Modifier = Modifier,
    onClick: (LatLng?) -> Unit,
    onLongClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
                is PressInteraction.Release -> {
                    if (isLongClick.not() /*&& gpsState is GpsState.Enabled*/){
                        val latLng = if(gpsState is GpsState.Enabled)LatLng(
                            latitude = gpsState.location.lat,
                            longitude = gpsState.location.lon
                        ) else null

                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick(latLng)
                    }
                }
                is PressInteraction.Cancel -> {
                    isLongClick = false
                }
            }
        }
    }

    GpsSaturationAnimatable(
        gpsState = gpsState,
    ) { saturation ->
        SmallFloatingActionButton(
            interactionSource = interactionSource,
            onClick = {},
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = gpsState.contentColor,
            modifier = modifier.saturation(saturation)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_gps),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun GpsSaturationAnimatable(
    gpsState: GpsState,
    content: @Composable (animatedValue: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val animatedValue = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "infiniteAnimation"
    )

    val finalValue by animateFloatAsState(
        targetValue = when(gpsState) {
            is GpsState.Enabled -> 5f
            is GpsState.EnabledSearching -> animatedValue.value
            else -> 0f
        },
        animationSpec = tween(500),
        label = "finalAnimation"
    )

    content(finalValue)
}

class SaturationModifier(private val amount: Float) : DrawModifier {
    override fun ContentDrawScope.draw() {
        val saturationMatrix = ColorMatrix().apply { setToSaturation(amount) }
        val saturationFilter = ColorFilter.colorMatrix(saturationMatrix)
        val paint = Paint().apply {
            colorFilter = saturationFilter
        }
        drawIntoCanvas {
            it.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
            drawContent()
            it.restore()
        }
    }
}

private fun Modifier.saturation(amount: Float) = this.then(SaturationModifier(amount))