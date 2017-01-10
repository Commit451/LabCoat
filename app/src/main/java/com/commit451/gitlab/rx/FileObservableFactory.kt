package com.commit451.gitlab.rx

import com.commit451.gitlab.util.FileUtil
import io.reactivex.Single
import okhttp3.MultipartBody
import java.io.File

/**
 * Rx'ifies file util calls
 */
object FileObservableFactory {

    fun toPart(file: File): Single<MultipartBody.Part> {
        return Single.defer { Single.just(FileUtil.toPart(file)) }
    }
}
