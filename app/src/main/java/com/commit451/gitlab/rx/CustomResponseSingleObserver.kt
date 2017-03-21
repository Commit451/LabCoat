package com.commit451.gitlab.rx

import android.support.annotation.CallSuper
import com.commit451.gitlab.api.NullBodyException
import com.commit451.reptar.CancellationFailureChecker
import com.commit451.reptar.retrofit.ResponseSingleObserver

/**
 * A custom observer that ignores [java.util.concurrent.CancellationException]s
 */
abstract class CustomResponseSingleObserver<T> : ResponseSingleObserver<T>() {
    init {
        add(CancellationFailureChecker())
    }

    abstract fun responseNonNullSuccess(t: T)

    @CallSuper
    override fun responseSuccess(t: T?) {
        if (t == null) {
            error(NullBodyException())
        } else {
            responseNonNullSuccess(t)
        }
    }
}
