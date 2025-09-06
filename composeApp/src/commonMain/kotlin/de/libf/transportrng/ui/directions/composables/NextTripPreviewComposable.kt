package de.libf.transportrng.ui.directions.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.libf.ptek.dto.IndividualLeg
import de.libf.ptek.dto.Leg
import de.libf.ptek.dto.PublicLeg
import de.libf.ptek.dto.Trip
import de.libf.ptek.dto.min
import de.libf.transportrng.data.utils.formatDuration
import de.libf.transportrng.data.utils.getName
import de.libf.transportrng.data.utils.getStandardFare
import de.libf.transportrng.data.utils.hasProblem
import de.libf.transportrng.ui.transport.composables.getDrawableRes
import de.libf.transportrng.ui.trips.composables.DelayTextComposable
import de.libf.transportrng.ui.trips.composables.forEachWithNeighbors
import de.libf.transportrng.ui.trips.search.getArrivalTimes
import de.libf.transportrng.ui.trips.search.getDepartureTimes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.action_navigation_expand
import transportr_ng.composeapp.generated.resources.and
import transportr_ng.composeapp.generated.resources.ic_walk
import transportr_ng.composeapp.generated.resources.till
import transportr_ng.composeapp.generated.resources.trip_from
import transportr_ng.composeapp.generated.resources.trip_via
import kotlin.math.max

private val Leg.previewText: String
    get() = if(this is PublicLeg)
                this.line.label ?: ""
            else
                "${this.min}min"

@Composable
fun NextTripPreviewComposable(
    trip: Trip,
    showFrom: Boolean = true,
    onClick: () -> Unit,
) {
    var departureTime by remember { mutableStateOf("") }
    var departureDelay by remember { mutableStateOf("") }
    var departureName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var warning by remember { mutableStateOf(false) }
    var arrivalTime by remember { mutableStateOf("") }
    var arrivalDelay by remember { mutableStateOf("") }
    var arrivalName by remember { mutableStateOf("") }
    var tripVia by remember { mutableStateOf(AnnotatedString("")) }

    val tripPreviewAttrs = remember { NextTripPreviewAttrs() }

    val str_and = stringResource(Res.string.and)

    LaunchedEffect(trip, str_and) {
        tripVia = buildAnnotatedString {
            val legNames = trip.legs
                .flatMapIndexed { i, it ->
                    if(it !is PublicLeg) emptyList()
                    else if(i != 0 && it == trip.firstPublicLeg) listOf(it.arrival.uniqueShortName)
                    else listOfNotNull(it.departure.uniqueShortName, it.arrival.uniqueShortName)
                }
                .filter { it != trip.from.uniqueShortName && it != trip.to.uniqueShortName }
                .distinct()

            legNames.forEachIndexed { i, str ->
                if(str == trip.via?.uniqueShortName) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(str)
                    }
                } else {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(str)
                    }
                }

                if (i < legNames.size - 2) {
                    append(", ")
                } else if(i == legNames.size - 2) {
                    append(" $str_and ")
                }
            }
        }
    }

    LaunchedEffect(trip) {
        val departureData = getDepartureTimes(trip)
        departureTime = departureData.first
        departureDelay = departureData.second
        departureName = trip.from.getName() ?: "???"

        duration = trip.duration.formatDuration() ?: ""
        price = trip.getStandardFare() ?: ""
        warning = trip.hasProblem()

        val arrivalData = getArrivalTimes(trip)
        arrivalTime = arrivalData.first
        arrivalDelay = arrivalData.second
        arrivalName = trip.to.getName() ?: "???"
    }

    //val busIcon = painterResource(R.drawable.product_bus)
    val tripIcons = trip.legs.mapNotNull {
        if(it is PublicLeg)
            it.line.product to painterResource(it.line.product.getDrawableRes())
        else null
    }.plus(null to painterResource(Res.drawable.ic_walk)).toMap()

    val textMeasurer = rememberTextMeasurer()
    val publicLblStyle = MaterialTheme.typography.labelMedium.copy(
        color = Color.Gray
    )
    val indivLblStyle = MaterialTheme.typography.labelSmall.copy(
        color = Color.DarkGray
    )

    val previewHeight = LocalDensity.current.run {
        publicLblStyle.lineHeight.toDp() + (tripPreviewAttrs.textVPad * 2)
    }

    OutlinedCard(
        modifier = Modifier.clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
            ) {
                Text(
                    text = departureTime,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
                    fontSize = 18.sp
                )

                DelayTextComposable(departureDelay)

                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = stringResource(Res.string.till),
                    modifier = Modifier.size(MaterialTheme.typography.bodyMedium.fontSize.value.dp)
                )

                Text(
                    text = arrivalTime,
                    fontWeight = FontWeight.Bold,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp
                )

                DelayTextComposable(arrivalDelay)

                Spacer(Modifier.weight(1f))

                Text(duration)
            }

            Canvas(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(previewHeight)
                    .fillMaxWidth()
            ) {
                computeLegPositions(
                    textMeasurer = textMeasurer,
                    publicLblStyle = publicLblStyle,
                    indivLblStyle = indivLblStyle,
                    trip = trip,
                    totalWidth = size.width,
                    attrs = tripPreviewAttrs
                )?.entries?.toList()?.forEachWithNeighbors { prev, it, next ->
                    val leg = it.key
                    val legColor = leg.colorOr(Color.Yellow)

                    if(leg is PublicLeg) {
                        publicLeg(
                            icon = tripIcons[leg.line.product],
                            start = it.value.start,
                            segmentWidth = it.value.width,
                            backgroundColor = legColor,
                            text = it.value.text!!,
                            attrs = tripPreviewAttrs
                        )
                    } else if(leg.min > 2 || prev == null || next == null) {
                        individualLeg(
                            icon = tripIcons[null],
                            start = it.value.start,
                            segmentWidth = it.value.width,
                            backgroundColor = legColor,
                            text = it.value.text,
                            attrs = tripPreviewAttrs
                        )
                    }
                } ?: run {
                    drawText(
                        textMeasurer.measure(AnnotatedString("..."), publicLblStyle)
                    )
                }
            }

            trip.firstPublicLeg.takeIf { trip.legs.firstOrNull() !is PublicLeg }?.let { startStation ->
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(Res.string.trip_from))
                        }
                        append(" ")
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(startStation.departure.uniqueShortName)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            tripVia.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(stringResource(Res.string.trip_via))
                        }
                        append(" ")
                        append(it)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun DrawScope.computeLegPositions(
    textMeasurer: TextMeasurer,
    publicLblStyle: TextStyle,
    indivLblStyle: TextStyle,
    trip: Trip,
    totalWidth: Float,
    attrs: NextTripPreviewAttrs = NextTripPreviewAttrs()
): Map<Leg, LegPosition>? {
    val legWidths = mutableMapOf<Leg, Pair<TextLayoutResult?, Float>>()
    var spacers = mutableListOf<Float>()

    val tripDurationMin = trip.duration.toMins().toFloat()

    trip.legs.forEachIndexed { i, leg ->
        val proportionalWidth = (leg.min / tripDurationMin) * totalWidth
        val measuredText = leg.previewText.let {
            textMeasurer.measure(
                AnnotatedString(it),
                style = if(leg is PublicLeg) publicLblStyle.copy(
                    color = leg.line.style?.foregroundColor?.let(::Color) ?: publicLblStyle.color
                ) else indivLblStyle
            )
        }

        val legMinWidth = if(leg is PublicLeg)
            measuredText.size.width.toFloat().plus(attrs.textMinHPad * 2)
        else if(leg.min < 3)
            if(trip.legs.getOrNull(i - 1) == null || trip.legs.getOrNull(i + 1) == null)
                attrs.iconSize.toPx().plus(attrs.textMinHPad)
            else
                0f
        else
            measuredText.size.width.toFloat().plus(attrs.iconSize.toPx() + attrs.iconPadding.toPx())

        val width = max(proportionalWidth, legMinWidth)
        legWidths[leg] = Pair(measuredText, width)

        if(i < trip.legs.size - 1) {
            val nextLeg = trip.legs[i+1]
            val spacerDuration = (nextLeg.departureTime - leg.arrivalTime).toMins()
            spacers.add((spacerDuration / tripDurationMin) * totalWidth)
        }

        val totalSpacerWidth = spacers.sum()
        val totalLegWidth = legWidths.map { it.value.second }.sum()
        var totalUsedWidth = totalLegWidth + totalSpacerWidth
        var excessWidth = totalUsedWidth - totalWidth
        if(excessWidth > 0) {

            if(totalSpacerWidth >= excessWidth) {
                val scaleFactor = (totalSpacerWidth - excessWidth) / totalSpacerWidth
                spacers = spacers.map { it * scaleFactor }.toMutableList()
            } else {
                excessWidth -= totalSpacerWidth
                spacers = spacers.map { 0f }.toMutableList()

                val adjustableLegs = legWidths.filter {
                    it.value.second > (it.value.first?.size?.width?.toFloat() ?: 0f)
                }

                val totalAdjustableWidth = adjustableLegs.map {
                    it.value.second - (it.value.first?.size?.width?.toFloat() ?: 0f)
                }.sum()

                if(totalAdjustableWidth > excessWidth) {
                    val scaleWidth = excessWidth / totalAdjustableWidth
                    adjustableLegs.forEach {
                        val cur = legWidths[it.key]!!
                        legWidths[it.key] = cur.copy(second = cur.second - scaleWidth)
                    }
                } else {
                    return null
                }
            }
        }
    }

    var currentPos = 0f
    var index = 0
    return legWidths.mapValues {
        val curWidth = it.value.second
        LegPosition(
            start = currentPos,
            width = curWidth,
            text = it.value.first,
            startImmediate = index > 0 && spacers[index - 1] < 1f,
            endImmediate = index < spacers.size && spacers[index] < 1f,
        ).also {
            currentPos += curWidth
            if(index < spacers.size) currentPos += spacers[index]

            index += 1
        }
    }
}

private fun Leg.getLineLabelOrNull(): String? {
    return if(this is PublicLeg) this.line.label
    else null
}

private fun Long.toMins(): Long = (this / 1000 / 60)

fun DrawScope.publicLeg(
    icon: Painter?,
    start: Float,
    segmentWidth: Float,
    optHeight: Float = 0f,
    backgroundColor: Color,
    text: TextLayoutResult,
    attrs: NextTripPreviewAttrs = NextTripPreviewAttrs(),
) {
//    val yPos: Float = attrs.textY.toPx()
    val textPad = attrs.textVPad.toPx()
    var _iconSize: Float = if(icon != null) attrs.iconSize.toPx() else 0f
    val iconPadding: Float = attrs.iconPadding.toPx()

    val drawIcon = text.size.width + iconPadding + _iconSize <= segmentWidth
    val contentWidth = text.size.width.plus(if(drawIcon) iconPadding + _iconSize else 0f)

    val iconX = start + (segmentWidth - contentWidth) / 2
    val textX = iconX.plus(if(drawIcon) _iconSize + iconPadding else 0f)

    val textHeightHint = text.size.height.toFloat() + (textPad * 2)
    val iconHeightHint = attrs.iconSize.toPx() + (textPad * 2)
    val intrinsicHeight = max(textHeightHint, iconHeightHint)
    val height = max(optHeight, intrinsicHeight)
//    val height = max(optHeight, text.size.height + (textPad * 2))

    val cornerRadius = 3.dp.toPx()
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(start, 0f),
        size = Size(segmentWidth, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    // Draw the image
    icon?.takeIf { drawIcon }?.let {
        with(icon) {
            translate(left = iconX, top = (height / 2) - (_iconSize / 2)) {
                draw(
                    Size(_iconSize, _iconSize),
                    colorFilter = ColorFilter.tint(text.layoutInput.style.color)
                )
            }
        }
    }

    // Draw the text
    drawText(
        textLayoutResult = text,
        topLeft = Offset(
            textX,
            textPad
        )
    )
}

fun DrawScope.individualLeg(
    icon: Painter?,
    start: Float,
    segmentWidth: Float,
    optHeight: Float = 0f,
    backgroundColor: Color,
    text: TextLayoutResult?,
    attrs: NextTripPreviewAttrs = NextTripPreviewAttrs(),
) {
    if(icon == null) return

    val textPad = attrs.textVPad.toPx()
    val _iconSize: Float = attrs.iconSize.toPx()
    val iconPadding: Float = attrs.iconPadding.toPx()

    val drawText = text != null //&& (text.size.width + iconPadding + _iconSize <= segmentWidth)
    val oversizeText = text != null && (text.size.width + iconPadding + _iconSize <= segmentWidth)
    val contentWidth = _iconSize.plus(if(drawText && text != null) iconPadding + text.size.width else 0f)

    val iconX = start + (segmentWidth - contentWidth) / 2
    val textX = iconX.plus(if(drawText) _iconSize + iconPadding else 0f)

    val textHeightHint = (text?.size?.height?.toFloat() ?: 0f) + (textPad * 2)
    val iconHeightHint = attrs.iconSize.toPx() + (textPad * 2)
    val intrinsicHeight = max(textHeightHint, iconHeightHint)
    val height = max(optHeight, intrinsicHeight)

    val cornerRadius = 3.dp.toPx()
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(start, 0f),
        size = Size(segmentWidth, height),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = attrs.strokeWidth)
    )

    // Draw the image
    with(icon) {
        translate(left = iconX, top = (height / 2) - (_iconSize / 2)) {
            draw(
                Size(_iconSize, _iconSize),
                colorFilter = ColorFilter.tint(text?.layoutInput?.style?.color ?: Color.Blue)
            )
        }
    }

    // Draw the text
    text?.takeIf { drawText }?.let {
        val tLR = if(oversizeText)
            it.copy(
                layoutInput = it.layoutInput.let { le -> TextLayoutInput(
                    le.text.subSequence(TextRange(0, le.text.length - 1)),
                    le.style,
                    le.placeholders,
                    le.maxLines,
                    le.softWrap,
                    le.overflow,
                    le.density,
                    le.layoutDirection,
                    it.layoutInput.fontFamilyResolver,
                    le.constraints
                ) }
            )
        else it

        drawText(
            textLayoutResult = tLR,
            topLeft = Offset(
                textX,
                textPad
            )
        )
    }
}

data class NextTripPreviewAttrs(
    val iconSize: Dp = 12.dp,
    val iconPadding: Dp = 2.dp,
    val textVPad: Dp = 2.dp,
    val textMinHPad: Float = 8f,
    val strokeWidth: Float = 3f
)