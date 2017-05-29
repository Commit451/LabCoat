package com.commit451.gitlab.api

import com.commit451.gitlab.model.Account
import com.commit451.gitlab.ssl.CustomTrustManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Creates an OkHttpClient with the needed defaults
 */
object OkHttpClientFactory {

    /**
     * Creates an [OkHttpClient] configured with the account configuration

     * @param account                    the account
     * *
     * @param includeSignInAuthenticator include a sign in authenticator that checks signed in status
     * *
     * @return a configured [okhttp3.OkHttpClient.Builder]
     */
    @JvmOverloads fun create(account: Account, includeSignInAuthenticator: Boolean = true): OkHttpClient.Builder {
        // Do we even need a custom trust manager?
        // Yep. Otherwise SSL won't work properly with some configurations :) -Michi
        val customTrustManager = CustomTrustManager()
        customTrustManager.setTrustedCertificate(account.trustedCertificate)
        customTrustManager.setTrustedHostname(account.trustedHostname)
        customTrustManager.setPrivateKeyAlias(account.privateKeyAlias)

        val builder = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthenticationRequestInterceptor(account))
                .sslSocketFactory(customTrustManager.getSSLSocketFactory(), X509TrustManagerProvider.x509TrustManager)
                .hostnameVerifier(customTrustManager.getHostnameVerifier())

        if (includeSignInAuthenticator) {
            val authenticator = OpenSignInAuthenticator(account)
            builder.authenticator(authenticator)
                    .proxyAuthenticator(authenticator)
        }

        return builder
    }
}
