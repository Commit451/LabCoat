package com.commit451.gitlab.rx;

import com.commit451.reptar.CancellationFailureChecker;
import com.commit451.reptar.ComposableSingleObserver;

/**
 * A custom observer that ignores {@link java.util.concurrent.CancellationException}s
 */
public abstract class CustomSingleObserver<T> extends ComposableSingleObserver<T> {

    public CustomSingleObserver() {
        add(new CancellationFailureChecker());
    }
}
