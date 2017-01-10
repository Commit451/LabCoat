package com.commit451.gitlab.ssl

import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class X509CertificateException : CertificateException {
    val chain: Array<X509Certificate>

    constructor(msg: String, chain: Array<X509Certificate>) : super(msg) {
        this.chain = chain
    }

    constructor(message: String, cause: Throwable, chain: Array<X509Certificate>) : super(message, cause) {
        this.chain = chain
    }

    constructor(cause: Throwable, chain: Array<X509Certificate>) : super(cause) {
        this.chain = chain
    }
}
