package com.commit451.gitlab

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter

/**
 * The worst type converter you will ever see. Only used in testing if Robolectric decides to break
 * and not know what a [android.net.Uri] or other class is
 */
class NullTypeConverter : StringBasedTypeConverter<Any>() {

    override fun convertToString(`object`: Any): String? {
        return null
    }

    override fun getFromString(string: String): Any? {
        return null
    }
}
