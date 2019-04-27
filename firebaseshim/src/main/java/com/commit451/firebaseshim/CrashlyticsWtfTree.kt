package com.commit451.firebaseshim

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Logs all [timber.log.Timber.wtf] calls to Crashlytics
 */
class CrashlyticsWtfTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ASSERT) {
            Crashlytics.log(Log.ASSERT, tag, message)
        }
    }
}
