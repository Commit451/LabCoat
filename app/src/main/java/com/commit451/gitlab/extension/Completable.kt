package com.commit451.gitlab.extension

import com.commit451.reptar.kotlin.fromIoToMainThread
import com.trello.rxlifecycle2.LifecycleTransformer
import io.reactivex.Completable

fun Completable.setup(transformer: LifecycleTransformer<Any>): Completable {
    return this.compose(transformer).fromIoToMainThread()
}
