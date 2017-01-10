package com.commit451.gitlab.util

import android.os.Handler
import android.os.Looper

/**
 * Thread things
 */
object ThreadUtil {

    fun postOnMainThread(runnable: Runnable) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(runnable)
    }
}
