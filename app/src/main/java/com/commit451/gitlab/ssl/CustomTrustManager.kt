package com.commit451.gitlab.ssl

import com.commit451.gitlab.api.X509TrustManagerProvider
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Allows for custom configurations, such as custom trusted hostnames, custom trusted certificates,
 * and private keys
 */
class CustomTrustManager : X509TrustManager {

    private var trustedCertificate: String? = null
    private var trustedHostname: String? = null
    private var privateKeyAlias: String? = null
    private var sslSocketFactory: SSLSocketFactory? = null
    private var hostnameVerifier: HostnameVerifier? = null

    fun setTrustedCertificate(trustedCertificate: String?) {
        if (this.trustedCertificate == null && trustedCertificate == null || this.trustedCertificate != null && this.trustedCertificate == trustedCertificate) {
            return
        }

        this.trustedCertificate = trustedCertificate
        sslSocketFactory = null
    }

    fun setTrustedHostname(trustedHostname: String?) {
        if (this.trustedHostname == null && trustedHostname == null || this.trustedHostname != null && this.trustedHostname == trustedHostname) {
            return
        }

        this.trustedHostname = trustedHostname
        hostnameVerifier = null
    }

    fun setPrivateKeyAlias(privateKeyAlias: String?) {
        if (this.privateKeyAlias == null && privateKeyAlias == null || this.privateKeyAlias != null && this.privateKeyAlias == privateKeyAlias) {
            return
        }

        this.privateKeyAlias = privateKeyAlias
        sslSocketFactory = null
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        X509TrustManagerProvider.x509TrustManager.checkClientTrusted(chain, authType)
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        val cause: CertificateException
        try {
            X509TrustManagerProvider.x509TrustManager.checkServerTrusted(chain, authType)
            return
        } catch (e: CertificateException) {
            cause = e
        }

        if (trustedCertificate != null && trustedCertificate == X509Util.getFingerPrint(chain[0])) {
            return
        }

        throw X509CertificateException(cause.message!!, cause, chain)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return X509TrustManagerProvider.x509TrustManager.acceptedIssuers
    }

    fun getSSLSocketFactory(): SSLSocketFactory {
        if (sslSocketFactory != null) {
            return sslSocketFactory!!
        }

        var keyManagers: Array<KeyManager>? = null
        if (privateKeyAlias != null) {
            keyManagers = arrayOf(CustomKeyManager(privateKeyAlias))
        }

        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, arrayOf<TrustManager>(this), null)
            sslSocketFactory = CustomSSLSocketFactory(sslContext.socketFactory)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }

        return sslSocketFactory!!
    }

    fun getHostnameVerifier(): HostnameVerifier {
        if (hostnameVerifier == null) {
            hostnameVerifier = CustomHostnameVerifier(trustedHostname)
        }

        return hostnameVerifier!!
    }
}
