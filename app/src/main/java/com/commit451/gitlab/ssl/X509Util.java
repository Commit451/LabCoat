package com.commit451.gitlab.ssl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public final class X509Util {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private X509Util() {}

    public static String getFingerPrint(X509Certificate certificate) throws CertificateEncodingException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        digest.update(certificate.getEncoded());
        return hexify(digest.digest());
    }

    public static String hexify(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 3] = HEX_ARRAY[v >>> 4];
            hexChars[i * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[i * 3 + 2] = ':';
        }

        int length = hexChars.length;
        if (length > 0) {
            return new String(hexChars, 0, length - 1);
        } else {
            return "";
        }
    }
}
