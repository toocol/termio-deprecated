package com.toocol.ssh.core.mosh.core;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 22:17
 * @version: 0.0.1
 */
public class MoshOutputStream extends PipedOutputStream {
    public static class Transport {
        final String serverHost;
        final int port;
        final String key;

        public Transport(String serverHost, int port, String key) {
            this.serverHost = serverHost;
            this.port = port;
            this.key = key;
        }
    }

    final DatagramSocket socket;
    final Transport transport;
    final byte[] buff = new byte[1];

    public MoshOutputStream(PipedInputStream in, DatagramSocket socket, Transport transport) throws IOException {
        super(in);
        this.socket = socket;
        this.transport = transport;
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
        socket.send(Buffer.buffer(bytes), transport.port, transport.serverHost);
    }

    @Override
    public void write(int i) throws IOException {
        this.buff[0] = (byte) i;
        socket.send(Buffer.buffer(this.buff), transport.port, transport.serverHost);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.socket.close();
    }
}
