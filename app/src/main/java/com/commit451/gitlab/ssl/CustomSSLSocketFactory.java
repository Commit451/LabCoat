package com.commit451.gitlab.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Custom SSL factory so that we can enforce using TLS 1.2 on Android 4.1-4.4 where it is not
 * enabled by default and also have custom trusted https servers
 */
public class CustomSSLSocketFactory extends SSLSocketFactory {

    /**
     * You may be wondering why this is named "delegate"
     * See here for deets:
     * https://github.com/square/okhttp/issues/2323
     */
    private final SSLSocketFactory delegate;

    public CustomSSLSocketFactory(SSLSocketFactory internalFactory) {
        super();
        this.delegate = internalFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableProtocols(delegate.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableProtocols(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableProtocols(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableProtocols(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableProtocols(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableProtocols(delegate.createSocket(address, port, localAddress, localPort));
    }

    private static Socket enableProtocols(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = ((SSLSocket) socket);

            Set<String> supportedProtocols = new HashSet<>(Arrays.asList(sslSocket.getSupportedProtocols()));
            Set<String> enabledProtocols = new HashSet<>(Arrays.asList(sslSocket.getEnabledProtocols()));

            if (supportedProtocols.contains("TLSv1.2")) {
                enabledProtocols.add("TLSv1.1");
            }
            if (supportedProtocols.contains("TLSv1.2")) {
                enabledProtocols.add("TLSv1.2");
            }
            sslSocket.setEnabledProtocols(enabledProtocols.toArray(new String[enabledProtocols.size()]));
        }

        return socket;
    }
}
