package com.commit451.gitlab.util;

import android.support.annotation.ColorInt;

/**
 * Does cool things with colors
 */
public class ColorUtil {

    public static String convertColorIntToString(@ColorInt int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
