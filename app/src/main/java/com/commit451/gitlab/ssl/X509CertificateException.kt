package com.commit451.gitlab.ssl

import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class X509CertificateException(message: String, cause: Throwable, val chain: Array<X509Certificate>) : CertificateException(message, cause)
