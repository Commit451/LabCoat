package com.commit451.gitlab.extension

import com.commit451.reptar.kotlin.fromIoToMainThread
import com.trello.rxlifecycle2.LifecycleTransformer
import io.reactivex.Single

fun <T> Single<T>.setup(transformer: LifecycleTransformer<T>): Single<T> {
    return this.compose(transformer).fromIoToMainThread()
}
