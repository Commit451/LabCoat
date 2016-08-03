package com.commit451.gitlab.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.commit451.gitlab.BuildConfig;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Enables Fabric
 */
public class FabricUtil {

    public static void init(@NonNull Context context) {
        // Start crashlytics if enabled
        if (!BuildConfig.DEBUG) {
            Fabric.with(context, new Crashlytics());
        }
    }
}
