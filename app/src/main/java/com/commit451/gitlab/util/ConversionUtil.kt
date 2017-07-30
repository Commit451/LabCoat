package com.commit451.gitlab.util

import android.net.Uri
import com.commit451.gitlab.model.Account

/**
 * Converts things!
 */
object ConversionUtil {

    fun fromUri(uri: Uri?): String? {
        if (uri == null) {
            return null
        }

        return uri.toString()
    }

    fun toUri(account: Account?, uriString: String?): Uri? {
        if (uriString == null) {
            return null
        }
        if (uriString.isEmpty()) {
            return Uri.EMPTY
        }

        val uri = Uri.parse(uriString)
        if (!uri.isRelative) {
            return uri
        }

        if (account == null) {
            return uri
        }

        val serverUri = Uri.parse(account.serverUrl)
        val builder = serverUri
                .buildUpon()
                .encodedQuery(uri.encodedQuery)
                .encodedFragment(uri.encodedFragment)

        if (uri.path.startsWith("/")) {
            builder.encodedPath(uri.encodedPath)
        } else {
            builder.appendEncodedPath(uri.encodedPath)
        }

        return builder.build()
    }
}
