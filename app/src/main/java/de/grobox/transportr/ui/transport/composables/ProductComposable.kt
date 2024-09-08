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

package de.grobox.transportr.ui.transport.composables

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.data.dto.KProduct

@Composable
fun ProductComposable(
    line: KLine,
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
    drawableId: Int? = null,
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
            painter = painterResource(drawableId ?: R.drawable.product_bus),
            contentDescription = drawableDescription ?: "",
            tint = foregroundColor ?: LocalContentColor.current,
            modifier = Modifier.padding(end = 4.dp)
        )


        Text(
            text = label ?: "",
            fontFamily = FontFamily(
                Font(R.font.mw, FontWeight.Medium)
            ),
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
                drawableId = R.drawable.product_regional_train,
                label = "RE 3"
            )

            ProductComposable(
                drawableId = R.drawable.product_regional_train,
                label = "RE 3",
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily(
                    Font(R.font.mw, FontWeight.Medium)
                )
            )
        }
    }
}

fun KProduct?.getNameRes(): Int = when(this) {
    KProduct.HIGH_SPEED_TRAIN -> R.string.product_high_speed_train
    KProduct.REGIONAL_TRAIN -> R.string.product_regional_train
    KProduct.SUBURBAN_TRAIN -> R.string.product_suburban_train
    KProduct.SUBWAY -> R.string.product_subway
    KProduct.TRAM -> R.string.product_tram
    KProduct.BUS -> R.string.product_bus
    KProduct.FERRY -> R.string.product_ferry
    KProduct.CABLECAR -> R.string.product_cablecar
    KProduct.ON_DEMAND -> R.string.product_on_demand
    else -> R.string.product_bus
}

fun KProduct?.getDrawableRes(): Int = when (this) {
    KProduct.HIGH_SPEED_TRAIN -> R.drawable.product_high_speed_train
    KProduct.REGIONAL_TRAIN -> R.drawable.product_regional_train
    KProduct.SUBURBAN_TRAIN -> R.drawable.product_suburban_train
    KProduct.SUBWAY -> R.drawable.product_subway
    KProduct.TRAM -> R.drawable.product_tram
    KProduct.BUS -> R.drawable.product_bus
    KProduct.FERRY -> R.drawable.product_ferry
    KProduct.CABLECAR -> R.drawable.product_cablecar
    KProduct.ON_DEMAND -> R.drawable.product_on_demand
    null -> R.drawable.product_bus
    else -> R.drawable.ic_action_about
}