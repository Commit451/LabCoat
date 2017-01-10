package com.commit451.gitlab.api.rss

import android.net.Uri

import com.commit451.gitlab.model.Account
import com.commit451.gitlab.util.ConversionUtil

import org.simpleframework.xml.transform.Transform

/**
 * Uri Transformer
 */
class UriTransform(private val account: Account) : Transform<Uri> {

    @Throws(Exception::class)
    override fun read(value: String): Uri? {
        return ConversionUtil.toUri(account, value)
    }

    @Throws(Exception::class)
    override fun write(value: Uri): String? {
        return ConversionUtil.fromUri(value)
    }
}
