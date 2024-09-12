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

package de.grobox.transportr.ui.map.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

class SpeechBubbleDrawable(
    private val context: Context,
    private val title: String,
    private val content: String,
    private val backgroundColor: Int,
    private val outlineColor: Int,
    private val textColor: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val contentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val width: Float
    private val height: Float

    private val titleHeight: Float
    private val contentPerLineHeight: Float
    private val contentLines: List<String>

    init {
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL

        outlinePaint.color = outlineColor
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.strokeWidth = 8f

        titlePaint.color = textColor
        titlePaint.textSize = 48f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textAlign = Paint.Align.CENTER

        contentPaint.color = textColor
        contentPaint.textSize = 36f
        contentPaint.textAlign = Paint.Align.CENTER

        contentLines = content.split("\n")

        width = maxOf(
            titlePaint.measureText(title),
            contentPaint.measureText(contentLines.maxByOrNull { it.length } ?: "")
        ) + 64f

        titleHeight = titlePaint.fontMetrics.height()
        contentPerLineHeight = contentPaint.fontMetrics.height()

        height = ((titleHeight + contentPerLineHeight * contentLines.size) + 32f) * 2.5f
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(4f, 4f, width - 4f, (height / 2.5f) - 4f, 16f, 16f, paint)
        canvas.drawRoundRect(4f, 4f, width - 4f, (height / 2.5f) - 4f, 16f, 16f, outlinePaint)

        path.reset()
        path.moveTo((width/2) - (width/10), (height / 2.5f) - 10f)
        path.lineTo((width/2) - (width/10), height / 2.5f)
        path.lineTo(width / 2, height / 2)
        path.lineTo((width/2) + (width/10), height / 2.5f)
        path.lineTo((width/2) + (width/10), (height / 2.5f) - 10f)

        canvas.drawPath(path, paint)

        canvas.drawLine((width/2) - (width/10) - 4f, (height / 2.5f) -4f, 2f + width/2,  2f + height/2, outlinePaint)
        canvas.drawLine(width/2, height / 2, (width/2) + (width/10) + 4f, (height/2.5f) - 4f, outlinePaint)
        canvas.drawLine((width/2) - (width/10) - 4f, (height / 2.5f) -8f, (width/2) + (width/10) + 8f, (height / 2.5f) -8f, paint)

        //canvas.drawPath(path, outlinePaint)


//        canvas.drawLine(width / 2, height / 2.5f, width / 2, height / 2f, paint)

        // Draw the main bubble
        path.reset()
        path.moveTo(0f, 20f)
        path.lineTo(0f, height - 40f)
        path.quadTo(0f, height - 20f, 20f, height - 20f)
        path.lineTo(width - 40f, height - 20f)
        path.quadTo(width - 20f, height - 20f, width - 20f, height - 40f)
        path.lineTo(width - 20f, 20f)
        path.quadTo(width - 20f, 0f, width - 40f, 0f)
        path.lineTo(20f, 0f)
        path.quadTo(0f, 0f, 0f, 20f)

        // Draw the tail
        path.moveTo(width - 40f, height - 20f)
        path.lineTo(width, height)
        path.lineTo(width - 60f, height - 20f)

//        canvas.drawPath(path, paint)

        // Draw the title
        val xPos = width / 2
        val titleYPos = 3f - titlePaint.fontMetrics.top
        //val titleYPos = height * 0.3f - ((titlePaint.descent() + titlePaint.ascent()) / 2)
        canvas.drawText(title, xPos, titleYPos, titlePaint)

        contentLines.forEachIndexed { index, s ->
            val lineY = titleYPos + 16f + (index * contentPerLineHeight) - contentPaint.fontMetrics.top
//            val centerX = (width / 2) - (contentPaint.measureText(s) / 2)
            canvas.drawText(s, xPos, lineY, contentPaint)
        }

        // Draw the content
        //val contentYPos = height * 0.6f - ((contentPaint.descent() + contentPaint.ascent()) / 2)
        //canvas.drawText(content, xPos, contentYPos, contentPaint)
    }

    override fun getIntrinsicWidth(): Int = width.toInt()
    override fun getIntrinsicHeight(): Int = height.toInt()


    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        titlePaint.alpha = alpha
        contentPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        titlePaint.colorFilter = colorFilter
        contentPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun toBitmapDrawable(): BitmapDrawable {
        val width = this.intrinsicWidth
        val height = this.intrinsicHeight

        if (width <= 0 || height <= 0) {
            throw IllegalStateException("Drawable has no intrinsic dimensions")
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, width, height)
        this.draw(canvas)
        return BitmapDrawable(context.resources, bitmap)
    }


    fun toBitmapDrawable(width: Int, height: Int): BitmapDrawable {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, width, height)
        draw(canvas)
        return BitmapDrawable(context.resources, bitmap)
    }
}

private fun Paint.FontMetrics.height(): Float {
    return this.bottom - this.top
}

fun createSpeechBubbleDrawable(
    context: Context,
    title: String,
    content: String,
    backgroundColor: Int,
    outlineColor: Int,
    textColor: Int
): SpeechBubbleDrawable {
    return SpeechBubbleDrawable(context, title, content, backgroundColor, outlineColor, textColor)
}
