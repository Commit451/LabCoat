package com.commit451.gitlab.extension

import com.commit451.gitlab.util.FileUtil
import io.reactivex.Single
import okhttp3.MultipartBody
import java.io.File

//File extensions

fun File.toPart(): Single<MultipartBody.Part> {
    return Single.fromCallable {
        FileUtil.toPart(this)
    }
}