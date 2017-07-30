package com.commit451.gitlab.ssl

import com.commit451.gitlab.util.Hash
import java.security.cert.X509Certificate

object X509Util {

    fun getFingerPrint(certificate: X509Certificate): String {
        return Hash.sha1(certificate.encoded)
    }
}
