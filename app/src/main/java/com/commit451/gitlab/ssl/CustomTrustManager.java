package com.commit451.gitlab.ssl;

import com.commit451.gitlab.api.X509TrustManagerProvider;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Allows for custom configurations, such as custom trusted hostnames, custom trusted certificates,
 * and private keys
 */
public class CustomTrustManager implements X509TrustManager {

    private String trustedCertificate;
    private String trustedHostname;
    private String privateKeyAlias;
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier hostnameVerifier;

    public CustomTrustManager() {}

    public void setTrustedCertificate(String trustedCertificate) {
        if ((this.trustedCertificate == null && trustedCertificate == null) || (this.trustedCertificate != null && this.trustedCertificate.equals(trustedCertificate))) {
            return;
        }

        this.trustedCertificate = trustedCertificate;
        sslSocketFactory = null;
    }

    public void setTrustedHostname(String trustedHostname) {
        if ((this.trustedHostname == null && trustedHostname == null) || (this.trustedHostname != null && this.trustedHostname.equals(trustedHostname))) {
            return;
        }

        this.trustedHostname = trustedHostname;
        hostnameVerifier = null;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {
        if ((this.privateKeyAlias == null && privateKeyAlias == null) || (this.privateKeyAlias != null && this.privateKeyAlias.equals(privateKeyAlias))) {
            return;
        }

        this.privateKeyAlias = privateKeyAlias;
        sslSocketFactory = null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (X509TrustManagerProvider.INSTANCE.get() == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        X509TrustManagerProvider.INSTANCE.get().checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (X509TrustManagerProvider.INSTANCE.get() == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        CertificateException cause;
        try {
            X509TrustManagerProvider.INSTANCE.get().checkServerTrusted(chain, authType);
            return;
        } catch (CertificateException e) {
            cause = e;
        }

        if (trustedCertificate != null && trustedCertificate.equals(X509Util.getFingerPrint(chain[0]))) {
            return;
        }

        throw new X509CertificateException(cause.getMessage(), cause, chain);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (X509TrustManagerProvider.INSTANCE.get() == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        return X509TrustManagerProvider.INSTANCE.get().getAcceptedIssuers();
    }

    public SSLSocketFactory getSSLSocketFactory() {
        if (sslSocketFactory != null) {
            return sslSocketFactory;
        }

        KeyManager[] keyManagers = null;
        if (privateKeyAlias != null) {
            keyManagers = new KeyManager[] { new CustomKeyManager(privateKeyAlias) };
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{this}, null);
            sslSocketFactory = new CustomSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return sslSocketFactory;
    }

    public HostnameVerifier getHostnameVerifier() {
        if (hostnameVerifier == null) {
            hostnameVerifier = new CustomHostnameVerifier(trustedHostname);
        }

        return hostnameVerifier;
    }
}
