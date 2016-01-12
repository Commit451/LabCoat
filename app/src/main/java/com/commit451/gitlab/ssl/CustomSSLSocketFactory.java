package com.commit451.gitlab.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class CustomSSLSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory mInternalFactory;

    public CustomSSLSocketFactory(SSLSocketFactory internalFactory) {
        super();

        this.mInternalFactory = internalFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return mInternalFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mInternalFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableProtocols(mInternalFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableProtocols(mInternalFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableProtocols(mInternalFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableProtocols(mInternalFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableProtocols(mInternalFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableProtocols(mInternalFactory.createSocket(address, port, localAddress, localPort));
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
