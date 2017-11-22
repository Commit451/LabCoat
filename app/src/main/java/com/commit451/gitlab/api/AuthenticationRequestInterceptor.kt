package com.commit451.gitlab.api

import com.commit451.gitlab.model.Account
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Adds the private token to all requests
 */
class AuthenticationRequestInterceptor(private val account: Account) : Interceptor {

    companion object {
        const val PRIVATE_TOKEN_HEADER_FIELD = "Private-Token"
        private const val PRIVATE_TOKEN_GET_PARAMETER = "private_token"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        var url = request.url()

        var cleanUrl = url.toString().toLowerCase()
        cleanUrl = cleanUrl.substring(cleanUrl.indexOf(':'))

        var cleanServerUrl = account.serverUrl.toString().toLowerCase()
        cleanServerUrl = cleanServerUrl.substring(cleanServerUrl.indexOf(':'))

        if (cleanUrl.startsWith(cleanServerUrl)) {
            val privateToken = account.privateToken
            if (privateToken == null) {
                Timber.e("The private token was null")
            } else {
                url = url.newBuilder()
                        .addQueryParameter(PRIVATE_TOKEN_GET_PARAMETER, privateToken)
                        .build()

                request = request.newBuilder()
                        .header(PRIVATE_TOKEN_HEADER_FIELD, privateToken)
                        .url(url)
                        .build()
            }
        }

        return chain.proceed(request)
    }
}
