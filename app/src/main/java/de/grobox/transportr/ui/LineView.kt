/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.ui

import android.content.Context
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat.getDrawable
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KLine
import de.grobox.transportr.utils.TransportrUtils.dpToPx
import de.grobox.transportr.utils.TransportrUtils.getDrawableForProduct

class LineView(context: Context, attr: AttributeSet?) : AppCompatTextView(context, attr) {

    fun setLine(line: KLine) {
        // get colors
        val foregroundColor = line.style?.foregroundColor
        val backgroundColor = line.style?.backgroundColor

        // set colored background
        val backgroundDrawable = getDrawable(context, R.drawable.line_box) as GradientDrawable
        backgroundDrawable.mutate() // mutate to not share state with other instances
        if (backgroundColor != null) backgroundDrawable.setColor(backgroundColor)
        @Suppress("DEPRECATION")
        setBackgroundDrawable(backgroundDrawable)

        // correct padding that gets lost when setting background
        setPadding(0, dpToPx(context, 2), dpToPx(context, 4), dpToPx(context, 2))

        // product icon
        if (line.product != null) {
            setDrawable(getDrawableForProduct(line.product), foregroundColor)
        }

        // set colored label
        text = line.label
        if (foregroundColor != null) setTextColor(foregroundColor)
    }

    fun setWalk() {
        setCompoundDrawablesWithIntrinsicBounds(getDrawable(context, R.drawable.ic_walk), null, null, null)
        setBackgroundResource(R.drawable.walk_box)
    }

    private fun setDrawable(@DrawableRes res: Int, @ColorInt color: Int?) {
        val drawable = getDrawable(context, res)!!
        color?.let {
            drawable.mutate().setColorFilter(color, SRC_IN)
        }
        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

}
