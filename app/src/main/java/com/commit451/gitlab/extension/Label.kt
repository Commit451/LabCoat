package com.commit451.gitlab.extension

import android.graphics.Color
import android.support.annotation.ColorInt
import com.commit451.gitlab.model.api.Label
import com.commit451.gitlab.util.ColorUtil

@ColorInt
fun Label.getColor(): Int {
    try {
        return Color.parseColor(color)
    } catch (e: Exception) {
        return Color.TRANSPARENT
    }
}

@ColorInt
fun Label.getTitleColor(): Int {
    return ColorUtil.getTitleColor(getColor())
}