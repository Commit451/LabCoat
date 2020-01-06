package com.commit451.gitlab.extension

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Base64
import com.commit451.gitlab.model.Account
import io.reactivex.Single
import java.util.*

fun String.base64Decode(): Single<ByteArray> {
    return Single.fromCallable {
        Base64.decode(this, Base64.DEFAULT)
    }
}

/**
 * Assures HTML is formatted the same way pre and post Android N
 */
@Suppress("DEPRECATION")
fun String.formatAsHtml(imageGetter: Html.ImageGetter? = null, tagHandler: Html.TagHandler? = null): Spanned {
    return if (Build.VERSION.SDK_INT >= 24) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler)
    } else {
        Html.fromHtml(this, imageGetter, tagHandler)
    }
}

fun String.resolveUrl(account: Account): String {
    if (startsWith("/")) {
        return account.serverUrl + this.replaceFirst("/", "")
    }
    return this
}

fun String.toLowercaseRoot(): String {
    return toLowerCase(Locale.ROOT)
}
