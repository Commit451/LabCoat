package com.commit451.gitlab.navigation

import android.app.Activity

import com.commit451.gitlab.R
import com.novoda.simplechromecustomtabs.navigation.IntentCustomizer
import com.novoda.simplechromecustomtabs.navigation.SimpleChromeCustomTabsIntentBuilder

import java.lang.ref.WeakReference

/**
 * Customize custom intents
 */
class LabCoatIntentCustomizer(activity: Activity, private val colorToolbar: Int) : IntentCustomizer {

    private val activity: WeakReference<Activity>

    init {
        this.activity = WeakReference(activity)
    }

    override fun onCustomiseIntent(simpleChromeCustomTabsIntentBuilder: SimpleChromeCustomTabsIntentBuilder): SimpleChromeCustomTabsIntentBuilder {
        val activity = this.activity.get() ?: return simpleChromeCustomTabsIntentBuilder
        return simpleChromeCustomTabsIntentBuilder
                .withStartAnimations(activity, R.anim.fade_in, R.anim.do_nothing)
                .withExitAnimations(activity, R.anim.do_nothing, R.anim.fade_out)
                .withToolbarColor(colorToolbar)
    }
}
