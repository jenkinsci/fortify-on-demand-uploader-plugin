package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.io.PrintStream;

class RemoteAgentClient implements IHttpClient {
    private int _connectionTimeout;
    private int _writeTimeout;
    private int _readTimeout;
    private ProxyConfiguration _proxy;
    private VirtualChannel _channel;
    private PrintStream _logger;

    public RemoteAgentClient(int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy, Launcher launcher, PrintStream logger) {
        this(connectionTimeout, writeTimeout, readTimeout, proxy);

        _logger = logger;
        _channel = launcher.getChannel();
        if (_channel == null) {
            throw new IllegalStateException("Launcher doesn't support remoting but it is required");
        }
    }

    public RemoteAgentClient(int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy, VirtualChannel channel, PrintStream logger) {
        this(connectionTimeout, writeTimeout, readTimeout, proxy);

        _logger = logger;
        _channel = channel;
        if (channel == null) {
            throw new IllegalStateException("Launcher doesn't support remoting but it is required");
        }
    }

    private RemoteAgentClient(int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy) {
        _connectionTimeout = connectionTimeout;
        _writeTimeout = writeTimeout;

        _readTimeout = readTimeout;
        _proxy = proxy;
    }

    public ResponseContent execute(HttpRequest request) throws IOException {
        RemoteProxyCallable callable = new RemoteProxyCallable(request, _connectionTimeout, _writeTimeout, _readTimeout, _proxy, _logger);

        try {
            return _channel.call(callable);
        } catch (InterruptedException e) {
            throw new IOException("Remote agent http call failed", e);
        }
    }


}
