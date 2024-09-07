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

package de.grobox.transportr.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.ui.transport.composables.ProductComposable

//@Composable
//fun LegComposable(
//    leg: Leg,
//    previousLeg: Leg?,
//    nextLeg: Leg?,
//    modifier: Modifier = Modifier
//) {
//    var collapsed by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = modifier
//    ) {
//        LegComponent(
//            type = LegType.FIRST,
//            thisLegColor = Color.Red,
//            collapsed = collapsed
//        )
//        LegComponent(
//            type = LegType.MIDDLE,
//            thisLegColor = Color.Red,
//            collapsed = collapsed
//        )
//        LegComponent(
//            type = LegType.INTERMEDIATE_LAST,
//            thisLegColor = Color.Red,
//            otherLegColor = Color.Blue,
//            collapsed = collapsed
//        )
//        LegComponent(
//            type = LegType.INTERMEDIATE,
//            thisLegColor = Color.Blue
//        )
//        LegComponent(
//            type = LegType.INTERMEDIATE_FIRST,
//            thisLegColor = Color.Red,
//            otherLegColor = Color.Blue
//        )
//
//        LegComponent(
//            type = LegType.LAST,
//            thisLegColor = Color.Red,
//            collapsed = collapsed
//        )
////        StartingLeg()
////        MiddleLeg()
////        IntermediaryLastLeg()
////        IntermediaryLeg()
//    }
//}


@Composable
fun IntermediaryLastLeg(
    legBaseSize: Dp = 20.dp,
    strokeWidth: Dp = 4.5.dp,
    nextLegColor: Color = Color.Blue,
    legColor: Color = Color.Red
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(38.dp)
        ) {
            Text(
                text = "12:22"
            )
            Text(
                text = "+12",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(legBaseSize)
                .fillMaxHeight()
        ) {
            val legBaseSizePx = legBaseSize.toPx() / 1.8f
            val legHeight = (size.height / 2 - legBaseSizePx)
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 10f)

            drawLine(
                color = legColor,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, legHeight * 1.1f),
                strokeWidth = strokeWidth.toPx()
            )

            drawArc(
                color = legColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset((size.width / 2) - legBaseSizePx, (size.height / 2)- legBaseSizePx),
                size = Size(legBaseSizePx * 2, legBaseSizePx * 2)
            )

            drawArc(
                color = nextLegColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset((size.width / 2) - legBaseSizePx, (size.height / 2)- legBaseSizePx),
                size = Size(legBaseSizePx * 2, legBaseSizePx * 2)
            )

//            drawCircle(
//                color = legColor,
//                radius = legBaseSizePx,
//                center = Offset(size.width / 2, size.height / 2),
//            )



            drawLine(
                color = nextLegColor,
                start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
                end = Offset(size.width / 2, size.height),
                pathEffect = pathEffect,
                strokeWidth = strokeWidth.toPx()
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Michelau (Oberfr)",
                style = MaterialTheme.typography.bodyLarge,
            )

        }

        IconButton(
            onClick = { /* TODO */ }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.more)
            )
        }
    }
}

@Composable
fun IntermediaryLeg(
    legBaseSize: Dp = 20.dp,
    strokeWidth: Dp = 4.5.dp,
    legColor: Color = Color.Blue
) {
    Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(38.dp)
        ) { }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(legBaseSize)
                .fillMaxHeight()
        ) {
            val legBaseSizePx = legBaseSize.toPx() / 2.2f
            val legHeight = (size.height / 2 - legBaseSizePx)
            val strokeWidthPx = strokeWidth.toPx()
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 10f)

            drawLine(
                color = legColor,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                pathEffect = pathEffect,
                strokeWidth = strokeWidthPx
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Umsteigen (5min)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}

//@Composable
//fun IntermediaryFirstLeg(
//    legBaseSize: Dp = 20.dp,
//    strokeWidth: Dp = 4.5.dp,
//    lastLegColor: Color = Color.Blue,
//    legColor: Color = Color.Red
//) {
//    Row(
//        modifier = Modifier.height(IntrinsicSize.Min),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(
//            horizontalAlignment = Alignment.End,
//            modifier = Modifier.width(38.dp)
//        ) {
//            Text(
//                text = "12:22"
//            )
//            Text(
//                text = "+12",
//                style = MaterialTheme.typography.bodySmall,
//            )
//        }
//
//        Canvas(
//            modifier = Modifier
//                .padding(horizontal = 8.dp)
//                .width(legBaseSize)
//                .fillMaxHeight()
//        ) {
//            val legBaseSizePx = legBaseSize.toPx() / 1.8f
//            val legHeight = (size.height / 2 - legBaseSizePx)
//            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
//
//            drawLine(
//                color = lastLegColor,
//                start = Offset(size.width / 2, 0f),
//                end = Offset(size.width / 2, legHeight * 1.1f),
//                pathEffect = pathEffect,
//                strokeWidth = strokeWidth.toPx()
//            )
//
//            drawArc(
//                color = lastLegColor,
//                startAngle = 180f,
//                sweepAngle = 180f,
//                useCenter = true,
//                topLeft = Offset((size.width / 2) - legBaseSizePx, (size.height / 2)- legBaseSizePx),
//                size = Size(legBaseSizePx * 2, legBaseSizePx * 2)
//            )
//
//            drawArc(
//                color = legColor,
//                startAngle = 0f,
//                sweepAngle = 180f,
//                useCenter = true,
//                topLeft = Offset((size.width / 2) - legBaseSizePx, (size.height / 2)- legBaseSizePx),
//                size = Size(legBaseSizePx * 2, legBaseSizePx * 2)
//            )
//
//            drawLine(
//                color = legColor,
//                start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
//                end = Offset(size.width / 2, size.height),
//                strokeWidth = strokeWidth.toPx()
//            )
//        }
//
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = "Michelau (Oberfr)",
//                style = MaterialTheme.typography.bodyLarge,
//            )
//
//        }
//
//        IconButton(
//            onClick = { /* TODO */ }
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.ic_more_vert),
//                contentDescription = stringResource(R.string.more)
//            )
//        }
//    }
//}



@Composable
fun StartingLeg(
    legBaseSize: Dp = 20.dp,
    strokeWidth: Dp = 4.5.dp,
    legColor: Color = Color.Red
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(38.dp)
        ) {
            Text("12:22")
            Text(
                text = "+12",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(legBaseSize)
                .fillMaxHeight()
                .padding(top = 4.dp)
        ) {
            val legBaseSizePx = legBaseSize.toPx() / 1.8f

            drawCircle(
                color = legColor,
                radius = legBaseSizePx,
                center = Offset(size.width / 2, legBaseSizePx)
            )

            drawLine(
                color = legColor,
                start = Offset(size.width / 2, legBaseSizePx),
                end = Offset(size.width / 2, size.height),
                strokeWidth = strokeWidth.toPx()
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "From Location with quite a long name",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                ProductComposable(
                    drawableId = R.drawable.product_regional_train,
                    label = "RE1234",
                )

                Text(
                    text = "Direction of Line that can also be very long"
                )
            }

            Text(
                text = "This is an important message that can be several lines long and even include HTML.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
                    .clickable {
                        /* TODO */
                    }
            ) {
                Text(
                    text = "42 Stops"
                )

                Icon(
                    painter = painterResource(R.drawable.ic_action_navigation_unfold_more),
                    contentDescription = stringResource(R.string.more),
                    modifier = Modifier.width(48.dp)
                )
            }
        }

        IconButton(
            onClick = { /* TODO */ }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.more)
            )
        }



    }
}

@Composable
fun MiddleLeg(
    legBaseSize: Dp = 20.dp,
    strokeWidth: Dp = 4.5.dp,
    legColor: Color = Color.Red
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(38.dp)
        ) {
            Text(
                text = "12:22",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = MaterialTheme.typography.bodySmall.fontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "+12",
                lineHeight = MaterialTheme.typography.bodySmall.fontSize,
                style = MaterialTheme.typography.bodySmall,
                fontSize = MaterialTheme.typography.labelSmall.fontSize
            )
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(legBaseSize)
                .fillMaxHeight()
        ) {
            val legBaseSizePx = legBaseSize.toPx() / 2.2f
            val legHeight = (size.height / 2 - legBaseSizePx)
            val strokeWidthPx = strokeWidth.toPx()

            drawLine(
                color = legColor,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, legHeight * 1.1f),
                strokeWidth = strokeWidthPx
            )

            drawCircle(
                color = legColor,
                radius = legBaseSizePx - (strokeWidthPx/2),
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(
                    width = strokeWidthPx
                )
            )

            drawLine(
                color = legColor,
                start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
                end = Offset(size.width / 2, size.height),
                strokeWidth = strokeWidthPx
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Michelau (Oberfr)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }

        IconButton(
            onClick = { /* TODO */ }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.more)
            )
        }
    }
}

@Composable
fun LastLeg(
    legBaseSize: Dp = 20.dp,
    strokeWidth: Dp = 4.5.dp,
    legColor: Color = Color.Red
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(38.dp)
        ) {
            Text(
                text = "12:22"
            )
            Text(
                text = "+12",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(legBaseSize)
                .fillMaxHeight()
        ) {
            val legBaseSizePx = legBaseSize.toPx() / 1.8f
            val legHeight = (size.height / 2 - legBaseSizePx)

            drawLine(
                color = legColor,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, legHeight * 1.1f),
                strokeWidth = strokeWidth.toPx()
            )

            drawCircle(
                color = legColor,
                radius = legBaseSizePx,
                center = Offset(size.width / 2, size.height / 2),

                )

//            drawLine(
//                color = legColor,
//                start = Offset(size.width / 2, legHeight + (1.8f * legBaseSizePx)),
//                end = Offset(size.width / 2, size.height),
//                strokeWidth = strokeWidth
//            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Michelau (Oberfr)",
                style = MaterialTheme.typography.bodyLarge,
            )

        }

        IconButton(
            onClick = { /* TODO */ }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.more)
            )
        }
    }
}

//@Composable
//@Preview
//fun LegComposablePreview() {
//    ElevatedCard {
//        LegComposable(
//            modifier = Modifier.padding(8.dp)
//        )
//    }
//}