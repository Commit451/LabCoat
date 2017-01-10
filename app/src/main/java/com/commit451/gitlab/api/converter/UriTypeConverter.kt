package com.commit451.gitlab.api.converter

import android.net.Uri

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter

/**
 * Simple Uri type converter
 */
class UriTypeConverter : StringBasedTypeConverter<Uri>() {

    override fun convertToString(`object`: Uri): String {
        return `object`.toString()
    }

    override fun getFromString(string: String?): Uri? {
        if (string != null) {
            return Uri.parse(string)
        } else {
            return null
        }
    }
}
