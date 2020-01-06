package com.commit451.gitlab.extension

import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.fragment.BaseFragment
import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun Completable.with(baseActivity: BaseActivity): CompletableSubscribeProxy {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(baseActivity.scopeProvider)
}

fun Completable.with(baseFragment: BaseFragment): CompletableSubscribeProxy {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(baseFragment.scopeProvider)
}
