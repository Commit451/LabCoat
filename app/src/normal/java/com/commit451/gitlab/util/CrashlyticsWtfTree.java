package com.commit451.gitlab.util;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

/**
 * Logs all {@link timber.log.Timber#wtf(String, Object...)} calls to Crashlytics
 */
public class CrashlyticsWtfTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.ASSERT) {
            Crashlytics.log(Log.ASSERT, tag, message);
        }
    }
}
