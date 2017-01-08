package com.commit451.gitlab.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class X509CertificateException extends CertificateException {
    private final X509Certificate[] chain;

    public X509CertificateException(String msg, X509Certificate[] chain) {
        super(msg);
        this.chain = chain;
    }

    public X509CertificateException(String message, Throwable cause, X509Certificate[] chain) {
        super(message, cause);
        this.chain = chain;
    }

    public X509CertificateException(Throwable cause, X509Certificate[] chain) {
        super(cause);
        this.chain = chain;
    }

    public X509Certificate[] getChain() {
        return this.chain;
    }
}
