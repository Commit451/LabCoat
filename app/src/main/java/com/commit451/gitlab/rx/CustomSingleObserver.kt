package com.commit451.gitlab.rx

import com.commit451.reptar.CancellationFailureChecker
import com.commit451.reptar.ComposableSingleObserver

/**
 * A custom observer that ignores [java.util.concurrent.CancellationException]s
 */
abstract class CustomSingleObserver<T> : ComposableSingleObserver<T>() {
    init {
        add(CancellationFailureChecker())
    }
}
