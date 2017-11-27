package com.commit451.gitlab.util

import android.graphics.Color
import android.support.annotation.ColorInt

/**
 * Does cool things with colors
 */
object ColorUtil {

    fun convertColorIntToString(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    fun getTitleColor(@ColorInt color: Int): Int {
        val darkness = 1.0 - (0.299 * Color.red(color).toDouble() +
                0.587 * Color.green(color).toDouble() +
                0.114 * Color.blue(color).toDouble()) / 255.0

        if (darkness < 0.35) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.25f
            return Color.HSVToColor(hsv)
        }
        return Color.WHITE;
    }
}
