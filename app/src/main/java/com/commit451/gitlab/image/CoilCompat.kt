package com.commit451.gitlab.image

import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import coil.Coil
import coil.api.get
import coil.request.GetRequestBuilder
import kotlinx.coroutines.runBlocking

object CoilCompat {

    @JvmStatic
    @WorkerThread
    fun getBlocking(
            url: String,
            builder: GetRequestBuilder.() -> Unit
    ): Drawable = runBlocking { Coil.get(url, builder) }
}
