package com.commit451.gitlab.api

import java.security.KeyStore
import java.util.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Gets the X509TrustManager on the system and caches it
 */
object X509TrustManagerProvider {

    val x509TrustManager: X509TrustManager by lazy {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val nullKeystore: KeyStore? = null
        trustManagerFactory.init(nullKeystore)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        trustManagers[0] as X509TrustManager
    }
}
