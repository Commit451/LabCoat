package com.commit451.gitlab.extension

import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.fragment.BaseFragment
import com.commit451.reptar.kotlin.fromIoToMainThread
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Single
import retrofit2.HttpException
import retrofit2.Response

fun <T> Single<T>.with(baseActivity: BaseActivity): SingleSubscribeProxy<T> {
    return this.fromIoToMainThread().autoDisposable(baseActivity.scopeProvider)
}

fun <T> Single<T>.with(baseFragment: BaseFragment): SingleSubscribeProxy<T> {
    return this.fromIoToMainThread().autoDisposable(baseFragment.scopeProvider)
}

fun <T> Single<Response<T>>.mapResponseSuccess(): Single<T> {
    return flatMap { response ->
        if (!response.isSuccessful) {
            error(HttpException(response))
        } else {
            Single.just(response.body())
        }
    }
}
