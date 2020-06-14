package com.commit451.gitlab.extension

import autodispose2.CompletableSubscribeProxy
import autodispose2.autoDispose
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.fragment.BaseFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

fun Completable.with(baseActivity: BaseActivity): CompletableSubscribeProxy {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(baseActivity.scopeProvider)
}

fun Completable.with(baseFragment: BaseFragment): CompletableSubscribeProxy {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(baseFragment.scopeProvider)
}
