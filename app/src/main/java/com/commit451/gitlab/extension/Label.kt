package com.commit451.gitlab.extension

import android.graphics.Color
import android.support.annotation.ColorInt
import com.commit451.gitlab.model.api.Label

@ColorInt
fun Label.getColor(): Int {
    try {
        return Color.parseColor(color)
    } catch (e: Exception) {
        return Color.TRANSPARENT
    }

}