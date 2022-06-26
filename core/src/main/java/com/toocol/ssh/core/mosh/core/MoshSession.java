package com.toocol.ssh.core.mosh.core;

import com.toocol.ssh.core.mosh.core.network.MoshInputStream;
import com.toocol.ssh.core.mosh.core.network.MoshOutputStream;
import com.toocol.ssh.core.mosh.core.network.Transport;
import com.toocol.ssh.core.mosh.core.statesnyc.UserEvent;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.utils.IpUtil;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:58
 */
public final class MoshSession {

    private static class IO {
        private MoshOutputStream outputStream;
        private MoshInputStream inputStream;
    }

    private final IO io;
    private final Vertx vertx;
    private final Transport transport;
    private final long sessionId;

    private DatagramSocket socket;
    private volatile boolean connected = false;

    public MoshSession(Vertx vertx, long sessionId, String host, int port, String key) {
        this.vertx = vertx;
        this.io = new IO();
        this.sessionId = sessionId;
        this.transport = new Transport(host, port, key);
    }

    // handle by event loop.
    public <T> void connect(Message<T> message) {
        try {
            IpUtil.getLocalIp4Address().ifPresent(localIpv4 -> {
                try {
                    this.socket = vertx.createDatagramSocket(new DatagramSocketOptions());
                    this.transport.connect(socket);
                    this.io.inputStream = new MoshInputStream();
                    this.io.outputStream = new MoshOutputStream(this.io.inputStream, transport);

                    Term term = Term.getInstance();
                    socket.listen(transport.addr.port(), localIpv4.toString().replaceFirst("/", ""), result -> {
                        if (result.succeeded()) {
                            socket.handler(this.io.outputStream::receivePacket);
                            term.printDisplay("Mosh success to listened local port: " + transport.addr.port());
                            this.connected = true;
                            message.reply(null);
                        } else {
                            term.printErr("Mosh fail to listened local port: " + transport.addr.port());
                            message.fail(-1, "Mosh fail to listened local port" + transport.addr.port());
                        }
                    });

                } catch (Exception e) {
                    message.fail(-1, e.getMessage());
                }
            });
        } catch (SocketException e) {
            message.fail(-1, e.getMessage());
        }
    }

    public void tick() {
        transport.tick();
    }

    public void resize(UserEvent.Resize resizeEvent) {
        transport.pushBackEvent(resizeEvent);
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
