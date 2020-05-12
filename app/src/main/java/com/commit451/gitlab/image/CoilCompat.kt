package com.commit451.gitlab.image

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import coil.Coil
import coil.request.ErrorResult
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import coil.request.SuccessResult
import kotlinx.coroutines.runBlocking

object CoilCompat {

    @JvmStatic
    @WorkerThread
    fun getBlocking(
            context: Context,
            url: String,
            builder: GetRequestBuilder.() -> Unit
    ): Drawable = runBlocking {
        when (val result = Coil.imageLoader(context).execute(GetRequest.Builder(context).data(url).apply(builder).build())) {
            is SuccessResult -> result.drawable
            is ErrorResult -> throw result.throwable
        }
    }
}
