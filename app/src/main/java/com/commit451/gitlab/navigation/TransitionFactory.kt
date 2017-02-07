package com.commit451.gitlab.navigation


import android.app.ActivityOptions
import android.content.Context

import com.commit451.gitlab.R

/**
 * Creates transitions
 */
object TransitionFactory {

    fun createFadeInOptions(context: Context): ActivityOptions {
        return ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.do_nothing)
    }
}
