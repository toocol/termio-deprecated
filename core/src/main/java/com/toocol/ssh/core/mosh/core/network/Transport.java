package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.statesnyc.UserStream;
import io.vertx.core.datagram.DatagramSocket;

public final class Transport {

    public record Addr(String serverHost, int port, String key) {
        public String serverHost() {
            return serverHost;
        }

        public int port() {
            return port;
        }

        public String key() {
            return key;
        }
    }

    public final Addr addr;

    private TransportSender<UserStream> sender;

    public Transport(String serverHost, int port, String key) {
        this.addr = new Addr(serverHost, port, key);
    }

    public void connect(DatagramSocket socket) {
        this.sender = new TransportSender<>(new UserStream(), this.addr, socket);
    }

    public void send(String diff) {
        sender.sendToReceiver(diff);
    }

    public void tick() {
        sender.tick();
    }
}