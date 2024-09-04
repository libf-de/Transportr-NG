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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class RoundedCornerShapeWithCutout(
    private val cornerRadius: Float,
    private val cutoutRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = Path().apply {
                val cutoutDiameter = cutoutRadius * 2

                // Start from top-left after the cutout
                moveTo(cutoutDiameter, 0f)

                // Top edge with cutout
                lineTo(size.width - cornerRadius, 0f)

                // Top-right corner
                arcTo(
                    Rect(
                        left = size.width - cornerRadius * 2,
                        top = 0f,
                        right = size.width,
                        bottom = cornerRadius * 2
                    ),
                    270f,
                    90f,
                    false
                )

                // Right edge
                lineTo(size.width, size.height - cornerRadius)

                // Bottom-right corner
                arcTo(
                    Rect(
                        left = size.width - cornerRadius * 2,
                        top = size.height - cornerRadius * 2,
                        right = size.width,
                        bottom = size.height
                    ),
                    0f,
                    90f,
                    false
                )

                // Bottom edge
                lineTo(cornerRadius, size.height)

                // Bottom-left corner
                arcTo(
                    Rect(
                        left = 0f,
                        top = size.height - cornerRadius * 2,
                        right = cornerRadius * 2,
                        bottom = size.height
                    ),
                    90f,
                    90f,
                    false
                )

                // Left edge
                lineTo(0f, cutoutRadius)

                // Cutout
                arcTo(
                    Rect(
                        left = 0f,
                        top = 0f,
                        right = cutoutDiameter,
                        bottom = cutoutDiameter
                    ),
                    180f,
                    180f,
                    false
                )

                close()
            }
        )
    }
}