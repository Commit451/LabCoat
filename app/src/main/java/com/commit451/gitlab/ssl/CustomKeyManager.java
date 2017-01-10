package com.commit451.gitlab.ssl;

import android.content.Context;
import android.os.AsyncTask;
import android.security.KeyChain;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.X509ExtendedKeyManager;

public class CustomKeyManager extends X509ExtendedKeyManager {

    private static final Map<String, KeyEntry> keyCache = new ConcurrentHashMap<>();

    public static boolean isCached(String alias) {
        return keyCache.containsKey(alias);
    }

    public static KeyEntry getCachedKey(String alias) {
        return keyCache.get(alias);
    }

    public static void cache(final Context context, final String alias, final KeyCallback callback) {
        //TODO remove this async task in favor of Rx
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    X509Certificate[] chain = KeyChain.getCertificateChain(context, alias);
                    PrivateKey privateKey = KeyChain.getPrivateKey(context, alias);

                    KeyEntry entry = new KeyEntry(alias, chain, privateKey);
                    keyCache.put(alias, entry);
                    callback.onSuccess(entry);
                } catch (Exception e) {
                    callback.onError(e);
                }

                return null;
            }
        }.execute();
    }

    private final KeyEntry entry;

    public CustomKeyManager(String alias) {
        entry = getCachedKey(alias);

        if (entry == null) {
            throw new IllegalStateException("No cached key available");
        }
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return entry.alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        if (!entry.alias.equals(alias)) {
            return null;
        }

        return entry.chain;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return new String[] { entry.alias };
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return new String[0];
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        if (!entry.alias.equals(alias)) {
            return null;
        }

        return entry.privateKey;
    }

    public static class KeyEntry {
        public final String alias;
        public final X509Certificate[] chain;
        public final PrivateKey privateKey;

        public KeyEntry(String alias, X509Certificate[] chain, PrivateKey privateKey) {
            this.alias = alias;
            this.chain = chain;
            this.privateKey = privateKey;
        }
    }

    public interface KeyCallback {
        void onSuccess(KeyEntry entry);
        void onError(Exception e);
    }
}
