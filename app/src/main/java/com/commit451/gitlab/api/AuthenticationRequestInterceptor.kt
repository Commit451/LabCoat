package com.commit451.gitlab.api

import com.commit451.gitlab.model.Account
import okhttp3.Interceptor
import okhttp3.Response
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
        val serverUrl = account.serverUrl ?: "https://example.com"

        if (isSameServer(url.toString(), serverUrl)) {
            val privateToken = account.privateToken
            privateToken?.let {
                request = request.newBuilder()
                        .header(PRIVATE_TOKEN_HEADER_FIELD, it)
                        .url(url)
                        .build()
            }
        }

        return chain.proceed(request)
    }

    private fun isSameServer(requestUrl: String, serverUrl: String): Boolean {
        var cleanUrl = requestUrl.toLowerCase()
        cleanUrl = cleanUrl.substring(cleanUrl.indexOf(':'))

        var cleanServerUrl = serverUrl.toLowerCase()
        cleanServerUrl = cleanServerUrl.substring(cleanServerUrl.indexOf(':'))

        return cleanUrl.startsWith(cleanServerUrl)
    }
}
