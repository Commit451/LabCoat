package com.commit451.gitlab.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet

import com.commit451.easel.Easel
import com.commit451.gitlab.R

import me.zhanghai.android.materialprogressbar.MaterialProgressBar


/**
 * A subclass of ProgressWheel that automagically themes itself to the accent color
 */
class LabCoatProgressView : MaterialProgressBar {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        val color = Easel.getThemeAttrColor(context, R.attr.colorAccent)
        progressTintList = ColorStateList.valueOf(color)
    }
}
