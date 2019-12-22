package com.commit451.gitlab.util

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import coil.Coil
import coil.api.load

class CoilImageGetter(
        private val textView: TextView,
        private val sourceModifier: ((source: String) -> String)? = null
) : Html.ImageGetter {

    var maxWidth: Int = -1

    override fun getDrawable(source: String): Drawable {
        val drawablePlaceholder = BitmapDrawablePlaceHolder()
        val finalSource = sourceModifier?.invoke(source) ?: source

        Coil.load(textView.context, finalSource) {
            target { drawable ->
                if (maxWidth == -1) {
                    val horizontalPadding = textView.paddingLeft + textView.paddingRight
                    maxWidth = textView.measuredWidth - horizontalPadding
                    if (maxWidth == 0) {
                        maxWidth = Int.MAX_VALUE
                    }
                }

                val aspectRatio: Double = 1.0 * drawable.intrinsicWidth / drawable.intrinsicHeight
                val width = maxWidth.coerceAtMost(drawable.intrinsicWidth)
                val height = (width / aspectRatio).toInt()

                drawable.setBounds(0, 0, width, height)

                drawablePlaceholder.drawable = drawable
                drawablePlaceholder.setBounds(0, 0, width, height)

                textView.text = textView.text // invalidate() doesn't work correctly...

            }
        }
        return drawablePlaceholder
    }

    private class BitmapDrawablePlaceHolder : BitmapDrawable() {

        var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.draw(canvas)
        }
    }
}
