package com.commit451.gitlab.extension

import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.fragment.BaseFragment
import com.commit451.reptar.kotlin.fromIoToMainThread
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Completable

fun Completable.with(baseActivity: BaseActivity): CompletableSubscribeProxy {
    return this.fromIoToMainThread().autoDisposable(baseActivity.scopeProvider)
}

fun Completable.with(baseFragment: BaseFragment): CompletableSubscribeProxy {
    return this.fromIoToMainThread().autoDisposable(baseFragment.scopeProvider)
}
