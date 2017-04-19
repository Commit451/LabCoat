package com.commit451.gitlab.extension

import android.util.Base64
import io.reactivex.Single

//String extension methods

fun String.base64Decode(): Single<ByteArray> {
    return Single.defer {
        Single.just(Base64.decode(this, Base64.DEFAULT))
    }
}
