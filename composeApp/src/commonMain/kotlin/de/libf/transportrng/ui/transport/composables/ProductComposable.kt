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

package de.libf.transportrng.ui.transport.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.libf.ptek.dto.Line
import de.libf.ptek.dto.Product
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.ic_action_about
import transportr_ng.composeapp.generated.resources.product_bus
import transportr_ng.composeapp.generated.resources.product_cablecar
import transportr_ng.composeapp.generated.resources.product_ferry
import transportr_ng.composeapp.generated.resources.product_high_speed_train
import transportr_ng.composeapp.generated.resources.product_on_demand
import transportr_ng.composeapp.generated.resources.product_regional_train
import transportr_ng.composeapp.generated.resources.product_suburban_train
import transportr_ng.composeapp.generated.resources.product_subway
import transportr_ng.composeapp.generated.resources.product_tram

@Composable
fun ProductComposable(
    line: Line,
    modifier: Modifier = Modifier
) = ProductComposable(
    drawableId = line.product.getDrawableRes(),
    drawableDescription = line.product.getNameRes().let { stringResource(it) },
    backgroundColor = line.style?.backgroundColor?.let { Color(it) },
    foregroundColor = line.style?.foregroundColor?.let { Color(it) },
    label = line.label,
    modifier = modifier
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun ProductComposable(
    drawableId: DrawableResource? = null,
    drawableDescription: String? = null,
    backgroundColor: Color? = null,
    foregroundColor: Color? = null,
    label: String? = null,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp, 6.dp, 6.dp, 6.dp))
            .background(backgroundColor ?: Color.Gray)
            .padding(start = 2.dp, top = 3.dp, bottom = 3.dp, end = 6.dp)
    ) {
        Icon(
            painter = painterResource(drawableId ?: Res.drawable.product_bus),
            contentDescription = drawableDescription ?: "",
            tint = foregroundColor ?: LocalContentColor.current,
            modifier = Modifier.padding(end = 4.dp)
        )


        Text(
            text = label ?: "",
//            fontFamily = FontFamily(
//                Font(Res.font.mw, FontWeight.Medium)
//            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = foregroundColor ?: Color.Unspecified
        )
    }
}


@OptIn(ExperimentalTextApi::class)
@Composable
@Preview
fun FontTest() {
    ElevatedCard {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProductComposable(
                drawableId = Res.drawable.product_regional_train,
                label = "RE 3"
            )

            ProductComposable(
                drawableId = Res.drawable.product_regional_train,
                label = "RE 3",
                fontWeight = FontWeight.Medium,
//                fontFamily = FontFamily(
//                    Font(R.font.mw, FontWeight.Medium)
//                )
            )
        }
    }
}

fun Product?.getNameRes(): StringResource = when(this) {
    Product.HIGH_SPEED_TRAIN -> Res.string.product_high_speed_train
    Product.REGIONAL_TRAIN -> Res.string.product_regional_train
    Product.SUBURBAN_TRAIN -> Res.string.product_suburban_train
    Product.SUBWAY -> Res.string.product_subway
    Product.TRAM -> Res.string.product_tram
    Product.BUS -> Res.string.product_bus
    Product.FERRY -> Res.string.product_ferry
    Product.CABLECAR -> Res.string.product_cablecar
    Product.ON_DEMAND -> Res.string.product_on_demand
    else -> Res.string.product_bus
}

fun Product?.getDrawableRes(): DrawableResource = when (this) {
    Product.HIGH_SPEED_TRAIN -> Res.drawable.product_high_speed_train
    Product.REGIONAL_TRAIN -> Res.drawable.product_regional_train
    Product.SUBURBAN_TRAIN -> Res.drawable.product_suburban_train
    Product.SUBWAY -> Res.drawable.product_subway
    Product.TRAM -> Res.drawable.product_tram
    Product.BUS -> Res.drawable.product_bus
    Product.FERRY -> Res.drawable.product_ferry
    Product.CABLECAR -> Res.drawable.product_cablecar
    Product.ON_DEMAND -> Res.drawable.product_on_demand
    null -> Res.drawable.product_bus
    else -> Res.drawable.ic_action_about
}