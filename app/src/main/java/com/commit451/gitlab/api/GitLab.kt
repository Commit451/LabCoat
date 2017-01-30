package com.commit451.gitlab.api

import android.net.Uri
import com.bluelinelabs.logansquare.LoganSquare
import com.commit451.gitlab.api.converter.UriTypeConverter

/**
 * Provides access to all the GitLab things
 */
object GitLab {

    fun init() {
        /**
         * Register our type converters on our singleton LoganSquare get. Needs to be set here
         * since we are fetching accounts immediately with LoganSquare
         */
        LoganSquare.registerTypeConverter(Uri::class.java, UriTypeConverter())
    }
}