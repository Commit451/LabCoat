package com.commit451.gitlab.util

import `in`.uncod.android.bypass.Bypass
import `in`.uncod.android.bypass.ImageSpanClickListener
import android.content.Context

/**
 * Created by adibk on 4/15/17.
 */
object BypassFactory {

    fun create(context: Context): Bypass {
        return Bypass(context)
    }

    fun create(context: Context, clickListener: ImageSpanClickListener): Bypass {
        val bypass = Bypass(context)
        bypass.setImageSpanClickListener (clickListener)
        return bypass
    }
}