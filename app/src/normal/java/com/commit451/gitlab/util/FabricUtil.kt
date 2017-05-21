package com.commit451.gitlab.util

import android.content.Context

import com.commit451.gitlab.BuildConfig
import com.crashlytics.android.Crashlytics

import io.fabric.sdk.android.Fabric
import timber.log.Timber

/**
 * Enables Fabric
 */
object FabricUtil {

    fun init(context: Context) {
        // Start crashlytics if enabled
        if (!BuildConfig.DEBUG) {
            Fabric.with(context, Crashlytics())
            Timber.plant(CrashlyticsWtfTree())
        }
    }
}
