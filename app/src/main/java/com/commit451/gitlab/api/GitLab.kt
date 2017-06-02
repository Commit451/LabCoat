package com.commit451.gitlab.api

import android.net.Uri
import com.bluelinelabs.logansquare.LoganSquare
import com.commit451.gitlab.api.converter.UriTypeConverter
import okhttp3.OkHttpClient

/**
 * Provides access to all the GitLab things. Wraps RSS and the Retrofit service, in
 * case we need to do overrides or global
 */
class GitLab(val client: OkHttpClient, gitLabService: GitLabService, gitLabRss: GitLabRss): GitLabService by gitLabService,
        GitLabRss by gitLabRss {

    companion object {
        fun init() {
            /**
             * Register our type converters on our singleton LoganSquare get. Needs to be set here
             * since we are fetching accounts immediately with LoganSquare
             */
            LoganSquare.registerTypeConverter(Uri::class.java, UriTypeConverter())
        }
    }

}