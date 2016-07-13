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

    private String mTrustedCertificate;
    private String mTrustedHostname;
    private String mPrivateKeyAlias;
    private SSLSocketFactory mSSLSocketFactory;
    private HostnameVerifier mHostnameVerifier;

    public CustomTrustManager() {}

    public void setTrustedCertificate(String trustedCertificate) {
        if ((mTrustedCertificate == null && trustedCertificate == null) || (mTrustedCertificate != null && mTrustedCertificate.equals(trustedCertificate))) {
            return;
        }

        mTrustedCertificate = trustedCertificate;
        mSSLSocketFactory = null;
    }

    public void setTrustedHostname(String trustedHostname) {
        if ((mTrustedHostname == null && trustedHostname == null) || (mTrustedHostname != null && mTrustedHostname.equals(trustedHostname))) {
            return;
        }

        mTrustedHostname = trustedHostname;
        mHostnameVerifier = null;
    }

    public void setPrivateKeyAlias(String privateKeyAlias) {
        if ((mPrivateKeyAlias == null && privateKeyAlias == null) || (mPrivateKeyAlias != null && mPrivateKeyAlias.equals(privateKeyAlias))) {
            return;
        }

        mPrivateKeyAlias = privateKeyAlias;
        mSSLSocketFactory = null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (X509TrustManagerProvider.get() == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        X509TrustManagerProvider.get().checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (X509TrustManagerProvider.get() == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        CertificateException cause;
        try {
            X509TrustManagerProvider.get().checkServerTrusted(chain, authType);
            return;
        } catch (CertificateException e) {
            cause = e;
        }

        if (mTrustedCertificate != null && mTrustedCertificate.equals(X509Util.getFingerPrint(chain[0]))) {
            return;
        }

        throw new X509CertificateException(cause.getMessage(), cause, chain);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (X509TrustManagerProvider.get() == null) {
            throw new IllegalStateException("No default TrustManager available");
        }

        return X509TrustManagerProvider.get().getAcceptedIssuers();
    }

    public SSLSocketFactory getSSLSocketFactory() {
        if (mSSLSocketFactory != null) {
            return mSSLSocketFactory;
        }

        KeyManager[] keyManagers = null;
        if (mPrivateKeyAlias != null) {
            keyManagers = new KeyManager[] { new CustomKeyManager(mPrivateKeyAlias) };
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{this}, null);
            mSSLSocketFactory = new CustomSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return mSSLSocketFactory;
    }

    public HostnameVerifier getHostnameVerifier() {
        if (mHostnameVerifier == null) {
            mHostnameVerifier = new CustomHostnameVerifier(mTrustedHostname);
        }

        return mHostnameVerifier;
    }
}
