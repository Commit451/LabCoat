package com.commit451.gitlab.extension

import android.graphics.Color
import android.support.annotation.ColorInt
import com.commit451.easel.Easel
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
    if (Easel.isColorDark(getColor())) {
        return Color.WHITE
    }
    return Easel.darkerColor(getColor(), 0.25f)
}