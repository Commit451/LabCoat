package com.commit451.gitlab.rx;

import com.commit451.reptar.CancellationFailureChecker;
import com.commit451.reptar.retrofit.ResponseSingleObserver;

/**
 * A custom observer that ignores {@link java.util.concurrent.CancellationException}s
 */
public abstract class CustomResponseSingleObserver<T> extends ResponseSingleObserver<T> {

    public CustomResponseSingleObserver() {
        add(new CancellationFailureChecker());
    }
}
