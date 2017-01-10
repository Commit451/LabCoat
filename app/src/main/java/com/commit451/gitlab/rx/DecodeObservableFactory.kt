package com.commit451.gitlab.rx

import android.util.Base64
import io.reactivex.Single

/**
 * Observable that decodes a byte array
 */
object DecodeObservableFactory {

    fun newDecode(string: String): Single<ByteArray> {
        return Single.defer { Single.just(Base64.decode(string, Base64.DEFAULT)) }
    }
}
