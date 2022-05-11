package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.statesnyc.UserStream;

@SuppressWarnings("all")
public class Transport {

    public final String serverHost;
    public final int port;
    public final String key;
    public final TransportSender<UserStream> sender;

    public Transport(String serverHost, int port, String key) {
        this.serverHost = serverHost;
        this.port = port;
        this.key = key;
        this.sender = new TransportSender<>(new UserStream());
    }

    public void tick() {
        sender.tick();
    }
}