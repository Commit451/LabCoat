package com.commit451.gitlab.util

import android.support.annotation.ColorInt

/**
 * Does cool things with colors
 */
object ColorUtil {

    fun convertColorIntToString(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}
