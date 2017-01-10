package com.commit451.gitlab.ssl

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.util.*
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Custom SSL factory so that we can enforce using TLS 1.2 on Android 4.1-4.4 where it is not
 * enabled by default and also have custom trusted https servers
 */
class CustomSSLSocketFactory(
        /**
         * You may be wondering why this is named "delegate"
         * See here for deets:
         * https://github.com/square/okhttp/issues/2323
         */
        private val delegate: SSLSocketFactory) : SSLSocketFactory() {

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(): Socket {
        return enableProtocols(delegate.createSocket())
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return enableProtocols(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return enableProtocols(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return enableProtocols(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return enableProtocols(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return enableProtocols(delegate.createSocket(address, port, localAddress, localPort))
    }

    private fun enableProtocols(socket: Socket): Socket {
        if (socket is SSLSocket) {

            val supportedProtocols = HashSet(Arrays.asList(*socket.supportedProtocols))
            val enabledProtocols = HashSet(Arrays.asList(*socket.enabledProtocols))

            if (supportedProtocols.contains("TLSv1.2")) {
                enabledProtocols.add("TLSv1.1")
            }
            if (supportedProtocols.contains("TLSv1.2")) {
                enabledProtocols.add("TLSv1.2")
            }
            socket.enabledProtocols = enabledProtocols.toTypedArray()
        }

        return socket
    }
}
