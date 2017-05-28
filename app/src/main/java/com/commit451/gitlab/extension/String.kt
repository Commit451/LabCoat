package com.commit451.gitlab.extension

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Base64
import io.reactivex.Single

//String extension methods

fun String.base64Decode(): Single<ByteArray> {
    return Single.fromCallable {
        Base64.decode(this, Base64.DEFAULT)
    }
}

/**
 * Assures HTML is formatted the same way pre and post Android N
 */
@Suppress("DEPRECATION")
fun String.formatAsHtml(): Spanned {
    if (Build.VERSION.SDK_INT >= 24) {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        return Html.fromHtml(this)
    }
}