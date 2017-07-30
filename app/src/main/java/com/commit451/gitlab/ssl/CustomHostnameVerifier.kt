package com.commit451.gitlab.ssl

import okhttp3.internal.tls.OkHostnameVerifier
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class CustomHostnameVerifier(private val trustedHostname: String?) : HostnameVerifier {

    companion object {
        private val DEFAULT_HOSTNAME_VERIFIER = OkHostnameVerifier.INSTANCE
    }

    var lastFailedHostname: String? = null
        private set

    override fun verify(hostname: String, session: SSLSession): Boolean {
        if (DEFAULT_HOSTNAME_VERIFIER.verify(hostname, session)) {
            lastFailedHostname = null
            return true
        }

        if (trustedHostname != null && trustedHostname == hostname) {
            lastFailedHostname = null
            return true
        }

        lastFailedHostname = hostname
        return false
    }
}
