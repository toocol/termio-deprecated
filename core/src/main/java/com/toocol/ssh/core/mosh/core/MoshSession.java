package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.core.term.core.Term;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:58
 */
public class MoshSession {

    private static class IO {
        private MoshOutputStream outputStream;
        private MoshInputStream inputStream;
    }

    private final IO io;
    private final Vertx vertx;
    private final MoshOutputStream.Transport transport;
    private final long sessionId;

    private DatagramSocket socket;
    private boolean connected = false;

    public MoshSession(Vertx vertx, long sessionId, String host, int port, String key) {
        this.vertx = vertx;
        this.io = new IO();
        this.transport = new MoshOutputStream.Transport(host, port, key);
        this.sessionId = sessionId;
    }

    // handle by event loop.
    public <T> void connect(Message<T> message) {
        try {
            this.socket = vertx.createDatagramSocket(new DatagramSocketOptions());
            this.io.inputStream = new MoshInputStream();
            this.io.outputStream = new MoshOutputStream(this.io.inputStream, socket, transport);

            Term term = Term.getInstance();
            socket.listen(transport.port, "127.0.0.1", result -> {
                if (result.succeeded()) {
                    socket.handler(this.io.outputStream::receivePacket);
                    term.printDisplay("[" + Thread.currentThread().getName() + "] Mosh success to listened local port: " + transport.port);
                } else {
                    term.printDisplay("[" + Thread.currentThread().getName() + "] Mosh fail to listened local port: " + transport.port);
                }
                this.connected = true;
                message.reply(null);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getInputStream() throws IOException {
        if (!connected) {
            throw new IOException("Mosh session is not connected.");
        }
        return this.io.inputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            throw new IOException("Mosh session is not connected.");
        }
        return this.io.outputStream;
    }

    public void close() {
        try {
            this.connected = false;
            this.socket.close();
            this.io.outputStream.close();
            this.io.inputStream.close();
        } catch (Exception e) {
            // do nothing
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public long getSessionId() {
        return sessionId;
    }
}
