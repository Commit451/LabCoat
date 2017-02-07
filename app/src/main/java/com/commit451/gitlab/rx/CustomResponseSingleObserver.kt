package com.commit451.gitlab.rx

import com.commit451.reptar.CancellationFailureChecker
import com.commit451.reptar.retrofit.ResponseSingleObserver

/**
 * A custom observer that ignores [java.util.concurrent.CancellationException]s
 */
abstract class CustomResponseSingleObserver<T> : ResponseSingleObserver<T>() {
    init {
        add(CancellationFailureChecker())
    }
}
