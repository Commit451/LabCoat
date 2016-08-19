package com.commit451.gitlab.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Thread things
 */
public class ThreadUtil {

    public static void postOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(runnable);
    }
}
