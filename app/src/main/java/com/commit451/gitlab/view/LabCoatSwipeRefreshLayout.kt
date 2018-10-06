package com.commit451.gitlab.view

import android.content.Context
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.AttributeSet

import com.commit451.gitlab.R


/**
 * Just so that we do not have to keep setting the colors everywhere
 */
class LabCoatSwipeRefreshLayout : androidx.swiperefreshlayout.widget.SwipeRefreshLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        val colors = resources.getIntArray(R.array.cool_colors)
        setColorSchemeColors(*colors)
    }
}
