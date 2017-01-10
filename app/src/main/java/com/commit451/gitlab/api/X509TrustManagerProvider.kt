package com.commit451.gitlab.api

import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.util.Arrays

import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Gets the X509TrustManager on the system and caches it
 */
object X509TrustManagerProvider {

    private var x509TrustManager: X509TrustManager? = null

    /**
     * Get the static [X509TrustManager] for the system

     * @return the static get
     */
    fun get(): X509TrustManager {
        if (x509TrustManager == null) {
            try {
                init()
            } catch (any: Exception) {
                //If they don't have X509 trust manager, they have bigger problems
                throw RuntimeException(any)
            }

        }
        return x509TrustManager
    }

    /**
     * Getting the [X509TrustManager] as shown in the [okhttp3.OkHttpClient.Builder.sslSocketFactory] docs
     * @throws NoSuchAlgorithmException
     * *
     * @throws KeyStoreException
     */
    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class)
    private fun init() {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        x509TrustManager = trustManagers[0] as X509TrustManager
    }
}
