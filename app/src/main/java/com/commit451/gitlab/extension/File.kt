package com.commit451.gitlab.extension

import com.commit451.gitlab.util.FileUtil
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import java.io.File

fun File.toPart(): Single<MultipartBody.Part> {
    return Single.fromCallable {
        FileUtil.toPart(this)
    }
}
