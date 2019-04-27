package com.commit451.firebaseshim

import android.content.Context
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import timber.log.Timber

/**
 * Enables Fabric
 */
object FirebaseShim {

    private const val PATH = "io.fabric.sdk.android.Fabric"

    fun init(context: Context, isDebug: Boolean) {
        if (!isDebug && hasFirebaseOnPath()) {
            Fabric.with(context, Crashlytics())
            Timber.plant(CrashlyticsWtfTree())
        }
    }

    private fun hasFirebaseOnPath(): Boolean {
        return try {
            Class.forName(PATH)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
