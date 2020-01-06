package com.commit451.gitlab.extension

import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.api.BodyWithPagination
import com.commit451.gitlab.fragment.BaseFragment
import com.commit451.gitlab.util.LinkHeaderParser
import com.uber.autodispose.SingleSubscribeProxy
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import retrofit2.Response

fun <T> Single<T>.with(baseActivity: BaseActivity): SingleSubscribeProxy<T> {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(baseActivity.scopeProvider)
}

fun <T> Single<T>.with(baseFragment: BaseFragment): SingleSubscribeProxy<T> {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(baseFragment.scopeProvider)
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

fun <T> Single<Response<T>>.mapResponseSuccessWithPaginationData(): Single<BodyWithPagination<T>> {
    return flatMap { response ->
        if (!response.isSuccessful) {
            error(HttpException(response))
        } else {
            val body = response.body()!!
            val paginationData = LinkHeaderParser.parse(response)
            Single.just(BodyWithPagination(body, paginationData))
        }
    }
}
